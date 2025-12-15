package pl.pollub.android.sprinteyeapp.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import pl.pollub.android.sprinteyeapp.adapters.BasicRunTimeAdapter;
import pl.pollub.android.sprinteyeapp.databinding.ActivityRunResultsBinding;
import pl.pollub.android.sprinteyeapp.model.local.AppDatabase;
import pl.pollub.android.sprinteyeapp.model.local.dao.AthleteDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.LapDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.RunDataDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.RunSessionDao;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.entity.Lap;
import pl.pollub.android.sprinteyeapp.model.local.entity.RunData;
import pl.pollub.android.sprinteyeapp.model.local.entity.RunSession;
import pl.pollub.android.sprinteyeapp.util.UserSessionManager;

public class RunResultsActivity extends AppCompatActivity {
    private ActivityRunResultsBinding binding;
    private BasicRunTimeAdapter adapter;

    private RunSessionDao runSessionDao;
    private RunDataDao runDataDao;
    private LapDao lapDao;
    private AthleteDao athleteDao;
    private UserSessionManager sessionManager;

    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        binding = ActivityRunResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new UserSessionManager(this);
        userId = sessionManager.getCurrentUserId();

        if (userId == -1) {
            Toast.makeText(this, "Musisz być zalogowany!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        binding.backButton.setOnClickListener(v -> {
            finish();
        });

        AppDatabase db = AppDatabase.getInstance(this);
        runSessionDao = db.runSessionDao();
        runDataDao = db.runDataDao();
        lapDao = db.lapDao();
        athleteDao = db.athleteDao();

        setupRecyclerView();
        setupButtons();
        loadSessions();
    }

    private void setupRecyclerView(){
        adapter = new BasicRunTimeAdapter(session -> {
            int lapDist = session.isSeriesMode ? (session.plannedDistance / 2) : 400;

            RunTimesDialog dialog = RunTimesDialog.newInstance(
                    session.sessionId,
                    session.isSeriesMode,
                    lapDist
            );
            dialog.show(getSupportFragmentManager(), "RunTimesDialog");
        });
        binding.recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerResults.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.homeButton.setOnClickListener(v -> finish());

        binding.btnSelectAll.setOnClickListener(v -> {
            adapter.selectAll();
        });

        binding.btnDelete.setOnClickListener(v -> {
            Set<Long> selected = adapter.getSelectedSessionIds();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Zaznacz sesje do usunięcia", Toast.LENGTH_SHORT).show();
                return;
            }
            deleteSessions(selected);
        });

        binding.btnExportCsv.setOnClickListener(v -> {
            Toast.makeText(this, "Wyeksportowano", Toast.LENGTH_SHORT).show();
            //TODO csv
        });
    }

    private void loadSessions() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<RunSession> sessions = runSessionDao.getSessionsForUser(userId);

            if (sessions == null || sessions.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Brak zapisanych sesji", Toast.LENGTH_SHORT).show());
                return;
            }

            List<BasicRunTimeAdapter.SessionData> sessionDataList = new ArrayList<>();

            for (RunSession session : sessions) {
                List<RunData> runs = runDataDao.getRunsForSession(session.id);

                Set<String> athleteNames = new HashSet<>();
                long bestTime = Long.MAX_VALUE;

                for (RunData run : runs) {
                    Athlete athlete = athleteDao.getById(run.athleteId);
                    if (athlete != null) {
                        athleteNames.add(athlete.nick);
                    }

                    // Get best lap time
                    List<Lap> laps = lapDao.getLapsForRun(run.id);
                    for (Lap lap : laps) {
                        if (lap.lapTimeMs < bestTime) {
                            bestTime = lap.lapTimeMs;
                        }
                    }
                }

                String athletesStr = String.join(", ", athleteNames);
                if (athletesStr.isEmpty())
                    athletesStr = "Brak danych";
                if (bestTime == Long.MAX_VALUE)
                    bestTime = 0;

                sessionDataList.add(new BasicRunTimeAdapter.SessionData(
                        session.id,
                        session.attemptName,
                        athletesStr,
                        session.plannedDistance,
                        bestTime,
                        session.sessionType,
                        session.createdAt,
                        session.seriesMode));
            }

            runOnUiThread(() -> adapter.setSessions(sessionDataList));
        });
    }
    private void deleteSessions(Set<Long> sessionIds) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (Long sessionId : sessionIds) {
                runSessionDao.deleteById(sessionId);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Usunięto " + sessionIds.size() + " sesji", Toast.LENGTH_SHORT).show();
                adapter.deselectAll();
                loadSessions(); // Reload
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
