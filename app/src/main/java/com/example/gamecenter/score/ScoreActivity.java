package com.example.gamecenter.score;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamecenter.GameSelectorActivity;
import com.example.gamecenter.R;
import com.example.gamecenter.database.GameCenterOpenHelper;
import com.example.gamecenter.score.GameScore;
import com.example.gamecenter.score.ScoreAdapter;

import java.util.ArrayList;
import java.util.Collections;

public class ScoreActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ArrayList<GameScore> mGameData;
    private ScoreAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Screen config
        setContentView(R.layout.app_activity_scores);

        // Hide system bars (Status Bar & Navigation Bar)
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        controller.hide(WindowInsetsCompat.Type.systemBars());

        int gridColumnCount = getResources().getInteger(R.integer.grid_column_count);

        // Initialize the RecyclerView.
        mRecyclerView = findViewById(R.id.recyclerView);

        // Set the Layout Manager.
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridColumnCount));

        // Initialize the ArrayList that will contain the data.
        mGameData = new ArrayList<>();

        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new ScoreAdapter(this, mGameData);
        mRecyclerView.setAdapter(mAdapter);

        int swipeDirs;
        if(gridColumnCount > 1){
            swipeDirs = 0;
        } else {
            swipeDirs = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        }
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback
                (ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN
                        | ItemTouchHelper.UP, swipeDirs) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                Collections.swap(mGameData, from, to);
                mAdapter.notifyItemMoved(from, to);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                mGameData.remove(viewHolder.getAdapterPosition());
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });

        helper.attachToRecyclerView(mRecyclerView);

        // Get the data.
        initializeData();
    }

    /**
     * Initialize the sports data from resources.
     */
    private void initializeData() {
        mGameData.clear();

        GameCenterOpenHelper db = new GameCenterOpenHelper(this);
        int limitPerGame = 20;

        addScoresFromCursorWithUsername(
                db.queryTopScoresWithUsernameByGame("_2048", limitPerGame),
                R.drawable.app_2048banner
        );

        addScoresFromCursorWithUsername(
                db.queryTopScoresWithUsernameByGame("JUICE_DUNGEON_2", limitPerGame),
                R.drawable.jd2_presentation_scene
        );

        mAdapter.notifyDataSetChanged();
    }

    private void addScoresFromCursorWithUsername(android.database.Cursor cursor, int imageRes) {
        if (cursor == null) return;

        try {
            int colUsername = cursor.getColumnIndexOrThrow("username");
            int colDatetime = cursor.getColumnIndexOrThrow("datetime");
            int colScore = cursor.getColumnIndexOrThrow("score");
            int colGame = cursor.getColumnIndexOrThrow("game");

            while (cursor.moveToNext()) {
                String username = cursor.getString(colUsername);
                String datetime = cursor.getString(colDatetime);
                int score = cursor.getInt(colScore);
                String game = cursor.getString(colGame);

                mGameData.add(new GameScore(username, datetime, imageRes, score, game));
            }
        } finally {
            cursor.close();
        }
    }

    public void goBackHome(View view) {
        Intent intent = new Intent(this, GameSelectorActivity.class);
        startActivity(intent);
        finish();
    }
}
