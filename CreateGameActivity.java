package com.example.mysteryartist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.UUID;

public class CreateGameActivity extends AppCompatActivity {
    private DatabaseReference gamesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        EditText playerNameInput = findViewById(R.id.playerNameInput);
        Button createGameBtn = findViewById(R.id.createGameBtn);

        // Reference to the "games" node in Firebase
        gamesRef = FirebaseDatabase.getInstance().getReference("games");

        createGameBtn.setOnClickListener(v -> {
            String playerName = playerNameInput.getText().toString().trim();
            if (playerName.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate a unique game code
            String gameCode = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

            // Generate a unique host ID
            String hostId = UUID.randomUUID().toString();

            // Create a new Game object
            Game newGame = new Game(gameCode, hostId, playerName);

            // Save the game object to Firebase
            gamesRef.child(gameCode).setValue(newGame).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Navigate to LobbyActivity on success
                    Intent intent = new Intent(CreateGameActivity.this, LobbyActivity.class);
                    intent.putExtra("gameCode", gameCode);
                    intent.putExtra("playerId", hostId); // Pass the host ID
                    intent.putExtra("isHost", true);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to create game. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
