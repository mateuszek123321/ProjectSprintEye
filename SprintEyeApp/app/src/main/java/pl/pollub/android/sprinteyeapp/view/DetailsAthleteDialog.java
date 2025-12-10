package pl.pollub.android.sprinteyeapp.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.List;

import pl.pollub.android.sprinteyeapp.R;
import pl.pollub.android.sprinteyeapp.databinding.DialogAthleteDetailsBinding;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.entity.ShoeModel;
import pl.pollub.android.sprinteyeapp.model.local.entity.ShoeType;
import pl.pollub.android.sprinteyeapp.viewmodel.ManageAthletesViewModel;

public class DetailsAthleteDialog extends Dialog {
    private final Athlete athlete;
    private final OnEditClickListener editListener;
    private final OnDeleteClickListener deleteListener;
    private final ManageAthletesViewModel viewModel;
    private DialogAthleteDetailsBinding binding;
    public interface OnEditClickListener{
        void onEditClick(Athlete athlete);
    }
    public interface OnDeleteClickListener{
        void onDeleteClick(Athlete athlete);
    }
    public DetailsAthleteDialog(@NonNull Context context, Athlete athlete, ManageAthletesViewModel viewModel, OnEditClickListener editListener, OnDeleteClickListener deleteListener){
        super(context);
        this.athlete = athlete;
        this.viewModel = viewModel;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        binding = DialogAthleteDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fillFieldWithData();
        loadShoes();
        setupButtons();
    }


    private void fillFieldWithData(){
        binding.tvName.setText(athlete.nick);

        if(athlete.weight != null){
            binding.tvWeight.setText(athlete.weight + " kg");
        }else{
            binding.tvWeight.setText(" - ");
        }

        if(athlete.height != null){
            binding.tvHeight.setText(athlete.height + " cm");
        }else{
            binding.tvHeight.setText(" - ");
        }

        if(athlete.age != null){
            binding.tvAge.setText(athlete.age + " lat");
        }else{
            binding.tvAge.setText(" - ");
        }

        String genderT;
        if(athlete.genderId == 1){
            genderT = "Mężczyzna";
        }else if(athlete.genderId == 2){
            genderT = "Kobieta";
        }else{
            genderT = "Nie podano";
        }
        binding.tvGender.setText(genderT);
    }

    private void loadShoes(){
        viewModel.getShoesForAthlete(athlete.id).observe((LifecycleOwner) getContext(), shoes -> {
            displayShoes(shoes);
        });
    }
    private  void displayShoes(List<ShoeModel> shoes){
        if(shoes == null || shoes.isEmpty()){
            binding.tvShoeModel.setText("Nie dodano");
            return;
        }
        StringBuilder shoesText = new StringBuilder();

        List<ShoeModel> longDistance = new ArrayList<>();
        List<ShoeModel> shortDistance = new ArrayList<>();

        for(ShoeModel shoe : shoes){
            if (shoe.shoeType == ShoeType.LONG_DISTANCE){
                longDistance.add(shoe);
            }else{
                shortDistance.add(shoe);
            }
        }
        /*
        StringBuilder shoesText = new StringBuilder();

        if (!longDistance.isEmpty()) {
            shoesText.append("Długi dystans:\n");
            for (ShoeModel shoe : longDistance) {
                shoesText.append("• ").append(shoe.shoeName).append("\n");
            }
            shoesText.append("\n");
        }

        if (!shortDistance.isEmpty()) {
            shoesText.append("Krótki dystans:\n");
            for (ShoeModel shoe : shortDistance) {
                shoesText.append("• ").append(shoe.shoeName).append("\n");
            }
        }
         */
        binding.tvShoeModel.setText(shoesText.toString().trim());
    }

    private void setupButtons(){
        binding.btnEdit.setOnClickListener(v -> {
            if(editListener != null){
                editListener.onEditClick(athlete);
            }
            dismiss();
        });

        binding.btnDelete.setOnClickListener(v -> {
            if(deleteListener != null){
                deleteListener.onDeleteClick(athlete);
            }
            dismiss();
        });
    }
}
