package com.example.gamecenter._2048;

public interface GameListener {
    void onScoreChanged(int scoreIncrease);
    void onGameOver();

    void updatePreviousStepScore();

    void onRollBackScore();
}