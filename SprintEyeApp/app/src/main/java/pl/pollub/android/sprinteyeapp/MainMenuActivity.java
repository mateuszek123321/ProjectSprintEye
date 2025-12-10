package pl.pollub.android.sprinteyeapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import pl.pollub.android.sprinteyeapp.databinding.ActivityMainBinding;
import pl.pollub.android.sprinteyeapp.view.AccountSettingsActivity;
import pl.pollub.android.sprinteyeapp.view.AdvancedSessionActivity;
import pl.pollub.android.sprinteyeapp.view.BasicSessionActivity;
import pl.pollub.android.sprinteyeapp.view.JoinSessionActivity;
import pl.pollub.android.sprinteyeapp.view.ManageAthletesActivity;
import pl.pollub.android.sprinteyeapp.view.RunResultsActivity;

public class MainMenuActivity extends AppCompatActivity {
    private long userId = -1;
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getLongExtra("USER_ID", -1);
        String userName = getIntent().getStringExtra("USER_NAME");

        if(userName != null && !userName.isEmpty()){
            binding.loggedUser.setText(userName);
        }else{
            binding.loggedUser.setText("Niezalogowany");
        }
        navigation();
    }

    private void navigation(){
        binding.joinSessionButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, JoinSessionActivity.class);
            startActivity(intent);
        });

        binding.basicSessionButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, BasicSessionActivity.class);
            startActivity(intent);
        });

        // Nawigacja do "Sesja zaawansowana"
        binding.advancedSessionButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, AdvancedSessionActivity.class);
            startActivity(intent);
        });

        // Nawigacja do "Wyniki"
        binding.runHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, RunResultsActivity.class);
            startActivity(intent);
        });

        // Nawigacja do "Sportowcy"
        binding.athleteManagerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ManageAthletesActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        binding.accountOptionsButton.setOnClickListener(v ->{
            Intent intent = new Intent(MainMenuActivity.this, AccountSettingsActivity.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}