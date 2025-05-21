package com.example.mysteryartist;

public class Player {
    private String playerId;
    private String playerName;
    private PlayerRole role;
    private int score;

    public Player() {
        // Default constructor for Firebase
    }

    public Player(String playerId, String playerName, PlayerRole role) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.role = role;
        this.score = 0; // Default score
    }

    // --- Getters and Setters ---
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public PlayerRole getRole() {
        return role;
    }

    public String getName() {
        return playerName;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }

    public int getScore() {
        return score;
    }

    public void incrementScore() {
        score++;
    }
}
