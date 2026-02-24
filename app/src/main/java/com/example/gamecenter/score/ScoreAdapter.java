/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.gamecenter.score;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.gamecenter.R;

import java.util.ArrayList;

/***
 * The adapter class for the RecyclerView, contains the gameScore data.
 */
public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ViewHolder>  {

    // Member variables.
    private ArrayList<GameScore> mGameScoreData;
    private Context mContext;

    /**
     * Constructor that passes in the sports data and the context.
     *
     * @param gameScoreData ArrayList containing the sports data.
     * @param context Context of the application.
     */
    public ScoreAdapter(Context context, ArrayList<GameScore> gameScoreData) {
        this.mGameScoreData = gameScoreData;
        this.mContext = context;
    }

    /**
     * Required method for creating the viewholder objects.
     *
     * @param parent The ViewGroup into which the new View will be added
     *               after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return The newly created ViewHolder.
     */
    @Override
    public ScoreAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).
                inflate(R.layout.app_scorelistitem, parent, false));
    }

    /**
     * Required method that binds the data to the viewholder.
     *
     * @param holder The viewholder into which the data should be put.
     * @param position The adapter position.
     */
    @Override
    public void onBindViewHolder(ScoreAdapter.ViewHolder holder,
                                 int position) {
        // Get current sport.
        GameScore currentSport = mGameScoreData.get(position);

        // Populate the textviews with data.
        holder.bindTo(currentSport);

        Glide.with(mContext).load(currentSport.getImageResource()).into(holder.mGameImage);
    }

    /**
     * Required method for determining the size of the data set.
     *
     * @return Size of the data set.
     */
    @Override
    public int getItemCount() {
        return mGameScoreData.size();
    }


    /**
     * ViewHolder class that represents each row of data in the RecyclerView.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Member Variables for the TextViews
        private TextView mUsernameText;
        private TextView mScoreText;
        private TextView mDateTimeText;
        private ImageView mGameImage;

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         *
         * @param itemView The rootview of the list_item.xml layout file.
         */
        public ViewHolder(View itemView) {
            super(itemView);

            // Initialize the views.
            mUsernameText = itemView.findViewById(R.id.username);
            mScoreText = itemView.findViewById(R.id.score);
            mDateTimeText = itemView.findViewById(R.id.dateTime);
            mGameImage = (ImageView) itemView.findViewById(R.id.sportsImage);
            itemView.setOnClickListener(this);
        }

        void bindTo(GameScore currentScore){
            // Populate the textviews with data.
            mUsernameText.setText(currentScore.getUsername());
            mScoreText.setText(currentScore.getScore());

        }

        @Override
        public void onClick(View v) {
            /*
            GameScore currentScore = mGameScoreData.get(getAdapterPosition());

            Intent detailIntent = new Intent(mContext, DetailActivity.class);
            detailIntent.putExtra("title", currentScore.getTitle());
            detailIntent.putExtra("image_resource", currentScore.getImageResource());

            mContext.startActivity(detailIntent);

             */
        }

    }
}
