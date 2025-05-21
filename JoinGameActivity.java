package com.example.mysteryartist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.UUID;

public class JoinGameActivity extends AppCompatActivity {
    private DatabaseReference gamesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        EditText gameCodeInput = findViewById(R.id.gameCodeInput);
        EditText playerNameInput = findViewById(R.id.playerNameInput);
        Button joinGameBtn = findViewById(R.id.joinGameBtn);

        // Reference to the "games" node in Firebase
        gamesRef = FirebaseDatabase.getInstance().getReference("games");

        joinGameBtn.setOnClickListener(v -> {
            String gameCode = gameCodeInput.getText().toString().trim();
            String playerName = playerNameInput.getText().toString().trim();

            if (gameCode.isEmpty() || playerName.isEmpty()) {
                Toast.makeText(this, "Please enter both game code and name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the game exists in Firebase
            gamesRef.child(gameCode).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DataSnapshot gameSnapshot = task.getResult();

                    // Generate a unique ID for the new player
                    String playerId = UUID.randomUUID().toString();

                    // Add the player to the game
                    gamesRef.child(gameCode).child("players").child(playerId)
                            .setValue(new Player(playerId, playerName, PlayerRole.PLAYER))
                            .addOnCompleteListener(addTask -> {
                                if (addTask.isSuccessful()) {

                                    // Navigate to LobbyActivity
                                    Intent intent = new Intent(JoinGameActivity.this, LobbyActivity.class);
                                    intent.putExtra("gameCode", gameCode);
                                    intent.putExtra("playerId", playerId);
                                    intent.putExtra("isHost", false);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(this, "Failed to join game. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "Game not found. Please check the game code.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
