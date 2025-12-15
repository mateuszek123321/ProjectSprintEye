package pl.pollub.android.sprinteyeapp.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.airbnb.lottie.L;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.pollub.android.sprinteyeapp.R;
import pl.pollub.android.sprinteyeapp.databinding.DialogAddAthleteBinding;
import pl.pollub.android.sprinteyeapp.databinding.ItemShoeBinding;
import pl.pollub.android.sprinteyeapp.model.local.AppDatabase;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.entity.ShoeModel;
import pl.pollub.android.sprinteyeapp.model.local.entity.ShoeType;
import pl.pollub.android.sprinteyeapp.viewmodel.ManageAthletesViewModel;

public class AddAthleteDialog extends Dialog {

    private DialogAddAthleteBinding binding;
    private final ManageAthletesViewModel viewModel;
    private final Athlete athleteToEdit;
    private final boolean isEditMode;
    private Handler nickValidationHandler = new Handler(Looper.getMainLooper());
    private Runnable nickValidationRunnable;
    private final List<View> longDistanceShoeSlots = new ArrayList<>();
    private final List<View> shortDistanceShoeSlots = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AddAthleteDialog(@NonNull Context context, ManageAthletesViewModel viewModel, @Nullable Athlete athleteToEdit){
        super(context);
        this.viewModel = viewModel;
        this.athleteToEdit = athleteToEdit;
        this.isEditMode = (athleteToEdit != null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = DialogAddAthleteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //fullscreen
        if(getWindow() != null){
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        setCancelable(true);

        if(isEditMode){
            binding.tvTitleEditData.setVisibility(View.VISIBLE);
            fillFieldWithData();
        }
        setupShoeButtons();
        setupSaveButtons();
        validateNick();
    }


    private void fillFieldWithData(){
        binding.addName.setText(athleteToEdit.nick);

        if(athleteToEdit.weight != null ){
            binding.addWeight.setText(String.valueOf(athleteToEdit.weight));
        }
        if(athleteToEdit.height != null){
            binding.addHeight.setText(String.valueOf(athleteToEdit.height));
        }
        if(athleteToEdit.age != null){
            binding.addAge.setText(String.valueOf(athleteToEdit.age));
        }

        if(athleteToEdit.genderId == 1){
            binding.genderMale.setChecked(true);
        }else if(athleteToEdit.genderId == 2){
            binding.genderFemale.setChecked(true);
        }else{
            binding.genderNotProvide.setChecked(true);
        }

        loadShoes();
    }

    private void loadShoes(){
        executor.execute(() -> {
            List<ShoeModel> shoes = viewModel.getShoesForAthleteSync(athleteToEdit.id);

            if (binding == null) return;

            binding.getRoot().post(() -> {
                if (binding == null || !isShowing()) return;
                if (shoes == null || shoes.isEmpty()) return;

                for (ShoeModel shoe : shoes) {
                    addShoeSlot(shoe.shoeType == ShoeType.LONG_DISTANCE, shoe.shoeName);
                }
            });
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        executor.shutdownNow();
        binding = null;
    }

    private void setupShoeButtons(){
        binding.btnAddShoeShortDistance.setOnClickListener(v -> {
            addShoeSlot(false, null);
        });
        binding.btnAddShoeLongDistance.setOnClickListener(v -> {
            addShoeSlot(true, null);
        });

    }

    private void addShoeSlot(boolean isLongDistance, @Nullable String shoeName){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        ItemShoeBinding shoeBinding = ItemShoeBinding.inflate(inflater, binding.shoeModelsList, false);

        if(shoeName != null){
            shoeBinding.shoeModel.setText(shoeName);
        }

        shoeBinding.btnDeleteShoe.setOnClickListener(v -> {
            binding.shoeModelsList.removeView(shoeBinding.getRoot());

            if (isLongDistance){
                longDistanceShoeSlots.remove(shoeBinding.getRoot());
            }else{
                shortDistanceShoeSlots.remove(shoeBinding.getRoot());
            }
        });

        if (isLongDistance){
            int insertId = binding.shoeModelsList.indexOfChild(binding.btnAddShoeShortDistance);
            binding.shoeModelsList.addView(shoeBinding.getRoot(), insertId);
            longDistanceShoeSlots.add(shoeBinding.getRoot());
        }else{
            binding.shoeModelsList.addView(shoeBinding.getRoot());
            shortDistanceShoeSlots.add(shoeBinding.getRoot());
        }
    }

    private void setupSaveButtons(){
        binding.btnSaveAthlete.setOnClickListener(v -> {
            if(validateInput()){
                if(isEditMode){
                    updateAthlete();
                }else{
                    addNewAthlete();
                }
            }
        });
        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    private void validateNick(){
        binding.addName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(nickValidationHandler != null){
                    nickValidationHandler.removeCallbacks(nickValidationRunnable);
                }
                String nick = s.toString().trim();

                if(nick.length() < 1){
                    binding.addName.setError(null);
                    return;
                }
                nickValidationRunnable = () -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        long excludeId = isEditMode ? athleteToEdit.id : -1;
                        boolean  isTaken = viewModel.isNickTaken(nick, excludeId);

                        nickValidationHandler.post(() -> {
                            if(isTaken){
                                binding.addName.setError("Nick zajęty");
                            }else{
                                binding.addName.setError(null);
                            }
                        });
                    });
                };
                nickValidationHandler.postDelayed(nickValidationRunnable, 500);
            }
        });
    }
    private boolean validateInput(){
        String nick = binding.addName.getText().toString().trim();
        if(nick.isEmpty()){
            binding.addName.setError("Nick jest wymagany");
            binding.addName.requestFocus();
            return false;
        }

        String weightStr = binding.addWeight.getText().toString().trim();
        if(!weightStr.isEmpty()){
            try {
                float weight = Float.parseFloat(weightStr);
                if(weight <= 0 || weight > 300){
                    binding.addWeight.setError("Nieprawidłowa waga!");
                    return false;
                }
            }catch (NumberFormatException e){
                binding.addWeight.setError("Nieprawidłowa wartość!");
                return false;
            }
        }

        String heightStr = binding.addHeight.getText().toString().trim();
        if(!heightStr.isEmpty()){
            try{
                float height = Float.parseFloat(heightStr);
                if(height <= 0 || height > 250){
                    binding.addHeight.setError("Nieprawidłowy wzrost");
                    return false;
                }
            }catch (NumberFormatException e){
                binding.addHeight.setError("Nieprawidłowa wartość");
                return false;
            }
        }

        String ageStr = binding.addAge.getText().toString().trim();
        if(!ageStr.isEmpty()) {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 1 || age > 100) {
                    binding.addAge.setError("Nieprawidłowy wiek");
                    return false;
                }
            }catch (NumberFormatException e){
                binding.addAge.setError("Nieprawidłowa wartość");
                return false;
            }
        }
        return true;
    }
    private void addNewAthlete(){
        String name = binding.addName.getText().toString().trim();
        Float weight = parseFloatOrNull(binding.addWeight.getText().toString());
        Float height = parseFloatOrNull(binding.addHeight.getText().toString());
        Integer age = parseIntOrNull(binding.addAge.getText().toString());
        long genderId = getSelectedGenderId();

        List<String> shoesShort = shoeNamesFromSlots(shortDistanceShoeSlots);
        List<String> shoesLong = shoeNamesFromSlots(longDistanceShoeSlots);

        viewModel.addAthlete(name, weight, height, age, genderId, shoesShort, shoesLong);

        Toast.makeText(getContext(), "Dodano sportowca!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void updateAthlete(){
        athleteToEdit.nick = binding.addName.getText().toString().trim();
        athleteToEdit.weight = parseFloatOrNull(binding.addWeight.getText().toString());
        athleteToEdit.height = parseFloatOrNull(binding.addHeight.getText().toString());
        athleteToEdit.age = parseIntOrNull(binding.addAge.getText().toString());
        athleteToEdit.genderId = getSelectedGenderId();

        List<String> shoesShort = shoeNamesFromSlots(shortDistanceShoeSlots);
        List<String> shoesLong = shoeNamesFromSlots(longDistanceShoeSlots);

        viewModel.updateAthleteWithShoes(athleteToEdit, shoesShort, shoesLong);

        Toast.makeText(getContext(), "Zaktualizowano dane!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private List<String> shoeNamesFromSlots(List<View> slots){
        List<String> shoeNames = new ArrayList<>();

        for(View slotView : slots){
            EditText editText = slotView.findViewById(R.id.shoeModel);
            if(editText != null){
                String shoeName = editText.getText().toString().trim();
                if(!shoeName.isEmpty()){
                    shoeNames.add(shoeName);
                }
            }
        }
        return shoeNames;
    }

    private long getSelectedGenderId(){
        int selectedGender = binding.genderGroup.getCheckedRadioButtonId();
        if(selectedGender == binding.genderMale.getId()){
            return 1L;
        }else if(selectedGender == binding.genderFemale.getId()){
            return 2L;
        }else{
            return 3L;
        }
    }
    private Float parseFloatOrNull(String value){
        if(value == null || value.trim().isEmpty()){
            return null;
        }
        try{
            return Float.parseFloat(value.trim());
        }catch (NumberFormatException e){
            return null;
        }
    }
    private Integer parseIntOrNull(String value){
        if(value == null || value.trim().isEmpty()){
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        }catch (NumberFormatException e){
            return null;
        }
    }
}
