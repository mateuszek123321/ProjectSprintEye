package pl.pollub.android.sprinteyeapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pl.pollub.android.sprinteyeapp.databinding.ItemAthleteBinding;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;

public class AthleteAdapter extends RecyclerView.Adapter<AthleteAdapter.AthleteViewHolder> {
    private final List<Athlete> items = new ArrayList<>();
    private OnAthleteClickListener athleteListener;
    public interface OnAthleteClickListener{
        void onAthleteClick(Athlete athlete);
    }
    //sets click listener
    public void setAthleteListener(OnAthleteClickListener listener){
        this.athleteListener = listener;
    }

    //updates adapter's dataset
    public void submitList(List<Athlete> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    //binding for one Athlete item
    @NonNull
    @Override
    public AthleteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        ItemAthleteBinding binding = ItemAthleteBinding.inflate(inflater, viewGroup, false);
        return new AthleteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AthleteViewHolder holder, int position){
        Athlete athlete =items.get(position);
        holder.binding.tvName.setText(athlete.nick);

        holder.itemView.setOnClickListener(v -> {
            if(athleteListener != null){
                athleteListener.onAthleteClick(athlete);
            }
        });
    }

    public static class AthleteViewHolder extends RecyclerView.ViewHolder{
        final ItemAthleteBinding binding;
        public AthleteViewHolder(@NonNull ItemAthleteBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public int getItemCount(){
        return items.size();
    }
}
