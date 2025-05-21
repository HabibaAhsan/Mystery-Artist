package com.example.mysteryartist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;
import java.util.ArrayList;

public class LobbyActivity extends AppCompatActivity {
    private DatabaseReference gameRef;
    private String gameCode, playerId;
    private boolean isHost;
    private ArrayList<Player> players;
    private PlayerAdapter playerAdapter;
    private Button startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        TextView gameCodeText = findViewById(R.id.gameCodeText);
        RecyclerView playerRecyclerView = findViewById(R.id.playerRecyclerView);
        startGameButton = findViewById(R.id.startGameButton);

        Intent intent = getIntent();
        gameCode = intent.getStringExtra("gameCode");
        playerId = intent.getStringExtra("playerId");
        isHost = intent.getBooleanExtra("isHost", false);

        gameRef = FirebaseDatabase.getInstance().getReference("games").child(gameCode);
        players = new ArrayList<>();
        playerAdapter = new PlayerAdapter(this, players);

        playerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playerRecyclerView.setAdapter(playerAdapter);
        gameCodeText.setText("" + gameCode);

        // Show "Start Game" button only for the host
        if (isHost) {
            startGameButton.setOnClickListener(v -> {
                // Set game state to STARTED and transition to GameActivity
                gameRef.child("gameState").setValue(GameState.STARTED.name());

            });
        } else {
            startGameButton.setVisibility(Button.GONE);
        }

        // Listen for players joining
        gameRef.child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                players.clear();
                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    Player player = playerSnapshot.getValue(Player.class);
                    if (player != null) {
                        players.add(player);
                    }
                }
                playerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LobbyActivity.this, "Error loading players", Toast.LENGTH_SHORT).show();
            }
        });

        // Monitor game state and transition players to GameActivity when the game starts
        gameRef.child("gameState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String gameState = snapshot.getValue(String.class);
                if (GameState.STARTED.name().equals(gameState)) {
                    Intent gameIntent = new Intent(LobbyActivity.this, GameActivity.class);
                    gameIntent.putExtra("gameCode", gameCode);
                    gameIntent.putExtra("playerId", playerId); // Pass the player's unique ID
                    startActivity(gameIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LobbyActivity.this, "Error checking game state", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
