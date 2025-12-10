package pl.pollub.android.sprinteyeapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import pl.pollub.android.sprinteyeapp.MainMenuActivity;
import pl.pollub.android.sprinteyeapp.databinding.ActivityBasicSessionBinding;
import pl.pollub.android.sprinteyeapp.mediapipe.TurnOnCameraActivity;

public class BasicSessionActivity extends AppCompatActivity {
    private ActivityBasicSessionBinding binding;
    private int cycles = 0;
    private int detections = 0;
    private int seriesTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        binding = ActivityBasicSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        seriesMode();
        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void startBasicSession(){
        binding.startBasicSession.setOnClickListener(v -> {
            Intent intent = new Intent(BasicSessionActivity.this, TurnOnCameraActivity.class);
            startActivity(intent);
        });
    }

    private void seriesMode(){
        binding.seriesOptions.setVisibility(ViewFlipper.GONE);
        updateCyclesText();
        updateDetections();
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
        binding.btnDetectionIncrease.setOnClickListener(v -> {
            detections++;
            updateDetections();
        });
        binding.btnSeriesTimeIncrease.setOnClickListener(v -> {
            seriesTime++;
            updateSeriesTime();
        });

        //-
        binding.btnCyclesDecrease.setOnClickListener(v -> {
            if(cycles > 0){
                cycles--;
                //todo toast
            }
            updateCyclesText();
        });

        binding.btnDetectionDecrease.setOnClickListener(v -> {
            if(detections > 0){
                detections--;
                //todo toast
            }
            updateDetections();
        });

        binding.btnSeriesTimeDecrease.setOnClickListener(v -> {
            if(seriesTime > 0) {
                seriesTime--;
                //todo toast
            }
            updateSeriesTime();
        });
    }

    private void updateCyclesText() {
        binding.tvCyclesValue.setText(String.valueOf(cycles));
    }
    private void updateDetections() {
        binding.tvDetectionValue.setText(String.valueOf(detections));
    }
    private void updateSeriesTime() {
        binding.tvSeriesTime.setText(String.valueOf(seriesTime));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
