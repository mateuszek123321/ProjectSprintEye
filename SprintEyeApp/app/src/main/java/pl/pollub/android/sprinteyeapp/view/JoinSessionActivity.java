package pl.pollub.android.sprinteyeapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import pl.pollub.android.sprinteyeapp.databinding.ActivityJoinSessionBinding;

public class JoinSessionActivity extends AppCompatActivity {
    private ActivityJoinSessionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        boolean connected = isConnected();

        super.onCreate(savedInstanceState);

        binding = ActivityJoinSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(x -> {
            finish();
        });

        binding.btnConnectToSession.setOnClickListener(v -> {
            //one click only
            binding.btnConnectToSession.setEnabled(false);
            lottieLoading();
            new Handler(Looper.getMainLooper()).postDelayed(()->{
                joinSession();
            }, 1000);
        });
    }

    private void joinSession(){
        boolean connected = isConnected();

        if(connected) {
            lottieConfirm();
        }else{
            lottieError();
        }
    }
    private Boolean isConnected(){

        //todo logika połączenia
        return true;
    }

    private void lottieConfirm(){
        binding.lottieLoadingCircle.setVisibility(View.GONE);
        binding.lottieConfirm.setVisibility(View.VISIBLE);
        binding.lottieConfirm.setRepeatCount(0);
        binding.lottieConfirm.playAnimation();

        binding.lottieConfirm.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                Intent intent = new Intent(JoinSessionActivity.this, SecondPhoneSessionActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void lottieError(){
        binding.lottieLoadingCircle.setVisibility(View.GONE);
        binding.lottieError.setVisibility(View.VISIBLE);
        binding.lottieError.setRepeatCount(0);
        binding.lottieError.playAnimation();

        binding.lottieError.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                binding.lottieError.setVisibility(View.GONE);
                binding.btnConnectToSession.setEnabled(true);
            }
        });
    }

    private void lottieLoading(){
        binding.lottieLoadingCircle.setVisibility(View.VISIBLE);
        binding.lottieLoadingCircle.setRepeatCount(ValueAnimator.INFINITE);
        binding.lottieLoadingCircle.playAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (binding != null) {
            binding.lottieLoadingCircle.cancelAnimation();
            binding.lottieError.cancelAnimation();
        }
        binding = null;
    }
}
