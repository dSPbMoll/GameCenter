package com.example.gamecenter;

import static java.lang.Thread.sleep;

import android.content.res.TypedArray;
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
        // Get the resources from the XML file.
        String[] sportsList = getResources()
                .getStringArray(R.array.sports_titles);;

        // Clear the existing data (to avoid duplication).
        mGameData.clear();

        TypedArray sportsImageResources =
                getResources().obtainTypedArray(R.array.game_images);

        // Create the ArrayList of Sports objects with titles and
        // information about each sport.
        for(int i=0;i<sportsList.length;i++){
            mGameData.add(new GameScore( ));
        }

        sportsImageResources.recycle();

        // Notify the adapter of the change.
        mAdapter.notifyDataSetChanged();
    }

    public void resetSports(View view) {
        initializeData();
    }
}
