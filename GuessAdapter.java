package com.example.mysteryartist;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GuessAdapter extends RecyclerView.Adapter<GuessAdapter.GuessViewHolder> {
    private List<Guess> guessList;

    public GuessAdapter(List<Guess> guessList) {
        this.guessList = guessList;
    }

    @NonNull
    @Override
    public GuessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guess, parent, false);
        return new GuessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuessViewHolder holder, int position) {
        Guess guess = guessList.get(position);
        holder.guessTextView.setText(guess.getWord());

        holder.playerNameTextView.setText("By: " + guess.getPlayerName());
    }

    @Override
    public int getItemCount() {
        return guessList.size();
    }

    public static class GuessViewHolder extends RecyclerView.ViewHolder {
        TextView guessTextView, playerNameTextView;

        public GuessViewHolder(@NonNull View itemView) {
            super(itemView);
            guessTextView = itemView.findViewById(R.id.guessTextView);
            playerNameTextView = itemView.findViewById(R.id.playerNameTextView);
        }
    }
}
