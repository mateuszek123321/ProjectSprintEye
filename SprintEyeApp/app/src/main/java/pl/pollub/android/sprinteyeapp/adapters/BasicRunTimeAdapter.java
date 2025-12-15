package pl.pollub.android.sprinteyeapp.adapters;

import android.view.LayoutInflater;

import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pl.pollub.android.sprinteyeapp.databinding.ItemRuntimeBinding;

public class BasicRunTimeAdapter extends RecyclerView.Adapter<BasicRunTimeAdapter.RuntimeViewHolder> {

    private final List<SessionData> sessions = new ArrayList<>();
    private final Set<Long> selectedSessions = new HashSet<>();
    private OnSessionClickListener clickListener;

    public interface OnSessionClickListener {
        void onSessionClick(SessionData session);
    }

    public BasicRunTimeAdapter(@NonNull OnSessionClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.clickListener = listener;
    }

    public static class SessionData {
        public long sessionId;
        public String attemptName;
        public String athletes;
        public int plannedDistance;
        public long bestTime;
        public String sessionType;
        public long createdAt;
        public boolean isSeriesMode;

        public SessionData(long sessionId, String attemptName, String athletes,
                           int plannedDistance, long bestTime, String sessionType,
                           long createdAt, boolean isSeriesMode) {
            this.sessionId = sessionId;
            this.attemptName = attemptName;
            this.athletes = athletes;
            this.plannedDistance = plannedDistance;
            this.bestTime = bestTime;
            this.sessionType = sessionType;
            this.createdAt = createdAt;
            this.isSeriesMode = isSeriesMode;
        }
    }

    static class RuntimeViewHolder extends RecyclerView.ViewHolder {
        private final ItemRuntimeBinding binding;
        RuntimeViewHolder(@NonNull ItemRuntimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SessionData session, boolean isSelected, OnSessionClickListener listener) {
            binding.tvRunName.setText(session.attemptName);
            binding.tvAthletes.setText(session.athletes);
            binding.tvDistance.setText(session.plannedDistance + "m");
            binding.tvBestTime.setText(formatTime(session.bestTime));

            binding.tvSession.setText(
                    "basic".equalsIgnoreCase(session.sessionType)
                            ? "Sesja Podstawowa"
                            : "Sesja Zaawansowana"
            );

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            binding.tvDateRun.setText(sdf.format(new Date(session.createdAt)));

            binding.checkRun.setOnCheckedChangeListener(null);
            binding.checkRun.setChecked(isSelected);
            binding.checkRun.setOnCheckedChangeListener((buttonView, checked) -> {
                if (listener != null) listener.onSessionClick(session);
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onSessionClick(session);
            });
        }

        private String formatTime(long millis) {
            long totalSeconds = millis / 1000;
            long hundredths = (millis % 1000) / 10;
            return String.format(Locale.US, "%02d.%02d s", totalSeconds, hundredths);
        }
    }

    @NonNull
    @Override
    public RuntimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRuntimeBinding binding = ItemRuntimeBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RuntimeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RuntimeViewHolder holder, int position) {
        SessionData session = sessions.get(position);
        boolean isSelected = selectedSessions.contains(session.sessionId);

        holder.bind(session, isSelected, clickListener);

        holder.itemView.setOnClickListener(v -> toggleSelection(session.sessionId, holder.getBindingAdapterPosition()));
        holder.binding.checkRun.setOnClickListener(v -> toggleSelection(session.sessionId, holder.getBindingAdapterPosition()));
    }

    private void toggleSelection(long sessionId, int position) {
        if (position == RecyclerView.NO_POSITION) return;

        if (selectedSessions.contains(sessionId)) {
            selectedSessions.remove(sessionId);
        } else {
            selectedSessions.add(sessionId);
        }
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public void setSessions(List<SessionData> newSessions) {
        sessions.clear();
        if (newSessions != null) sessions.addAll(newSessions);
        selectedSessions.clear(); // opcjonalnie: reset selekcji po odświeżeniu danych
        notifyDataSetChanged();
    }

    public void selectAll() {
        selectedSessions.clear();
        for (SessionData session : sessions) {
            selectedSessions.add(session.sessionId);
        }
        notifyDataSetChanged();
    }

    public void deselectAll() {
        selectedSessions.clear();
        notifyDataSetChanged();
    }

    public Set<Long> getSelectedSessionIds() {
        return new HashSet<>(selectedSessions);
    }
}