package pl.pollub.android.sprinteyeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pl.pollub.android.sprinteyeapp.R;
import pl.pollub.android.sprinteyeapp.databinding.SingleCellForBasicTimesBinding;

public class BasicLapTimesAdapter extends RecyclerView.Adapter<BasicLapTimesAdapter.LapViewHolder> {

    private final List<LapData> laps = new ArrayList<>();

    public static class LapData {
        public int lapNumber;
        public long lapTimeMs;

        public LapData(int lapNumber, long lapTimeMs) {
            this.lapNumber = lapNumber;
            this.lapTimeMs = lapTimeMs;
        }
    }

    static class LapViewHolder extends RecyclerView.ViewHolder {
        private final SingleCellForBasicTimesBinding binding;

        LapViewHolder(@NonNull SingleCellForBasicTimesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LapData lap) {
            binding.tvLapNumber.setText("Okrążenie " + lap.lapNumber + ":");
            binding.tvLapTime.setText(formatTime(lap.lapTimeMs));
        }

        private String formatTime(long millis) {
            long totalSeconds = millis / 1000;
            long hundredths = (millis % 1000) / 10;
            return String.format(Locale.US, "%02d.%02d", totalSeconds, hundredths);
        }
    }

    @NonNull
    @Override
    public LapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SingleCellForBasicTimesBinding binding = SingleCellForBasicTimesBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new LapViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LapViewHolder holder, int position) {
        holder.bind(laps.get(position));
    }

    @Override
    public int getItemCount() {
        return laps.size();
    }

    public void setLaps(List<LapData> newLaps) {
        laps.clear();
        if (newLaps != null) laps.addAll(newLaps);
        notifyDataSetChanged();
    }

    public void addLap(LapData lap) {
        if (lap == null) return;
        laps.add(lap);
        notifyItemInserted(laps.size() - 1);
    }

    public void clear() {
        laps.clear();
        notifyDataSetChanged();
    }
}

