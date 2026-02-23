package com.example.gamecenter._2048;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamecenter.R;

public class _2048Activity extends AppCompatActivity implements GameListener, Runnable {

    private GameGrid gameGrid;
    private TextView scoreTv, bestScoreTv;
    private ImageButton stepBackBtn;
    private ImageButton restartBtn;
    private int currentScore = 0;
    private int previousStepScore = 0;
    private int remainingStepBacks = 3;
    private int bestScore = 0;
    private SharedPreferences prefs;
    private GameMode gameMode;
    private Thread thread;
    private boolean isGameRunning = true;
    private TextView clock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout._2048_activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scoreTv = findViewById(R.id.score_tv);
        bestScoreTv = findViewById(R.id.best_score_tv);
        gameGrid = findViewById(R.id.game_grid);
        stepBackBtn = findViewById(R.id.step_back_btn);
        restartBtn = findViewById(R.id.restart_btn);
        clock = findViewById(R.id.clock);

        prefs = getSharedPreferences("2048_DATA", Context.MODE_PRIVATE);
        bestScore = prefs.getInt("BEST_SCORE", 0);
        updateScoreUI();

        if (gameGrid != null) {
            gameGrid.setGameListener(this);
        }

        if (restartBtn != null) {
            restartBtn.setOnClickListener(v -> {
                if (gameGrid != null) {
                    onGameOver();
                }
            });
        }

        if (stepBackBtn != null) {
            stepBackBtn.setOnClickListener(v -> {
                if (remainingStepBacks > 0) {
                    onRollBackScore();

                    Toast.makeText(this, "Turno revertido. Quedan " + remainingStepBacks + " retrocesos.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No te quedan retrocesos.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        showModeSelectionDialog();
    }

    private void showModeSelectionDialog() {
        String[] modes = {"Classic", "Countdown"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a game mode");
        builder.setCancelable(false);

        builder.setItems(modes, (dialog, which) -> {
            this.gameMode = (which == 0) ? GameMode.CLASSIC : GameMode.COUNTDOWN;

            isGameRunning = true;
            this.thread = new Thread(this);
            this.thread.start();

            dialog.dismiss();
        });
        builder.show();
    }

    public void run() {
        if (gameMode == null) return;

        switch (gameMode) {
            case CLASSIC: runClassicMode(); break;
            case COUNTDOWN: runCountdownMode(); break;
        }

        if (isGameRunning) {
            runOnUiThread(this::onGameOver);
        }
    }

    private void runClassicMode() {
        int totalSeconds = 0;
        while (isGameRunning) {
            final int h = totalSeconds / 3600;
            final int m = (totalSeconds % 3600) / 60;
            final int s = totalSeconds % 60;

            runOnUiThread(() -> clock.setText(String.format("%d:%02d:%02d", h, m, s)));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
            totalSeconds++;
        }
    }
    private void runCountdownMode() {
        int totalSeconds = 10;

        while (totalSeconds >= 0 && isGameRunning) {
            final int h = totalSeconds / 3600;
            final int m = (totalSeconds % 3600) / 60;
            final int s = totalSeconds % 60;

            runOnUiThread(() -> clock.setText(String.format("%d:%02d:%02d", h, m, s)));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
            totalSeconds--;
        }

        if (totalSeconds < 0 && isGameRunning) {
            runOnUiThread(this::onGameOver);
        }
    }

    public void resetGame() {
        gameGrid.resetGame();
        currentScore = 0;
        scoreTv.setText("0");
        previousStepScore = 0;
        remainingStepBacks = 3;
        showModeSelectionDialog();
    }

    @Override
    public void updatePreviousStepScore() {
        previousStepScore = currentScore;
    }

    @Override
    public void onRollBackScore() {
        gameGrid.rollbackNumberLayer();
        currentScore = previousStepScore;
        remainingStepBacks--;
        updateScoreUI();
    }


    @Override
    public void onScoreChanged(int scoreIncrease) {
        previousStepScore = currentScore;

        currentScore += scoreIncrease;

        if (currentScore > bestScore) {
            bestScore = currentScore;
            prefs.edit().putInt("BEST_SCORE", bestScore).apply();
        }

        updateScoreUI();
    }

    @Override
    public void onGameOver() {
        if (!isGameRunning) return;

        isGameRunning = false;
        this.thread = null;

        runOnUiThread(() -> {
            showGameOverDialog();
        });
    }

    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Game Over!");
        builder.setMessage("Final score: " + this.currentScore + "\nTry again?");

        builder.setCancelable(false);

        builder.setPositiveButton("Retry", (dialog, which) -> {
            resetGame();
            dialog.dismiss();
        });

        builder.setNegativeButton("Exit", (dialog, which) -> {
            finish();
        });

        builder.show();
    }

    private void updateScoreUI() {
        if (scoreTv != null) scoreTv.setText(String.valueOf(currentScore));
        if (bestScoreTv != null) bestScoreTv.setText(String.valueOf(bestScore));
    }
}