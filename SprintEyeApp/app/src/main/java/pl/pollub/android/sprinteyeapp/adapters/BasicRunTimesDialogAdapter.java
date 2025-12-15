package pl.pollub.android.sprinteyeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pl.pollub.android.sprinteyeapp.R;
import pl.pollub.android.sprinteyeapp.databinding.ItemTimeBasicNoSeriesBinding;
import pl.pollub.android.sprinteyeapp.databinding.ItemTimeBasicSeriesBinding;
import pl.pollub.android.sprinteyeapp.model.local.entity.RunData;

public class BasicRunTimesDialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_NO_SERIES = 0;
    private static final int VIEW_TYPE_SERIES = 1;

    private final List<RunData> runs = new ArrayList<>();
    private final boolean isSeriesMode;

    public BasicRunTimesDialogAdapter(boolean isSeriesMode) {
        this.isSeriesMode = isSeriesMode;
    }

    // Jeśli masz już model RunData w innym miejscu – usuń tę klasę i użyj swojej.
    public static class RunData {
        public int runNumber;
        public int distance;
        public String athleteName;
        public List<BasicLapTimesAdapter.LapData> laps;

        public RunData(int runNumber, int distance, String athleteName, List<BasicLapTimesAdapter.LapData> laps) {
            this.runNumber = runNumber;
            this.distance = distance;
            this.athleteName = athleteName;
            this.laps = laps;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return isSeriesMode ? VIEW_TYPE_SERIES : VIEW_TYPE_NO_SERIES;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SERIES) {
            ItemTimeBasicSeriesBinding binding =
                    ItemTimeBasicSeriesBinding.inflate(inflater, parent, false);
            return new SeriesViewHolder(binding);
        } else {
            ItemTimeBasicNoSeriesBinding binding =
                    ItemTimeBasicNoSeriesBinding.inflate(inflater, parent, false);
            return new NoSeriesViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RunData run = runs.get(position);

        if (holder instanceof SeriesViewHolder) {
            ((SeriesViewHolder) holder).bind(run);
        } else if (holder instanceof NoSeriesViewHolder) {
            ((NoSeriesViewHolder) holder).bind(run);
        }
    }

    @Override
    public int getItemCount() {
        return runs.size();
    }

    public void setRuns(List<RunData> newRuns) {
        runs.clear();
        if (newRuns != null) runs.addAll(newRuns);
        notifyDataSetChanged();
    }

    public void addRun(RunData run) {
        if (run == null) return;
        runs.add(run);
        notifyItemInserted(runs.size() - 1);
    }

    public void clear() {
        runs.clear();
        notifyDataSetChanged();
    }

    // ------------------- ViewHolders -------------------

    static class NoSeriesViewHolder extends RecyclerView.ViewHolder {
        private final ItemTimeBasicNoSeriesBinding binding;

        NoSeriesViewHolder(@NonNull ItemTimeBasicNoSeriesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RunData run) {
            binding.tvRunNumber.setText(run.runNumber + ". Bieg");
            binding.tvRunDistance.setText(run.distance + "m");
            binding.tvAthlete.setText(run.athleteName);

            BasicLapTimesAdapter lapAdapter = new BasicLapTimesAdapter();
            binding.tvLapsNoSeries.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            binding.tvLapsNoSeries.setAdapter(lapAdapter);
            lapAdapter.setLaps(run.laps);
        }
    }

    static class SeriesViewHolder extends RecyclerView.ViewHolder {
        private final ItemTimeBasicSeriesBinding binding;

        SeriesViewHolder(@NonNull ItemTimeBasicSeriesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RunData run) {
            binding.tvSeriesNumber.setText("Seria " + run.runNumber);
            binding.tvDistanceHeader.setText(run.distance + "m");

            binding.tvSeriesBreakTime.setText("Przerwa: N/A");

            BasicLapTimesAdapter lapAdapter = new BasicLapTimesAdapter();
            binding.tvLapsSeries.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            binding.tvLapsSeries.setAdapter(lapAdapter);
            lapAdapter.setLaps(run.laps);
        }
    }
}
