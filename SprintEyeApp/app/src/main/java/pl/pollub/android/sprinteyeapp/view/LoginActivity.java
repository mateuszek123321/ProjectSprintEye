package pl.pollub.android.sprinteyeapp.view;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import pl.pollub.android.sprinteyeapp.MainMenuActivity;
import pl.pollub.android.sprinteyeapp.databinding.ActivityLoginBinding;
import pl.pollub.android.sprinteyeapp.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private final Handler loginIdHandler = new Handler(Looper.getMainLooper());
    private Runnable loginIdRunnable;
    private final Handler passwordHandler = new Handler(Looper.getMainLooper());
    private Runnable passwordRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupToolbarButtons();
        setupLoginButton();
        setupLoginValidation();
        setupPasswordValidation();
        observeLoginResult();
        observeLoginExistsResult();
    }

    private void setupToolbarButtons(){
        binding.backButton.setOnClickListener(v -> {
            finish();
        });
        binding.homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });
        binding.accountOptionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, AccountSettingsActivity.class);
            startActivity(intent);
        });
    }
    private void setupLoginButton(){
        binding.btnLogin.setOnClickListener(v -> {
            if(validateInput()){
                logInUser();
            }
        });
    }

    private void observeLoginResult(){
        viewModel.getLoginResult().observe(this, result -> {
            if(result == null) return;

            switch (result.status){
                case LOADING:
                    binding.btnLogin.setEnabled(false);
                    binding.lottieLoadingCircle.setVisibility(View.VISIBLE);
                    binding.lottieLoadingCircle.playAnimation();
                    break;
                case SUCCESS:
                    binding.lottieLoadingCircle.cancelAnimation();
                    binding.lottieLoadingCircle.setVisibility(View.GONE);
                    binding.lottieConfirm.setVisibility(View.VISIBLE);
                    binding.lottieConfirm.playAnimation();

                    Toast.makeText(this, result.message != null ? result.message : "Zalogowano", Toast.LENGTH_SHORT).show();
                    if (result.localUserId != null) {
                        Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                        intent.putExtra("USER_ID", result.localUserId);
                        startActivity(intent);
                        finish();
                    } else {
                        binding.lottieConfirm.postDelayed(this::finish, 2000);
                    }
                    break;
                case ERROR:
                    binding.lottieLoadingCircle.cancelAnimation();
                    binding.lottieLoadingCircle.setVisibility(View.GONE);
                    binding.lottieError.setVisibility(View.VISIBLE);
                    binding.lottieError.playAnimation();
                    binding.btnLogin.setEnabled(true);
                    Toast.makeText(this, result.message != null ? result.message : "Błąd logowania", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
    private void setupLoginValidation(){
        binding.emailUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(loginIdRunnable != null){
                    loginIdHandler.removeCallbacks(loginIdRunnable);
                }

                String login = s.toString().trim();
                loginIdRunnable = () -> {
                    if(TextUtils.isEmpty(login)){
                        binding.emailUsernameLayout.setError("Wprowadź email lub nazwę użytkownika");
                    }else{
                        binding.emailUsernameLayout.setError(null);
                        viewModel.checkLoginExists(login);
                    }
                };
                loginIdHandler.postDelayed(loginIdRunnable, 1000);
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
                if(passwordRunnable != null){
                    passwordHandler.removeCallbacks(passwordRunnable);
                }
                String password = s.toString();

                passwordRunnable = () ->{
                    if(TextUtils.isEmpty(password)){
                        binding.passwordLayout.setError("Hasło jest wymagane");
                    }else if(password.length() < 6){
                        binding.passwordLayout.setError("Hasło musi mieć min. 6 znaków");
                    }else{
                        binding.passwordLayout.setError(null);
                    }
                };
                passwordHandler.postDelayed(passwordRunnable, 300);
            }
        });
    }

    private boolean validateInput(){
        boolean isValid = true;

        String login = getText(binding.emailUsernameEditText);
        String password = getText(binding.passwordEditText);

        if (TextUtils.isEmpty(login)) {
            binding.emailUsernameLayout.setError("Wprowadź email lub nazwę użytkownika");
            isValid = false;
        } else {
            binding.emailUsernameLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError("Hasło jest wymagane");
            isValid = false;
        } else if (password.length() < 6) {
            binding.passwordLayout.setError("Hasło musi mieć min. 6 znaków");
            isValid = false;
        } else {
            binding.passwordLayout.setError(null);
        }

        return isValid;
    }

    private void logInUser(){
        String login = getText(binding.emailUsernameEditText);
        String password = getText(binding.passwordEditText);

        binding.btnLogin.setEnabled(false);

        viewModel.loginLocally(login, password);
    }

    private String getText(com.google.android.material.textfield.TextInputEditText editText){
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
    private void observeLoginExistsResult(){
        viewModel.getLoginResultExists().observe(this, result -> {
            if(result == null) return;

            switch (result.status){
                case CHECKING:
                    break;
                case EXISTS:
                    binding.emailUsernameLayout.setHelperText("Login poprawny");
                    binding.emailUsernameLayout.setError(null);
                    break;
                case ERROR:
                    binding.emailUsernameLayout.setHelperText(null);
                    binding.emailUsernameLayout.setError(null);
                    break;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginIdHandler.removeCallbacksAndMessages(null);
        passwordHandler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
