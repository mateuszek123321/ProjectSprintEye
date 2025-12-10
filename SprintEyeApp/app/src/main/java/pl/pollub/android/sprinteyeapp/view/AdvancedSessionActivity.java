package pl.pollub.android.sprinteyeapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import pl.pollub.android.sprinteyeapp.databinding.ActivityAdvancedSessionBinding;
import pl.pollub.android.sprinteyeapp.databinding.ActivityJoinSessionBinding;
import pl.pollub.android.sprinteyeapp.mediapipe.TurnOnCameraActivity;

public class AdvancedSessionActivity extends AppCompatActivity {

    private ActivityAdvancedSessionBinding binding;
    private int cycles = 0;
    private int seriesTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        binding = ActivityAdvancedSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v -> {
            finish();
        });
        seriesMode();
    }

    private void startAdvancedSession(){
        binding.startAdvancedSession.setOnClickListener(v -> {
            Intent intent = new Intent(AdvancedSessionActivity.this, TurnOnCameraActivity.class);
            startActivity(intent);
        });
    }

    private void seriesMode(){
        binding.seriesOptions.setVisibility(ViewFlipper.GONE);
        updateCyclesText();
        updateSeriesTime();

        binding.switchSeries.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                binding.seriesOptions.setVisibility(View.VISIBLE);
            }else{
                binding.seriesOptions.setVisibility(View.GONE);
            }
        });

        //+
        binding.btnCyclesIncrease.setOnClickListener(v -> {
            cycles++;
            updateCyclesText();
        });
        binding.btnSeriesTimeIncrease.setOnClickListener(v -> {
            seriesTime++;
            updateSeriesTime();
        });

        //-
        binding.btnCyclesDecrease.setOnClickListener(v -> {
            if (cycles > 0){
                cycles--;
                //TODO toast zrobic
            }
            updateCyclesText();
        });
        binding.btnSeriesTimeDecrease.setOnClickListener(v -> {
            if (seriesTime > 0){
                seriesTime--;
                //TODO toast zrobic dla decrase
            }
            updateSeriesTime();
        });
    }

    private void updateCyclesText(){
        binding.tvCyclesValue.setText(String.valueOf(cycles));
    }
    private void updateSeriesTime(){
        binding.tvSeriesTime.setText(String.valueOf(seriesTime));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
