package pl.pollub.android.sprinteyeapp.view;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import pl.pollub.android.sprinteyeapp.R;
import pl.pollub.android.sprinteyeapp.adapters.BasicTimesAdapter;
import pl.pollub.android.sprinteyeapp.databinding.CameraBasicSessionBinding;
import pl.pollub.android.sprinteyeapp.mediapipe.PoseDetectionHelper;
import pl.pollub.android.sprinteyeapp.model.local.AppDatabase;
import pl.pollub.android.sprinteyeapp.model.local.dao.AthleteDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.LapDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.RunDataDao;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.entity.Lap;
import pl.pollub.android.sprinteyeapp.model.local.entity.RunData;
import pl.pollub.android.sprinteyeapp.util.UserSessionManager;

public class BasicSessionCamera extends AppCompatActivity implements PoseDetectionHelper.DetectionListener {
    private static final String TAG = "BasicSessionCamera";

    public static final String SESSION_ID = "session_id";
    public static final String LAP_DISTANCE = "lap_distance";
    public static final String EXPECTED_DETECTIONS = "expected_detections";
    public static final String SERIES_MODE = "series_mode";
    public static final String SERIES_COUNT = "series_count";
    private static final String SERIES_BREAK_TIME = "series_break_time";
    private static final String USER_ID = "user_id";

    private CameraBasicSessionBinding binding;
    private PoseDetectionHelper detectionHelper;
    private RunDataDao runDataDao;
    private LapDao lapDao;
    private AthleteDao athleteDao;
    private BasicTimesAdapter adapter;
    private UserSessionManager sessionManager;

    // stopwatch
    private long stopwatchStartTime = 0L;
    private long elapsedTime = 0L;
    private Handler stopwatchHandler = new Handler(Looper.getMainLooper());
    private boolean isStopwatchRunning = false;

    // audio
    private SoundPool soundPool;
    private boolean soundPoolReady = false;
    private final Random random = new Random();
    private int pendingStreamId = 0;
    private long audioStartRealTimeMs = 0L;
    private int plannedShotOffSetMs = 0;
    private final List<startSound> lowStartSounds = new ArrayList<>();
    private final List<startSound> highStartSounds = new ArrayList<>();
    private int bellSound = -1;
    private final Handler audioHandler = new Handler(Looper.getMainLooper());

    // session config
    private long sessionId;
    private int lapDistance = 400;
    private int expectedDetections = 1;
    private long userId = -1;

    // series mode
    private boolean isSeriesMode = false;
    private int seriesCount = 0;
    private int seriesBreakTime = 0;
    private int currentSeriesNumber = 1;
    private boolean isInBreak = false;

    // current run data
    private List<LapTime> currentRunLaps = new ArrayList<>();
    private int currentRunNumber = 1;
    private long selectedAthleteId = -1;
    private String selectedAthleteName = "Nieznany";
    private String selectedStartType = "Niski";

    private static class LapTime {
        int lapNumber;
        long lapTimeMs;

        LapTime(int lapNumber, long lapTimeMs) {
            this.lapNumber = lapNumber;
            this.lapTimeMs = lapTimeMs;
        }
    }

    private static class startSound {
        final int soundId;
        final int shotOffSetMs;

        startSound(int soundId, int shotDelayMs) {
            this.soundId = soundId;
            this.shotOffSetMs = shotDelayMs;
        }
    }

    private final Runnable stopwatchRunnable = new Runnable() {
        @Override
        public void run() {
            if (isStopwatchRunning) {
                elapsedTime = SystemClock.uptimeMillis() - stopwatchStartTime;
                binding.stopper.setText(formatStopwatch(elapsedTime));
                stopwatchHandler.postDelayed(this, 16);
            }
        }
    };

    private final Runnable startAtShootRunnable = () -> {
        pendingStreamId = 0;

        long targetShotTime = audioStartRealTimeMs + plannedShotOffSetMs;

        elapsedTime = 0L;
        stopwatchStartTime = targetShotTime;
        isStopwatchRunning = true;

        stopwatchHandler.removeCallbacks(stopwatchRunnable);
        stopwatchHandler.post(stopwatchRunnable);

        // Enable detection AFTER stopwatch starts
        detectionHelper.setDetectionEnabled(true);
        Log.d(TAG, "Stopwatch started at shot moment, detection enabled");
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CameraBasicSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get user
        sessionManager = new UserSessionManager(this);
        userId = sessionManager.getCurrentUserId();

        if (userId == -1) {
            Toast.makeText(this, "Użytkownik nie zalogowany ", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        sessionId = getIntent().getLongExtra(SESSION_ID, -1);
        lapDistance = getIntent().getIntExtra(LAP_DISTANCE, 400);
        expectedDetections = getIntent().getIntExtra(EXPECTED_DETECTIONS, 1);
        isSeriesMode = getIntent().getBooleanExtra(SERIES_MODE, false);

        // Get athlete data from Intent
        selectedAthleteId = getIntent().getLongExtra("selected_athlete_id", -1);
        String athleteNameFromIntent = getIntent().getStringExtra("selected_athlete_name");
        if (selectedAthleteId != -1 && athleteNameFromIntent != null && !athleteNameFromIntent.isEmpty()) {
            selectedAthleteName = athleteNameFromIntent;
        }

        if (isSeriesMode) {
            seriesCount = getIntent().getIntExtra(SERIES_COUNT, 1);
            seriesBreakTime = getIntent().getIntExtra(SERIES_BREAK_TIME, 0);
            binding.isSeriesModeOn.setVisibility(View.VISIBLE);
        } else {
            binding.isSeriesModeOn.setVisibility(View.GONE);
        }

        AppDatabase db = AppDatabase.getInstance(this);
        runDataDao = db.runDataDao();
        lapDao = db.lapDao();
        athleteDao = db.athleteDao();

        initSoundPool();
        setupRecyclerView();
        setupDropdowns();
        setupButtons();
        setupDetectionHelper();
    }

    private void setupRecyclerView() {
        adapter = new BasicTimesAdapter();
        binding.recyclerTimesBasic.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTimesBasic.setAdapter(adapter);

        adapter.addRunHeader(currentRunNumber, selectedAthleteName);
    }

    private void setupDropdowns() {
        // start type
        String[] startTypes = { "Niski", "Wysoki" };
        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                startTypes);
        binding.startTypeDropdown.setAdapter(startAdapter);

        binding.startTypeDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedStartType = startTypes[position];
            Log.d(TAG, "Selected start type: " + selectedStartType);
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Athlete> athletes = athleteDao.getAthletesForUser(userId);

            runOnUiThread(() -> {
                if (athletes == null || athletes.isEmpty()) {
                    selectedAthleteName = "Brak sportowców";
                    selectedAthleteId = -1;
                    binding.athleteDropdown.setText(selectedAthleteName, false);
                    return;
                }

                String[] nicks = new String[athletes.size()];
                for (int i = 0; i < athletes.size(); i++) {
                    nicks[i] = athletes.get(i).nick;
                }

                ArrayAdapter<String> athleteAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, nicks);
                binding.athleteDropdown.setAdapter(athleteAdapter);

                binding.athleteDropdown.setOnItemClickListener((parent, view, position, id) -> {
                    Athlete selected = athletes.get(position);
                    selectedAthleteName = selected.nick;
                    selectedAthleteId = selected.id;

                    Log.d(TAG, "Selected athlete: " + selectedAthleteName + "id: " + selectedAthleteId);

                    adapter.clearAllTimes();
                    adapter.addRunHeader(currentRunNumber, selectedAthleteName);
                });

                // default athlete
                if (athletes.size() > 0) {
                    Athlete first = athletes.get(0);
                    selectedAthleteName = first.nick;
                    selectedAthleteId = first.id;
                    binding.athleteDropdown.setText(selectedAthleteName, false);
                }
            });
        });
    }

    private void setupButtons() {
        binding.btnStartRun.setOnClickListener(v -> {
            startRun();
        });

        binding.btnStop.setOnClickListener(v -> {
            stopRun();
        });
        binding.btnStop.setEnabled(false);
        binding.btnNewRun.setOnClickListener(v -> {
            newRun();
        });
        binding.btnRepeat.setOnClickListener(v -> {
            repeatLastRun();
        });
        binding.backButton.setOnClickListener(v -> {
            handleBackButton();
        });
        binding.homeButton.setOnClickListener(v -> {
            handleHomeButton();
        });
    }

    private void handleBackButton() {
        finish();
    }

    private void handleHomeButton() {
        finish();
    }

    private void setupDetectionHelper() {
        detectionHelper = new PoseDetectionHelper(this, this);
        detectionHelper.setFinishLineX(0.75f);

        long delay = calculateDetectionDelay(lapDistance);
        detectionHelper.setDetectionDelays(delay);
        Log.d(TAG, "Detection delay: " + delay);

        detectionHelper.setupPoseLandmarker();
        detectionHelper.setupCamera(this, binding.previewView, binding.overlay);
    }

    private long calculateDetectionDelay(int distance) {
        if (distance == 0) {
            // 20s default delay(no detecion mode)
            return 20000L;
        } else if (distance < 200) {
            return 10000L;
        } else if (distance < 400) {
            return 20000L;
        } else {
            return 40000L;
        }
    }

    // audio
    private void initSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attributes)
                .build();

        int lowStart1 = soundPool.load(this, R.raw.sound_8714_low_start, 1);
        int lowStart2 = soundPool.load(this, R.raw.sound_9263_low_start, 1);
        int lowStart3 = soundPool.load(this, R.raw.sound_9660_low_start, 1);

        int highStart1 = soundPool.load(this, R.raw.sound_6039_high_start, 1);
        int highStart2 = soundPool.load(this, R.raw.sound_5010_high_start, 1);
        int highStart3 = soundPool.load(this, R.raw.sound_5524_high_start, 1);

        bellSound = soundPool.load(this, R.raw.bell_sound, 1);

        lowStartSounds.clear();
        lowStartSounds.add(new startSound(lowStart1, 8714));
        lowStartSounds.add(new startSound(lowStart2, 9263));
        lowStartSounds.add(new startSound(lowStart3, 9660));

        highStartSounds.clear();
        highStartSounds.add(new startSound(highStart1, 6039));
        highStartSounds.add(new startSound(highStart2, 5010));
        highStartSounds.add(new startSound(highStart3, 5524));

        final int totalToLoad = 6;
        final AtomicInteger loadedCount = new AtomicInteger(0);

        soundPoolReady = false;
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                if (loadedCount.incrementAndGet() >= totalToLoad) {
                    soundPoolReady = true;
                    Log.d(TAG, "SoundPool loaded successfully: " + loadedCount.get() + " sounds");
                }
            } else {
                Log.e(TAG, "SoundPool load failed for sound ID: " + sampleId);
            }
        });
    }

    // stopwatch service
    private void configAudioAndStopwatch() {
        if (isStopwatchRunning)
            return;

        if (soundPool == null || !soundPoolReady) {
            Log.w(TAG, "SoundPool not ready, starting stopwatch without audio");
            startStopwatch();
            return;
        }

        final List<startSound> pool = "Niski".equals(selectedStartType) ? lowStartSounds : highStartSounds;

        if (pool.isEmpty()) {
            startStopwatch();
            return;
        }

        startSound chosen = pool.get(random.nextInt(pool.size()));

        if (pendingStreamId != 0) {
            soundPool.stop(pendingStreamId);
            pendingStreamId = 0;
        }
        audioHandler.removeCallbacks(startAtShootRunnable);

        // start audio
        pendingStreamId = soundPool.play(chosen.soundId, 1f, 1f, 1, 0, 1f);
        Log.d(TAG, "Playing start sound, stream ID: " + pendingStreamId);

        audioStartRealTimeMs = SystemClock.uptimeMillis();
        plannedShotOffSetMs = chosen.shotOffSetMs;

        audioHandler.postDelayed(startAtShootRunnable, chosen.shotOffSetMs);

    }

    private void startStopwatch() {
        elapsedTime = 0L;
        stopwatchStartTime = SystemClock.uptimeMillis();
        isStopwatchRunning = true;

        stopwatchHandler.removeCallbacks(stopwatchRunnable);
        stopwatchHandler.post(stopwatchRunnable);

        // Enable detection if audio is not being used
        detectionHelper.setDetectionEnabled(true);
        Log.d(TAG, "Stopwatch started immediately, detection enabled");
    }

    private void stopStopwatch() {
        isStopwatchRunning = false;
        stopwatchHandler.removeCallbacks(stopwatchRunnable);

        audioHandler.removeCallbacks(startAtShootRunnable);

        if (soundPool != null && pendingStreamId != 0) {
            soundPool.stop(pendingStreamId);
            pendingStreamId = 0;
        }
    }

    private void resetStopwatch() {
        elapsedTime = 0L;
        binding.stopper.setText("00:000");
    }

    private String formatStopwatch(long millis) {
        int seconds = (int) (millis / 1000);
        int millisPart = (int) (millis % 1000);
        return String.format(Locale.US, "%02d:%03d", seconds, millisPart);
    }

    // run control

    private void startRun() {
        if (selectedAthleteId == -1) {
            Toast.makeText(this, "Wybierz sportowca", Toast.LENGTH_SHORT).show();
            return;
        }
        currentRunLaps.clear();

        configAudioAndStopwatch();
        // Detection will be enabled after stopwatch starts (in startAtShootRunnable or
        // startStopwatch)

        binding.btnStartRun.setEnabled(false);
        binding.btnStop.setEnabled(true);
    }

    private void stopRun() {
        stopStopwatch();
        detectionHelper.setDetectionEnabled(false);

        // Don't clear laps - user needs them to save with "New" button
        // Don't reset stopwatch - keep final time visible

        binding.btnStartRun.setEnabled(false);
        binding.btnStop.setEnabled(false);
    }

    private void newRun() {
        if (currentRunLaps.isEmpty()) {
            Toast.makeText(this, "Brak danych do zapisania", Toast.LENGTH_SHORT).show();
            return;
        }
        saveCurrentRunToDataBase();

        currentRunNumber++;
        currentRunLaps.clear();

        // Reset stopwatch for next run
        resetStopwatch();

        adapter.addRunHeader(currentRunNumber, selectedAthleteName);

        binding.btnStartRun.setEnabled(true);
        binding.btnStop.setEnabled(false);

        Toast.makeText(this, "Bieg zapisany. Możesz rozpocząć kolejny.", Toast.LENGTH_SHORT).show();
    }

    private void repeatLastRun() {
        if (adapter.getItemCount() <= 1) { // Only header exists
            Toast.makeText(this, "Brak biegów do powtórzenia", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear current in-memory laps
        currentRunLaps.clear();

        // Remove last run from adapter (should remove header + all associated laps)
        adapter.removeLastWholeRun();

        // Decrement run number
        if (currentRunNumber > 1) {
            currentRunNumber--;
        }

        // Re-enable for new attempt
        binding.btnStartRun.setEnabled(true);
        binding.btnStop.setEnabled(false);

        Toast.makeText(this, "Usunięto ostatni bieg", Toast.LENGTH_SHORT).show();
    }

    // calbacks
    @Override
    public void onPoseDetected(PoseLandmarkerResult result, long inferenceTime) {
    }

    @Override
    public void onPoseLost() {
    }

    @Override
    public void onFinishLineCrossed(long timeStamp, PoseDetectionHelper.CrossingData crossingData) {
        runOnUiThread(() -> {
            binding.lottieDetectionLoading.setVisibility(View.VISIBLE);
            binding.lottieDetectionLoading.playAnimation();
            binding.lottieDetectionLoading.postDelayed(() -> {
                binding.lottieDetectionLoading.cancelAnimation();
                binding.lottieDetectionLoading.setVisibility(View.GONE);
            }, 2000);

            long lapTime = timeStamp - stopwatchStartTime;
            int lapNumber = currentRunLaps.size() + 1;

            LapTime lap = new LapTime(lapNumber, lapTime);
            currentRunLaps.add(lap);

            adapter.addLap(lapNumber, lapTime);

            Log.d(TAG, "Lap " + lapNumber + " -time: " + formatStopwatch(lapTime));

            if (!isSeriesMode) {
                handleNormalMode(lapNumber);
            } else {
                handleSeriesMode(lapNumber);
            }
        });
    }

    private void handleNormalMode(int lapNumber) {
        // In normal mode, stopwatch runs continuously until user presses Stop
        // Just re-enable detection for next lap
        long delay = calculateDetectionDelay(lapDistance);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isStopwatchRunning && !isSeriesMode) {
                detectionHelper.setDetectionEnabled(true);
                Log.d(TAG, "Normal mode: Detection re-armed for next lap");
            }
        }, delay);

        Log.d(TAG, "Normal mode: Lap " + lapNumber + " recorded, will re-arm detection in " + delay + "ms");
    }

    private void handleSeriesMode(int lapNumber) {
        int lapsPerSeries = expectedDetections;
        int currentLapInSeries = lapNumber % lapsPerSeries;
        if (currentLapInSeries == 0)
            currentLapInSeries = lapsPerSeries;

        if (currentLapInSeries >= lapsPerSeries) {
            if (currentSeriesNumber < seriesCount) {
                startSeriesBreak();
            } else {
                finishAllSeries();
            }
        } else {
            detectionHelper.setDetectionEnabled(true);
        }
    }

    private void startSeriesBreak() {
        stopStopwatch();
        resetStopwatch();
        isInBreak = true;
        detectionHelper.setDetectionEnabled(false);

        showBreakCountDown(seriesBreakTime);
    }

    private void showBreakCountDown(int seconds) {
        Handler countDownHandler = new Handler(Looper.getMainLooper());
        final int[] remaining = { seconds };

        Runnable countDown = new Runnable() {
            @Override
            public void run() {
                if (remaining[0] > 0 && isInBreak) {
                    binding.tvBreak.setVisibility(View.VISIBLE);
                    binding.stopper.setText(String.format(Locale.US, "%ds", remaining[0]));
                    remaining[0]--;
                    countDownHandler.postDelayed(this, 1000);
                } else if (remaining[0] == 0 && isInBreak) {
                    if (soundPool != null && bellSound != -1) {
                        soundPool.play(bellSound, 1f, 1f, 1, 0, 1f);
                    }
                    binding.tvBreak.setVisibility(View.GONE);
                    startNextSeries();
                }
            }
        };
        countDownHandler.post(countDown);
    }

    private void startNextSeries() {
        isInBreak = false;
        currentSeriesNumber++;
        resetStopwatch();

        configAudioAndStopwatch();
        detectionHelper.setDetectionEnabled(true);
    }

    private void finishAllSeries() {
        stopStopwatch();
        detectionHelper.setDetectionEnabled(false);
        binding.btnStartRun.setEnabled(false);
        binding.btnStop.setEnabled(false);

        Toast.makeText(this, "Cała seria zakończona. Wybierz NOWY aby zapisać", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(String error, int errorCode) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Błąd: " + error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Detection error: " + error);
        });
    }

    // database
    private void saveCurrentRunToDataBase() {
        Executors.newSingleThreadExecutor().execute(() -> {
            RunData runData = new RunData();
            runData.sessionId = sessionId;
            runData.athleteId = selectedAthleteId;
            runData.runOrder = currentRunNumber;

            long totalTime = currentRunLaps.isEmpty() ? 0 : currentRunLaps.get(currentRunLaps.size() - 1).lapTimeMs;
            runData.runTimeMs = totalTime;

            long runId = runDataDao.insert(runData);
            Log.d(TAG, "Run saved: ID=" + runId + ", Time=" + totalTime + "ms");

            for (LapTime lapTime : currentRunLaps) {
                Lap lap = new Lap();
                lap.runId = runId;
                lap.lapNumber = lapTime.lapNumber;
                lap.lapTimeMs = lapTime.lapTimeMs;
                lapDao.insert(lap);
            }
            runOnUiThread(() -> Toast.makeText(this, "Bieg został zapisany.", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (detectionHelper != null) {
            detectionHelper.close();
        }
        stopwatchHandler.removeCallbacksAndMessages(null);
        audioHandler.removeCallbacksAndMessages(null);

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
