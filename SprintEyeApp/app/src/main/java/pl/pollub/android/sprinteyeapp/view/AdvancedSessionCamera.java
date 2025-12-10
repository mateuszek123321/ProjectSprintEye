package pl.pollub.android.sprinteyeapp.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import pl.pollub.android.sprinteyeapp.databinding.CameraAdvancedSessionBinding;

public class AdvancedSessionCamera extends AppCompatActivity {
    private CameraAdvancedSessionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = CameraAdvancedSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void stoper(){

    }
}
