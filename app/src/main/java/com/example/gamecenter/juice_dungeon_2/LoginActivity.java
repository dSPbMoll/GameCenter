package com.example.gamecenter.juice_dungeon_2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.gamecenter.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Asign the screen design
        setContentView(R.layout.jd2_activity_login);
        // Hide system action bar
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Make it so the system action bar appears if the used slides
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide everything (Status Bar & Navigation Bar)
        controller.hide(WindowInsetsCompat.Type.systemBars());

        // Listener assigning
        View backgroundLayout = findViewById(R.id.loginScreen);
        backgroundLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, TavernActivity.class);
        startActivity(intent);
        finish();
    }
}
