package pl.pollub.android.sprinteyeapp.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import pl.pollub.android.sprinteyeapp.databinding.ActivityRunResultsBinding;

public class RunResultsActivity extends AppCompatActivity {

    private ActivityRunResultsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        binding = ActivityRunResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
