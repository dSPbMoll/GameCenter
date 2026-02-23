package com.example.gamecenter;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Screen config
        setContentView(R.layout.app_activity_splash);

        // Hide system bars (Status Bar & Navigation Bar)
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        controller.hide(WindowInsetsCompat.Type.systemBars());

        // Load animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_splash);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_splash);

        // View reference
        TextView title = findViewById(R.id.splashText);
        ImageView logo = findViewById(R.id.ImageView2_Left);

        // Start animations
        title.startAnimation(fadeIn);
        logo.startAnimation(fadeIn);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                title.startAnimation(fadeOut);
                logo.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                try {
                    sleep(500);
                } catch(Exception ignored) {}

                ((LinearLayout) title.getParent()).removeAllViews();

                Intent intent = new Intent(SplashActivity.this, GameSelectorActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }
}