package pl.pollub.android.sprinteyeapp.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import pl.pollub.android.sprinteyeapp.databinding.ActivityAccountSettingsBinding;

public class AccountSettingsActivity extends AppCompatActivity {

    private ActivityAccountSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityAccountSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(x -> {
            finish();
        });
        navigation();
    }

    private void navigation(){
        binding.profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccountSettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        binding.registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccountSettingsActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccountSettingsActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
