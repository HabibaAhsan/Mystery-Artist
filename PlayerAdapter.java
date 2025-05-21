package com.example.mysteryartist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {
    private final List<Player> players; // Changed from String to Player
    private final Context context;

    public PlayerAdapter(Context context, List<Player> players) {
        this.context = context;
        this.players = players;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = players.get(position); // Access the Player object
        holder.playerNameTextView.setText(player.getPlayerName()); // Use the getName() method
        holder.playerRoleTextView.setText("" + player.getRole()); // Example for displaying role
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView playerNameTextView;
        TextView playerRoleTextView;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            playerNameTextView = itemView.findViewById(R.id.playerNameTextView);
            playerRoleTextView = itemView.findViewById(R.id.playerRoleTextView); // Add this ID to your layout
        }
    }
}
