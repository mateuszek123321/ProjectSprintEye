package pl.pollub.android.sprinteyeapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.pollub.android.sprinteyeapp.MainMenuActivity;
import pl.pollub.android.sprinteyeapp.databinding.ActivityBasicSessionBinding;
import pl.pollub.android.sprinteyeapp.mediapipe.TurnOnCameraActivity;
import pl.pollub.android.sprinteyeapp.model.local.AppDatabase;
import pl.pollub.android.sprinteyeapp.model.local.dao.AthleteDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.RunSessionDao;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.entity.RunSession;
import pl.pollub.android.sprinteyeapp.util.UserSessionManager;

public class BasicSessionActivity extends AppCompatActivity {

    private ActivityBasicSessionBinding binding;
    private UserSessionManager sessionManager;
    private AthleteDao athleteDao;
    private RunSessionDao runSessionDao;

    private long userId = -1;
    private long selectedAthleteId = -1;
    private String selectedAthleteName = "";

    // series
    private int cycles = 1;
    private int detections = 1;
    private int seriesTime = 5;

    // distance / detection mode
    private int lapDistance = 400;
    private int expectedDetections = 1;
    private boolean distanceModeEnabled = false;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBasicSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new UserSessionManager(this);
        userId = sessionManager.getCurrentUserId();

        if (userId == -1) {
            Toast.makeText(this, "Użytkownik nie zalogowany.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);
        athleteDao = db.athleteDao();
        runSessionDao = db.runSessionDao();

        setupAthleteDropdown();
        setupSeriesMode();
        setupDistanceMode();
        setupStartButton();

        binding.backButton.setOnClickListener(v -> finish());
    }

    // athlete

    private void setupAthleteDropdown() {
        dbExecutor.execute(() -> {
            List<Athlete> athletes = athleteDao.getAthletesForUser(userId);

            runOnUiThread(() -> {
                if (athletes == null || athletes.isEmpty()) {
                    selectedAthleteId = -1;
                    binding.athleteDropdown.setText("Brak sportowców", false);
                    Toast.makeText(this, "Najpierw dodaj sportowca.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] nicks = new String[athletes.size()];
                for (int i = 0; i < athletes.size(); i++) {
                    nicks[i] = athletes.get(i).nick;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        nicks
                );
                binding.athleteDropdown.setAdapter(adapter);

                binding.athleteDropdown.setOnItemClickListener((p, v, pos, id) -> {
                    Athlete a = athletes.get(pos);
                    selectedAthleteId = a.id;
                    selectedAthleteName = a.nick;
                });

                Athlete first = athletes.get(0);
                selectedAthleteId = first.id;
                selectedAthleteName = first.nick;
                binding.athleteDropdown.setText(selectedAthleteName, false);
            });
        });
    }

    // series mode

    private void setupSeriesMode() {
        binding.seriesOptions.setVisibility(View.GONE);
        updateCyclesText();
        updateDetections();
        updateSeriesTime();

        binding.switchSeries.setOnCheckedChangeListener((b, checked) -> {
            binding.seriesOptions.setVisibility(checked ? View.VISIBLE : View.GONE);

            // series wyklucza distance mode
            if (checked) {
                binding.distanceMode.setChecked(false);
            }
        });

        binding.btnCyclesIncrease.setOnClickListener(v -> {
            cycles++;
            updateCyclesText();
        });

        binding.btnCyclesDecrease.setOnClickListener(v -> {
            if (cycles > 1) cycles--;
            updateCyclesText();
        });

        binding.btnDetectionIncrease.setOnClickListener(v -> {
            detections++;
            updateDetections();
        });

        binding.btnDetectionDecrease.setOnClickListener(v -> {
            if (detections > 1) detections--;
            updateDetections();
        });

        binding.btnSeriesTimeIncrease.setOnClickListener(v -> {
            seriesTime++;
            updateSeriesTime();
        });

        binding.btnSeriesTimeDecrease.setOnClickListener(v -> {
            if (seriesTime > 1) seriesTime--;
            updateSeriesTime();
        });
    }

    //distance mode

    private void setupDistanceMode() {
        binding.detectionCountContainer.setVisibility(View.GONE);
        binding.oneLapDistance.setVisibility(View.GONE);

        binding.distanceMode.setOnCheckedChangeListener((b, checked) -> {
            distanceModeEnabled = checked;

            binding.detectionCountContainer.setVisibility(checked ? View.VISIBLE : View.GONE);
            binding.oneLapDistance.setVisibility(checked ? View.VISIBLE : View.GONE);

            //turn off seriesmode
            if (checked) {
                binding.switchSeries.setChecked(false);
            }

            expectedDetections = 1;
            updateDetections();
        });
    }

    //sesion start

    private void setupStartButton() {
        binding.startBasicSession.setOnClickListener(v -> startBasicSession());
    }

    private void startBasicSession() {

        if (selectedAthleteId == -1) {
            Toast.makeText(this, "Wybierz sportowca!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (distanceModeEnabled) {
            String lapTxt = binding.oneLapDistance.getText().toString().trim();
            if (lapTxt.isEmpty()) {
                Toast.makeText(this, "Podaj dystans okrążenia", Toast.LENGTH_SHORT).show();
                return;
            }

            lapDistance = Integer.parseInt(lapTxt);
            expectedDetections = detections;

            if (lapDistance <= 0 || expectedDetections <= 0) {
                Toast.makeText(this, "Nieprawidłowe dane dystansu", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        boolean isSeriesMode = binding.switchSeries.isChecked();

        dbExecutor.execute(() -> {
            RunSession session = new RunSession();
            session.sessionType = "basic";
            session.attemptName = "Sesja podstawowa " + System.currentTimeMillis();
            session.createdAt = System.currentTimeMillis();
            session.accountUserId = userId;
            session.seriesMode = isSeriesMode;

            if (isSeriesMode) {
                session.seriesCount = cycles;
                session.detectionPerSeries = detections;
                session.seriesLapDistance = lapDistance;
                session.timeBetweenSeries = seriesTime * 60;
                session.plannedDistance = cycles * detections * lapDistance;

            } else if (distanceModeEnabled) {
                session.detectionDistanceMode = true;
                session.detectionLapDistance = lapDistance;
                session.detectionExpectedCount = expectedDetections;
                session.plannedDistance = lapDistance * expectedDetections;

            } else {
                session.detectionDistanceMode = false;
                session.plannedDistance = lapDistance;
            }

            long sessionId = runSessionDao.insert(session);

            runOnUiThread(() -> {
                Intent intent = new Intent(this, TurnOnCameraActivity.class);
                intent.putExtra(TurnOnCameraActivity.EXTRA_SESSION_MODE,
                        TurnOnCameraActivity.SESSION_MODE_BASIC);

                intent.putExtra(BasicSessionCamera.SESSION_ID, sessionId);
                intent.putExtra(BasicSessionCamera.LAP_DISTANCE, lapDistance);
                intent.putExtra(BasicSessionCamera.EXPECTED_DETECTIONS,
                        isSeriesMode ? detections : expectedDetections);
                intent.putExtra(BasicSessionCamera.SERIES_MODE, isSeriesMode);

                if (isSeriesMode) {
                    intent.putExtra(BasicSessionCamera.SERIES_COUNT, cycles);
                    intent.putExtra("series_break_time_seconds", seriesTime * 60);
                }

                startActivity(intent);
            });
        });
    }


    private void updateCyclesText() {
        binding.tvCyclesValue.setText(String.valueOf(cycles));
    }

    private void updateDetections() {
        binding.tvDetectionValue.setText(String.valueOf(detections));
    }

    private void updateSeriesTime() {
        binding.tvSeriesTime.setText(String.valueOf(seriesTime));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
        binding = null;
    }
}