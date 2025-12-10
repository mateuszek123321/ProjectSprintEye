package pl.pollub.android.sprinteyeapp.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.SimpleFormatter;

import pl.pollub.android.sprinteyeapp.MainMenuActivity;
import pl.pollub.android.sprinteyeapp.databinding.ActivityRegisterBinding;
import pl.pollub.android.sprinteyeapp.model.local.AppDatabase;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.viewmodel.ManageAthletesViewModel;
import pl.pollub.android.sprinteyeapp.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;
    private final Handler userNameValidationHandler = new Handler(Looper.getMainLooper());
    private Runnable userNameValidationRunnable;
    private final Handler emailValidationHandler = new Handler(Looper.getMainLooper());
    private Runnable emailValidationRunnable;
    private final Handler passwordValidationHandler = new Handler(Looper.getMainLooper());
    private Runnable passwordValidationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupToolbarButtons();
        setupRegisterButton();
        setupViewFlipperNavigation();
        observeRegistrationResult();
        setupUserNameValidation();
        setupEmailValidation();
        setupPasswordValidation();
        observeEmailExistsResult();
        observeUsernameExistsResult();
    }

    private void setupToolbarButtons(){
        binding.homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });
        binding.backButton.setOnClickListener(v -> {
            handleBackPress();
        });
        binding.accountOptionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, AccountSettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setupViewFlipperNavigation(){
        binding.registerNextButton.setOnClickListener(v -> {
            if(validateFirstScreen()){
                binding.viewFlipper.showPrevious();
            }
        });
    }
    private void handleBackPress(){
        if(binding.viewFlipper.getDisplayedChild() > 0){
            binding.viewFlipper.showPrevious();
        }else{
            finish();
        }
    }

    private void setupRegisterButton(){
        binding.btnRegister.setOnClickListener(v -> {
            if(validateSecondScreen()){
                registerNewUser();
            }
        });
    }

    //for asynchronic operation(background thread)
    private void observeRegistrationResult(){
        viewModel.getRegistrationResult().observe(this, result -> {
            if(result == null) return;

            switch (result.status){
                case LOADING:
                    binding.btnRegister.setEnabled(false);
                    binding.lottieLoadingCircle.setVisibility(View.VISIBLE);
                    binding.lottieLoadingCircle.playAnimation();
                    break;
                case SUCCESS:
                    binding.lottieLoadingCircle.cancelAnimation();
                    binding.lottieLoadingCircle.setVisibility(View.GONE);
                    binding.lottieConfirm.setVisibility(View.VISIBLE);
                    binding.lottieConfirm.playAnimation();
                   
                    Toast.makeText(this, result.message != null ? result.message : "Sucess", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Sprawdź email aby zweryfikować konto", Toast.LENGTH_SHORT).show();
                    binding.lottieConfirm.postDelayed(this::finish, 2000);
                    
                    break;
                case ERROR:
                    binding.lottieLoadingCircle.cancelAnimation();
                    binding.lottieLoadingCircle.setVisibility(View.GONE);
                    binding.lottieError.setVisibility(View.VISIBLE);
                    binding.lottieError.playAnimation();
                    binding.btnRegister.setEnabled(true);
                    Toast.makeText(this, result.message != null ? result.message : "Błąd rejestracji", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void setupUserNameValidation(){
        binding.userNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                userNameValidationHandler.removeCallbacks(userNameValidationRunnable);

                String userName = s.toString().trim();
                if (userName.isEmpty()) {
                    binding.userNameLayout.setError(null);
                    return;
                }
                if(userName.length() < 3){
                    binding.userNameLayout.setError("Nazwa musi mieć min. 3 znaki");
                    return;
                }else if(userName.length() > 20){
                    binding.userNameLayout.setError("Nazwa może mieć maks. 20 znaków");
                    return;
                }
                userNameValidationRunnable = () ->{
                    viewModel.isUserNameTaken(userName);
                };
                userNameValidationHandler.postDelayed(userNameValidationRunnable, 1000);
            }
        });
    }

    private void setupEmailValidation(){
        binding.emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                emailValidationHandler.removeCallbacks(emailValidationRunnable);
                String email = s.toString().trim();
                if (TextUtils.isEmpty(email)) {
                    binding.emailLayout.setError("Adres email jest wymagany");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.emailLayout.setError("Nieprawidłowy email");
                    return;
                }

                binding.emailLayout.setHelperText(null);
                emailValidationRunnable = () ->{
                    viewModel.isEmailTaken(email);
                };
                emailValidationHandler.postDelayed(emailValidationRunnable, 1000);
            }
        });
    }

    private void setupPasswordValidation(){
        binding.passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(passwordValidationRunnable != null){
                    passwordValidationHandler.removeCallbacks(passwordValidationRunnable);
                }
                String password = s.toString();
                passwordValidationRunnable = () ->{
                    if (TextUtils.isEmpty(password)) {
                        binding.passwordLayout.setError("Hasło jest wymagane");
                    } else if (password.length() < 6) {
                        binding.passwordLayout.setError("Hasło musi mieć min. 6 znaków");
                    } else if (!password.matches(".*[A-Z].*")) {
                        binding.passwordLayout.setError("Hasło musi zawierać wielką literę");
                    } else if (!password.matches(".*[0-9].*")) {
                        binding.passwordLayout.setError("Hasło musi zawierać cyfrę");
                    } else {
                        binding.passwordLayout.setError(null);
                    }
                };
                passwordValidationHandler.postDelayed(passwordValidationRunnable, 300);
            }
        });
    }
    //1 screen -email, date, gender
    private boolean validateFirstScreen(){
        boolean isValid = true;

        String email = getText(binding.emailEditText);
        String birthDate = getText(binding.birthDateEditText);

        if(TextUtils.isEmpty(email)){
            binding.emailLayout.setError("Email jest wymagany");
            isValid = false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailLayout.setError("Nieprawidłowy email");
            isValid = false;
        }else{
            binding.emailLayout.setError(null);
        }

        if(!isValidDate(birthDate)){
            binding.birthDateLayout.setError("Data musi być w formacie DD/MM/RRRR");
            isValid = false;
        }else{
            binding.birthDateLayout.setError(null);
        }

        if(binding.genderRadioGroup.getCheckedRadioButtonId() == -1){
            Toast.makeText(this, "Wybierz płeć", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }
    private boolean isValidDate(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try{
            sdf.parse(date.trim());
            return true;
        }catch (ParseException e){
            return false;
        }
    }

    //2 screen = username, password
    private boolean validateSecondScreen(){
        boolean isValid = true;

        String userName = getText(binding.userNameEditText);
        String password = getText(binding.passwordEditText);

        if(TextUtils.isEmpty(userName)){
            binding.userNameLayout.setError("Nazwa użytkownika jest wymagana");
            isValid = false;
        }else if(userName.length() < 3){
            binding.userNameLayout.setError("Nazwa musi się składać z min. 3 znaków");
            isValid = false;
        }else if(userName.length() > 20){
            binding.userNameLayout.setError("Nazwa może mieć maks. 20 znaków");
            isValid = false;
        }else{
            binding.userNameLayout.setError(null);
        }

        if(TextUtils.isEmpty(password)){
            binding.passwordLayout.setError("Hasło jest wymagane");
            isValid = false;
        }else if(password.length() < 6){
            binding.passwordLayout.setError("Hasło musi mieć min. 6 znaków");
            isValid = false;
        }else if(!password.matches(".*[A-Z].*")){
            binding.passwordLayout.setError("Hasło musi zawierać wielką literę");
            isValid = false;
        }else if(!password.matches(".*[0-9].*")){
            binding.passwordLayout.setError("Hasło musi zawierać cyfrę");
            isValid = false;
        }else{
            binding.passwordLayout.setError(null);
        }
        return isValid;
    }

    private void registerNewUser(){
        String email = getText(binding.emailEditText);
        String birthDate = getText(binding.birthDateEditText);
        String userName = getText(binding.userNameEditText);
        String password = getText(binding.passwordEditText);
        long genderId = getSelectedGenderId();

        binding.btnRegister.setEnabled(false);

        viewModel.registerNewUserLocally(email, userName, password, birthDate, genderId);
    }

    private long getSelectedGenderId(){
        int checkedId = binding.genderRadioGroup.getCheckedRadioButtonId();
        if(checkedId == binding.maleRadioButton.getId()){
            return 1L;
        }else if(checkedId == binding.femaleRadioButton.getId()){
            return 2L;
        }else{
            return 3L;
        }
    }

    private void observeEmailExistsResult(){
        viewModel.getEmailExistsResult().observe(this, result -> {
            if(result == null) return;

            switch (result.status){
                case CHECKING:
                    break;
                case EXISTS:
                    binding.emailLayout.setHelperText("Email jest już zajęty");
                    break;
                case NON_EXISTS:
                    binding.emailLayout.setError(null);
                    binding.emailLayout.setHelperText("Email dostępny");
                    break;
                case ERROR:
                    binding.emailLayout.setError(null);
                    binding.emailLayout.setHelperText(null);
            }
        });
    }

    private void observeUsernameExistsResult(){
        viewModel.getUserNameExistsResult().observe(this, result -> {
            if(result == null) return;

            switch (result.status){
                case CHECKING:
                    break;
                case EXISTS:
                    binding.userNameLayout.setHelperText("Nazwa użytkownika zajęta");
                    break;
                case NON_EXISTS:
                    binding.userNameLayout.setError(null);
                    binding.userNameLayout.setHelperText("Nazwa dostępna");
                    break;
                case ERROR:
                    binding.userNameLayout.setError(null);
                    binding.userNameLayout.setHelperText(null);
            }
        });
    }

    private String getText(com.google.android.material.textfield.TextInputEditText editText){
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        userNameValidationHandler.removeCallbacksAndMessages(null);
        emailValidationHandler.removeCallbacksAndMessages(null);
        passwordValidationHandler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
