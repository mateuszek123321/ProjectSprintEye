package pl.pollub.android.sprinteyeapp.adapters;

import android.annotation.SuppressLint;
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


public class BasicTimesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_RUN_HEADER = 0;
    private static final int TYPE_LAP = 1;
    private final List<Object> items = new ArrayList<>();

    public static class RunItem {
        public int runNumber;
        public String athleteName;

        public RunItem(int runNumber, String athleteName) {
            this.runNumber = runNumber;
            this.athleteName = athleteName;
        }
    }

    public static class LapItem {
        public int lapNumber;
        public long lapTimeMs;

        public LapItem(int lapNumber, long lapTimeMs) {
            this.lapNumber = lapNumber;
            this.lapTimeMs = lapTimeMs;
        }

        public String getFormattedTime() {
            int seconds = (int) (lapTimeMs / 1000);
            int milliseconds = (int) (lapTimeMs % 1000);
            return String.format(Locale.US, "%02d.%03d", seconds, milliseconds);
        }
    }

    static class RunHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvRunNumber;
        TextView tvAthleteName;

        RunHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRunNumber = itemView.findViewById(R.id.tv_run_number);
            tvAthleteName = itemView.findViewById(R.id.tv_athlete_name);
        }

        void bind(RunItem runItem) {
            tvRunNumber.setText(String.valueOf(runItem.runNumber));
            tvAthleteName.setText(runItem.athleteName);
        }
    }

    static class LapViewHolder extends RecyclerView.ViewHolder {
        TextView tvLapNumber;
        TextView tvLapTime;

        LapViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLapNumber = itemView.findViewById(R.id.tv_lap_number);
            tvLapTime = itemView.findViewById(R.id.tv_time_lap_value);
        }

        void bind(LapItem lapItem) {
            tvLapNumber.setText(String.valueOf(lapItem.lapNumber));
            tvLapTime.setText(lapItem.getFormattedTime());
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof RunItem) {
            return TYPE_RUN_HEADER;
        } else {
            return TYPE_LAP;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_RUN_HEADER) {
            View view = inflater.inflate(R.layout.item_camera_time_basic_session_header, parent, false);
            return new RunHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_camera_time_basic_session, parent, false);
            return new LapViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof RunHeaderViewHolder) {
            ((RunHeaderViewHolder) holder).bind((RunItem) item);
        } else if (holder instanceof LapViewHolder) {
            ((LapViewHolder) holder).bind((LapItem) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addRunHeader(int runNumber, String athleteName) {
        items.add(new RunItem(runNumber, athleteName));
        notifyItemInserted(items.size() - 1);
    }

    public void addLap(int lapNumber, long lapTimeMs) {
        items.add(new LapItem(lapNumber, lapTimeMs));
        notifyItemInserted(items.size() - 1);
    }

    public void removeLastWholeRun() {
        if (items.isEmpty())
            return;

        // Find the last run header
        int lastHeaderIndex = -1;
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i) instanceof RunItem) {
                lastHeaderIndex = i;
                break;
            }
        }

        if (lastHeaderIndex == -1)
            return;

        // Remove all items from the last header onwards (header + all its laps)
        int itemsToRemove = items.size() - lastHeaderIndex;
        for (int i = 0; i < itemsToRemove; i++) {
            items.remove(lastHeaderIndex);
        }
        notifyItemRangeRemoved(lastHeaderIndex, itemsToRemove);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearAllTimes() {
        items.clear();
        notifyDataSetChanged();
    }

    public int getCurrentLapCount() {
        int count = 0;

        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i) instanceof RunItem)
                break;
            if (items.get(i) instanceof LapItem)
                count++;
        }
        return count;
    }
}