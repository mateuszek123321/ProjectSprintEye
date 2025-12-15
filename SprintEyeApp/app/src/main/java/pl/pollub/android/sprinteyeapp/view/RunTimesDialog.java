package pl.pollub.android.sprinteyeapp.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import pl.pollub.android.sprinteyeapp.R;
import pl.pollub.android.sprinteyeapp.adapters.BasicLapTimesAdapter;
import pl.pollub.android.sprinteyeapp.adapters.BasicRunTimesDialogAdapter;
import pl.pollub.android.sprinteyeapp.databinding.ItemTimeBasicRunBinding;
import pl.pollub.android.sprinteyeapp.model.local.AppDatabase;
import pl.pollub.android.sprinteyeapp.model.local.dao.AthleteDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.LapDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.RunDataDao;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.entity.Lap;
import pl.pollub.android.sprinteyeapp.model.local.entity.RunData;

public class RunTimesDialog extends BottomSheetDialogFragment {
    private static final String ARG_SESSION_ID = "session_id";
    private static final String ARG_IS_SERIES = "is_series";
    private static final String ARG_LAP_DISTANCE = "lap_distance";
    private long sessionId;
    private boolean isSeriesMode;
    private int lapDistance;
    private BasicRunTimesDialogAdapter adapter;
    private ItemTimeBasicRunBinding binding;
    private RunDataDao runDataDao;
    private LapDao lapDao;
    private AthleteDao athleteDao;

    public static RunTimesDialog newInstance(long sessionId, boolean isSeriesMode, int lapDistance){
        RunTimesDialog dialog = new RunTimesDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_SESSION_ID, sessionId);
        args.putBoolean(ARG_IS_SERIES, isSeriesMode);
        args.putInt(ARG_LAP_DISTANCE, lapDistance);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sessionId = getArguments().getLong(ARG_SESSION_ID);
            isSeriesMode = getArguments().getBoolean(ARG_IS_SERIES);
            lapDistance = getArguments().getInt(ARG_LAP_DISTANCE, 400);
        }

        AppDatabase db = AppDatabase.getInstance(requireContext());
        runDataDao = db.runDataDao();
        lapDao = db.lapDao();
        athleteDao = db.athleteDao();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_time_basic_run, null);
        dialog.setContentView(view);

        RecyclerView recyclerView = view.findViewById(R.id.tv_times);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BasicRunTimesDialogAdapter(isSeriesMode);
        recyclerView.setAdapter(adapter);

        loadRunData();

        return dialog;
    }
    private void loadRunData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<RunData> runs = runDataDao.getRunsForSession(sessionId);

            if (runs == null || runs.isEmpty()) {
                requireActivity()
                        .runOnUiThread(() -> Toast.makeText(getContext(), "Brak danych", Toast.LENGTH_SHORT).show());
                return;
            }

            List<BasicRunTimesDialogAdapter.RunData> dialogRuns = new ArrayList<>();

            for (RunData run : runs) {
                List<Lap> laps = lapDao.getLapsForRun(run.id);

                Athlete athlete = athleteDao.getById(run.athleteId);
                String athleteName = athlete != null ? athlete.nick : "Nieznany";

                // Convert laps to LapData
                List<BasicLapTimesAdapter.LapData> lapDataList = new ArrayList<>();
                for (Lap lap : laps) {
                    lapDataList.add(new BasicLapTimesAdapter.LapData(lap.lapNumber, lap.lapTimeMs));
                }

                // Create RunData for adapter
                dialogRuns.add(new BasicRunTimesDialogAdapter.RunData(
                        run.runOrder,
                        lapDistance,
                        athleteName,
                        lapDataList));
            }

            requireActivity().runOnUiThread(() -> adapter.setRuns(dialogRuns));
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
