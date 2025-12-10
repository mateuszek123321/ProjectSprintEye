package pl.pollub.android.sprinteyeapp.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import pl.pollub.android.sprinteyeapp.databinding.CameraBasicSessionBinding;

public class BasicSessionCamera extends AppCompatActivity {

    private CameraBasicSessionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = CameraBasicSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void stoper(){

    }
}
