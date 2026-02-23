package com.example.gamecenter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.gamecenter._2048._2048Activity;
import com.example.gamecenter.juice_dungeon_2.LoginActivity;

public class GameSelectorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Asign the screen design
        setContentView(R.layout.app_activity_gameselector);
        // Hide system action bar
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Make it so the system action bar appears if the user slides
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide everything (Status Bar & Navigation Bar)
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }

    public void start2048Game(View view) {
        Intent intent = new Intent(this, _2048Activity.class);
        startActivity(intent);
        finish();
    }

    public void startJuiceDungeon2Game(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
