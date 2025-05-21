package com.example.mysteryartist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardAdapter extends RecyclerView.Adapter<ScoreboardAdapter.ScoreViewHolder> {

    private List<Player> players;

    public ScoreboardAdapter(List<Player> players) {
        this.players = players;
    }

    public void updateScores(List<Player> updatedPlayers) {
        this.players = updatedPlayers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scoreboard_item, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        Player player = players.get(position);
        holder.playerNameText.setText(player.getPlayerName());
        holder.scoreText.setText(String.valueOf(player.getScore()));
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {

        TextView playerNameText, scoreText;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            playerNameText = itemView.findViewById(R.id.playerName);
            scoreText = itemView.findViewById(R.id.playerScore);
        }
    }
}
