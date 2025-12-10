package pl.pollub.android.sprinteyeapp.view;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import pl.pollub.android.sprinteyeapp.R;
import pl.pollub.android.sprinteyeapp.databinding.ActivitySecondPhoneBinding;

public class SecondPhoneSessionActivity extends AppCompatActivity {
    private ActivitySecondPhoneBinding binding;
    private final String[] startType = {"Niski-Na miejsca, gotów...start", "Wysoki-Na miejsca...Start"};

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        binding = ActivitySecondPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //adapter
        ArrayAdapter<String> adapterItems = new ArrayAdapter<>(this, R.layout.dropdown_item, startType);

        binding.autoCompleteTextView.setAdapter(adapterItems);

        binding.autoCompleteTextView.setOnItemClickListener(((parent, view, position, id) -> {
            String item = parent.getItemAtPosition(position).toString();

            //todo obsluga
        }));
        binding.startButton.setOnClickListener(v -> {
            lottieCircleAnimation();
        });

        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void lottieCircleAnimation(){
            binding.stopperCircle.setRepeatCount(ValueAnimator.INFINITE);
            binding.stopperCircle.playAnimation();
    }
}
