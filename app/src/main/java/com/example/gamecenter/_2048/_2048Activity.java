package com.example.gamecenter._2048;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamecenter.R;

public class _2048Activity extends AppCompatActivity implements GameListener {

    private GameGrid gameGrid;
    private TextView scoreTv, bestScoreTv;
    private ImageButton stepBackBtn;
    private ImageButton restartBtn;
    private int currentScore = 0;
    private int previousStepScore = 0;
    private int remainingStepBacks = 3;
    private int bestScore = 0;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout._2048_activity_main);

        // Ajuste de márgenes del sistema (Barras de estado/navegación)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Inicializar Vistas (Con los IDs que pusimos en el XML)
        scoreTv = findViewById(R.id.score_tv);
        bestScoreTv = findViewById(R.id.best_score_tv);
        gameGrid = findViewById(R.id.game_grid);
        stepBackBtn = findViewById(R.id.step_back_btn);
        restartBtn = findViewById(R.id.restart_btn);

        // 2. Cargar Mejor Puntuación guardada
        prefs = getSharedPreferences("2048_DATA", Context.MODE_PRIVATE);
        bestScore = prefs.getInt("BEST_SCORE", 0);
        updateScoreUI();

        // 3. Conectar el Listener del Juego
        // Esto permite que GameGrid nos avise cuando hay puntos o Game Over
        if (gameGrid != null) {
            gameGrid.setGameListener(this);
        }

        // 4. Configurar Botones de Reinicio (Con protección anti-crash)
        if (restartBtn != null) {
            restartBtn.setOnClickListener(v -> {
                if (gameGrid != null) {
                    resetGame();
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

    }

    public void resetGame() {
        gameGrid.resetGame();
        currentScore = 0;
        scoreTv.setText("0");
        previousStepScore = 0;
        remainingStepBacks = 3;
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
        //Guardar puntuación actual como previa
        previousStepScore = currentScore;

        // Actualizar puntuación actual
        currentScore += scoreIncrease;

        // Comprobar si rompimos el récord
        if (currentScore > bestScore) {
            bestScore = currentScore;
            // Guardar en el teléfono permanentemente
            prefs.edit().putInt("BEST_SCORE", bestScore).apply();
        }

        updateScoreUI();
    }

    @Override
    public void onGameOver() {
        Toast.makeText(this, "¡Juego Terminado! Puntuación: " + currentScore, Toast.LENGTH_LONG).show();
    }

    // --- MÉTODOS AUXILIARES ---

    private void updateScoreUI() {
        if (scoreTv != null) scoreTv.setText(String.valueOf(currentScore));
        if (bestScoreTv != null) bestScoreTv.setText(String.valueOf(bestScore));
    }
}