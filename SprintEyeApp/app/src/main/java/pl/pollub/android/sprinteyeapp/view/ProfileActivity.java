package pl.pollub.android.sprinteyeapp.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import pl.pollub.android.sprinteyeapp.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(x -> {
            finish();
        });

    }
}
