package com.example.mysteryartist;

import static android.os.SystemClock.sleep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private TextView statusText, drawerText;
    private Button skipWordButton, revealAnswerButton, leaveGameButton, endGameButton, scoreboardButton;

    private DrawingCanvas drawingCanvas;
    private Button colorBlack,colorRed,colorBlue,eraser,reset;

    private DatabaseReference gameRef, wordsRef, guessRef, drawingRef;
    private Game game;
    private String currentPlayerId;
    private boolean isDrawer;
    private boolean isHost;
    private boolean reveal;
    private EditText guessInput;
    private Button sendGuessButton;

    private RecyclerView guessesRecyclerView;
    private GuessAdapter guessAdapter;
    private List<Guess> guessList = new ArrayList<>();


    private List<String> wordPool = new ArrayList<>();
    private List<String> usedWords = new ArrayList<>();

    private CountDownTimer roundTimer;
    private TextView timerTextView; // For displaying the countdown

    private Handler handler = new Handler(); // For scheduling delayed actions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize UI elements
        statusText = findViewById(R.id.gameStatusText);
        drawerText = findViewById(R.id.drawerText);
        drawingCanvas = findViewById(R.id.drawingCanvas);
        skipWordButton = findViewById(R.id.skipWordButton);
        revealAnswerButton = findViewById(R.id.revealAnswerButton);
        leaveGameButton = findViewById(R.id.leaveGameButton);
        endGameButton = findViewById(R.id.endGameButton);
        scoreboardButton = findViewById(R.id.scoreboardButton);
        guessInput = findViewById(R.id.guessInput);
        sendGuessButton = findViewById(R.id.sendGuessButton);
        timerTextView = findViewById(R.id.timerTextView);

        colorBlack = findViewById(R.id.colorBlack);
        colorRed = findViewById(R.id.colorRed);
        colorBlue = findViewById(R.id.colorBlue);
        eraser = findViewById(R.id.eraser);
        reset = findViewById(R.id.reset);

        // Initialize the RecyclerView for guesses
        guessesRecyclerView = findViewById(R.id.guessesRecyclerView);
        guessAdapter = new GuessAdapter(guessList);
        guessesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        guessesRecyclerView.setAdapter(guessAdapter);

        // Get game details
        String gameCode = getIntent().getStringExtra("gameCode");
        currentPlayerId = getIntent().getStringExtra("playerId");

        if (gameCode == null || currentPlayerId == null) {
            Toast.makeText(this, "Error: Missing game or player information.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if required data is missing
            return;
        }
        // Firebase references
        if (gameCode != null) {
            gameRef = FirebaseDatabase.getInstance().getReference("games").child(gameCode);
        }
        wordsRef = FirebaseDatabase.getInstance().getReference("wordPool");
        guessRef = FirebaseDatabase.getInstance().getReference("games/" + gameCode + "/guesses");
        drawingCanvas.setFirebaseReference(gameCode);
        drawingRef=drawingCanvas.getFirebaseReference(gameCode);

        reveal=false;
        // Load word pool
        loadWordPool();

        // Sync game state
        syncGameState();

        // Sync drawing
        syncDrawing();

        // Button listeners
        setupButtonListeners();

        // Listen for guesses
        listenForGuesses();

        startRoundTimerAsHost();

        listenForRoundStartTime();

    }

    private void loadWordPool() {
        wordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot wordSnapshot : snapshot.getChildren()) {
                    wordPool.add(wordSnapshot.getValue(String.class));
                }
                Collections.shuffle(wordPool); // Shuffle words for randomness
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                statusText.setText("Error loading words.");
            }
        });
    }

    private void syncGameState() {
        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                game = snapshot.getValue(Game.class);

                if (game != null) {
                    String gameState = game.getGameState().name(); // Get the name of the enum value

                    if (GameState.FINISHED.name().equals(gameState)) {

                        String winner = calculateWinner(); // Calculate the winner
                        // Navigate to the Winner Screen for all players
                        Intent winnerIntent = new Intent(GameActivity.this, WinnerActivity.class);
                        winnerIntent.putExtra("winnerName", winner);
                        startActivity(winnerIntent);

                        gameRef.removeValue();
                        finish(); // Close the current activity

                    }
                    // Continue syncing game state for drawing and guessing
                    isDrawer = currentPlayerId.equals(game.getCurrentDrawerId());
                    isHost = currentPlayerId.equals(game.getHostId());
                    drawingCanvas.setIsDrawer(isDrawer);

                    // Update the UI for the player's role
                    updateUIForRole();

                    // Display current drawer
                    if (game.getCurrentDrawerId() != null) {
                        String drawerName = game.getPlayers().get(game.getCurrentDrawerId()).getPlayerName();
                        drawerText.setText(" " + drawerName);
                    }
                    if(reveal){
                        statusText.setText("The secret word was "+ game.getCurrentWord());
                    } else if (!isDrawer ) {
                        statusText.setText("Guess the word!");
                    }

                    // Display word for the drawer
                    if (isDrawer) {
                        if(game.getCurrentWord() == null) {
                            assignNextWord();
                            statusText.setText("Secret Word: " + game.getCurrentWord());
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                statusText.setText("Error syncing game state.");
            }
        });

        gameRef.child("revealedAnswer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String revealedAnswer = snapshot.getValue(String.class);
                    if (revealedAnswer != null) {
                        reveal = true;
                        passDrawerToNextPlayer();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });

    }

    private void syncDrawing() {
        drawingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                DrawingCanvas.Point point = dataSnapshot.getValue(DrawingCanvas.Point.class);
                if (point != null) {
                    drawingCanvas.updateCanvas(point); // Update the canvas with received drawing actions
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot,String previousChildName) {
                DrawingCanvas.Point point = dataSnapshot.getValue(DrawingCanvas.Point.class);
                if (point != null) {
                    drawingCanvas.updateCanvas(point);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                drawingCanvas.clear();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // Handle moved drawings if necessary
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
    private void updateUIForRole() {
        if (isDrawer) {
            colorBlack.setVisibility(View.VISIBLE);
            colorRed.setVisibility(View.VISIBLE);
            colorBlue.setVisibility(View.VISIBLE);
            eraser.setVisibility(View.VISIBLE);
            reset.setVisibility(View.VISIBLE);
            skipWordButton.setVisibility(View.VISIBLE);
            revealAnswerButton.setVisibility(View.VISIBLE);
            findViewById(R.id.guessInputContainer).setVisibility(View.GONE); // Hide guess input
        } else {
            colorBlack.setVisibility(View.GONE);
            colorRed.setVisibility(View.GONE);
            colorBlue.setVisibility(View.GONE);
            eraser.setVisibility(View.GONE);
            reset.setVisibility(View.GONE);
            skipWordButton.setVisibility(View.GONE);
            revealAnswerButton.setVisibility(View.GONE);
            findViewById(R.id.guessInputContainer).setVisibility(View.VISIBLE); // Show guess input
        }
        if(isHost){
            endGameButton.setVisibility((View.VISIBLE));
        } else {
            endGameButton.setVisibility((View.GONE));
        }
    }

    private void setupButtonListeners() {
        revealAnswerButton.setOnClickListener(v -> {
                gameRef.child("isWordGuessed").setValue(true);
                revealAnswer();


        });

        skipWordButton.setOnClickListener(v -> {
                assignNextWord();
        });

        colorBlack.setOnClickListener(v -> drawingCanvas.setPenColor(0xFF000000)); // Black
        colorRed.setOnClickListener(v -> drawingCanvas.setPenColor(0xFFFF0000)); // Red
        colorBlue.setOnClickListener(v -> drawingCanvas.setPenColor(0xFF0000FF)); // Blue
        eraser.setOnClickListener(v -> drawingCanvas.setEraserMode(true)); // Eraser
        reset.setOnClickListener(v -> drawingRef.removeValue()); // Eraser

        leaveGameButton.setOnClickListener(v -> {
            if(game.getPlayersCount()==1){
                endGame();
            }
            else {
                leaveGame();
            }
        });
        endGameButton.setOnClickListener(v -> endGame());

        sendGuessButton.setOnClickListener(v -> sendGuess());

        scoreboardButton.setOnClickListener(v -> showScoreboard());
    }


    private void assignNextWord() {
        if (wordPool.isEmpty()) {
            return;
        }
        String nextWord;
        int index = 0;

        // Find the first unused word in the word pool
        while (index < wordPool.size() && usedWords.contains(wordPool.get(index))) {
            index++;
        }

        // If all words are used, reset the usedWords list (optional, based on your game logic)
        if (index == wordPool.size()) {
            usedWords.clear();
            index = 0; // Start from the first word again
        }

        // Assign the next word
        nextWord = wordPool.get(index);
        usedWords.add(nextWord);
        gameRef.child("isWordGuessed").setValue(false);
        game.setCurrentWord(nextWord);
        gameRef.child("currentWord").setValue(nextWord);
        statusText.setText("Secret Word: " + game.getCurrentWord());
    }

    private void listenForGuesses() {
        guessRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Guess newGuess = snapshot.getValue(Guess.class);
                if (newGuess != null) {
                    guessList.add(newGuess);
                    guessAdapter.notifyItemInserted(guessList.size() - 1);
                    guessesRecyclerView.scrollToPosition(guessList.size() - 1); // Scroll to the latest guess

                    // Handle the new guess
                    handleGuess(newGuess.getWord(), newGuess.getPlayerId());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    private void handleGuess(String guess, String playerId) {
        if (game.isWordGuessed()) {
            return; // Ignore further guesses once the word is guessed
        }
        // Check if the guess matches the current word
        if (guess.equalsIgnoreCase(game.getCurrentWord())) {
            updateScore(playerId); // Update the score of the player who guessed correctly
            gameRef.child("isWordGuessed").setValue(true);
            passDrawerToNextPlayer();
        }
    }

    private void sendGuess() {
        String guessText = guessInput.getText().toString().trim();
        if (!guessText.isEmpty()) {
            DatabaseReference newGuessRef = guessRef.push();
            String name = game.getPlayers().get(currentPlayerId).getPlayerName();
            newGuessRef.setValue(new Guess(guessText, currentPlayerId,name));
            guessInput.setText(""); // Clear input field after sending
        }
    }

    private void updateScore(String playerId) {
        Log.d("DEBUG", "Updating score for player: " + playerId);
        Player player = game.getPlayers().get(playerId); // Ensure this retrieves the correct player
        if (player != null) {
            Log.d("DEBUG", "Current score before increment: " + player.getScore());
            player.incrementScore();
            Log.d("DEBUG", "New score after increment: " + player.getScore());

            // Update in Firebase
            gameRef.child("players").child(playerId).child("score").setValue(player.getScore())
                    .addOnSuccessListener(aVoid -> Log.d("DEBUG", "Score updated successfully"))
                    .addOnFailureListener(e -> Log.e("DEBUG", "Failed to update score", e));
        } else {
            Log.e("DEBUG", "Player not found for ID: " + playerId);
        }
    }


    private void revealAnswer() {
        if (game.getCurrentWord() != null) {
            reveal = true;
            // Update the database to notify all players
            gameRef.child("revealedAnswer").setValue(game.getCurrentWord());
            gameRef.child("isWordGuessed").setValue(true);
            statusText.setText("The secret word was "+ game.getCurrentWord());
        }
    }


    private void passDrawerToNextPlayer() {
        List<String> playerIds = new ArrayList<>(game.getPlayers().keySet());
        int currentDrawerIndex = playerIds.indexOf(game.getCurrentDrawerId());
        int nextDrawerIndex = (currentDrawerIndex + 1) % playerIds.size();
        game.setCurrentDrawerId((playerIds.get(nextDrawerIndex)));
        gameRef.child("currentDrawerId").setValue(playerIds.get(nextDrawerIndex));
        showDrawerNotification();
    }

    private void nextPlayer(){
        // Load word pool
        reveal = false;
        gameRef.child("isWordGuessed").setValue(false);
        loadWordPool();
        assignNextWord();
        drawingCanvas.clear();
        resetRoundTimer();
    }

    private void showDrawerNotification() {
        // Create a dialog
        Dialog drawerDialog = new Dialog(this);
        drawerDialog.setContentView(R.layout.dialog_drawer);

        // Find the close button in the dialog
        Button closeButton = drawerDialog.findViewById(R.id.closeButton);

        // Set a click listener to dismiss the dialog
        closeButton.setOnClickListener(view -> drawerDialog.dismiss());

        // Set a listener to handle when the dialog is dismissed (either by button or outside tap)
        drawerDialog.setOnDismissListener(dialog -> nextPlayer());
        // Show the dialog
        if(currentPlayerId.equals(game.getCurrentDrawerId())){
            drawerDialog.show();
        }
    }

    private void startRoundTimerAsHost() {
        long startTime = System.currentTimeMillis();
        gameRef.child("roundStartTime").setValue(startTime);
    }

    private void listenForRoundStartTime() {
        gameRef.child("roundStartTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long startTime = snapshot.getValue(Long.class);
                if (startTime != null) {
                    if (roundTimer != null) {
                        roundTimer.cancel(); // Cancel any ongoing timer
                    }
                    reveal = false;
                    startRoundTimer(startTime); // Start the new timer
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GameActivity.this, "Failed to sync round timer", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void resetRoundTimer() {
        long newStartTime = System.currentTimeMillis();
        gameRef.child("roundStartTime").setValue(newStartTime);
    }

    private void startRoundTimer(long startTime) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        // Total round duration (e.g., 60 seconds = 60,000 milliseconds)
        final long timerDuration = 60000;

        long remainingTime = timerDuration - elapsedTime;

        if (remainingTime > 0) {
            roundTimer = new CountDownTimer(remainingTime, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long secondsRemaining = millisUntilFinished / 1000;
                    timerTextView.setText("" + secondsRemaining + "s");
                }

                @Override
                public void onFinish() {
                    onRoundTimeUp();
                }
            };
            roundTimer.start();
        } else {
            // If the timer has already expired
            onRoundTimeUp();
        }
    }


    private void onRoundTimeUp() {
        if (!game.isWordGuessed()) {
            revealAnswer(); // Reveal the answer if the word wasn’t guessed
        }
    }

    private void leaveGame() {
        gameRef.child("players").child(currentPlayerId).removeValue();
        game.removePlayer(currentPlayerId);

        if (currentPlayerId.equals(game.getHostId())) {
            assignNewHost();
        }
        if (currentPlayerId.equals(game.getCurrentDrawerId())) {
            passDrawerToNextPlayer();
        }
        Intent intent = new Intent(GameActivity.this, HomeActivity.class);
        startActivity(intent);

    }

    private void assignNewHost() {
        List<String> playerIds = new ArrayList<>(game.getPlayers().keySet());
        if (!playerIds.isEmpty()) {
            String newHostId = playerIds.get(0); // Assign the first player as the new host
            gameRef.child("hostId").setValue(newHostId);
        }
    }

    private void endGame() {
        // Update game to "finished" in Firebase
        gameRef.child("gameState").setValue(GameState.FINISHED.name());

    }

    private String calculateWinner() {
        StringBuilder winners = new StringBuilder();
        int maxScore = -1;

        // First, find the highest score
        for (Player player : game.getPlayers().values()) {
            if (player.getScore() > maxScore) {
                maxScore = player.getScore();
            }
        }

        // Then, collect all players with the highest score
        for (Player player : game.getPlayers().values()) {
            if (player.getScore() == maxScore) {
                if (winners.length() > 0) {
                    winners.append(", ");
                }
                winners.append(player.getPlayerName());
            }
        }

        return winners.toString();
    }

    private void showScoreboard() {
        Dialog scoreboardDialog = new Dialog(this);
        scoreboardDialog.setContentView(R.layout.scoreboard_dialog);

        RecyclerView recyclerView = scoreboardDialog.findViewById(R.id.scoreboardRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create adapter with players' data
        List<Player> playerList = new ArrayList<>(game.getPlayers().values());
        ScoreboardAdapter adapter = new ScoreboardAdapter(playerList);
        recyclerView.setAdapter(adapter);

        Button closeButton = scoreboardDialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> scoreboardDialog.dismiss());

        scoreboardDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roundTimer != null) {
            roundTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        // Show a confirmation dialog
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to leave the game?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Perform actions to leave the game, if necessary
                    gameRef.child("players").child(currentPlayerId).removeValue();

                    if (currentPlayerId.equals(game.getHostId())) {
                        assignNewHost();
                    } else if (currentPlayerId.equals(game.getCurrentDrawerId())) {
                        passDrawerToNextPlayer();
                    }
                    super.onBackPressed();
                })
                .setNegativeButton("No", null)
                .show();
    }
}

