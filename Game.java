package com.example.mysteryartist;

import java.util.HashMap;
import java.util.Map;

public class Game {
    private String gameCode;
    private String hostId; // ID of the host
    private String currentDrawerId; // ID of the current drawer
    private Map<String, Player> players; // Map of player ID to Player object
    private String currentWord; // The word being drawn
    private boolean isWordGuessed; // To track if the game is ongoing or paused
    private GameState gameState; // Current state of the game

    public Game() {
        // Default constructor for Firebase
    }

    public Game(String gameCode, String hostId, String hostName) {
        this.gameCode = gameCode;
        this.hostId = hostId;
        this.players = new HashMap<>();
        this.players.put(hostId, new Player(hostId, hostName, PlayerRole.ARTIST));
        this.currentDrawerId = hostId; // Initially, host is the drawer
        this.isWordGuessed = false; // Game starts as active
        this.gameState = GameState.WAITING; // Initial state is WAITING
    }

    // --- Getters and Setters ---
    public String getGameCode() {
        return gameCode;
    }

    public String getHostId() {
        return hostId;
    }

    public String getCurrentDrawerId() {
        return currentDrawerId;
    }

    public void setCurrentDrawerId(String currentDrawerId) {
        this.currentDrawerId = currentDrawerId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    public int getPlayersCount() {
        return players.size();
    }


    public void setPlayers(Map<String, Player> players) {
        this.players = players;
    }

    public void removePlayer(String playerId) {
        if (players == null || !players.containsKey(playerId)) {
            return; // Player does not exist
        }
        players.remove(playerId);

    }
    public String getCurrentWord() {
        return currentWord;
    }

    public void setCurrentWord(String currentWord) {
        this.currentWord = currentWord;
    }

    public boolean isWordGuessed() {
        return isWordGuessed;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    // --- Game Logic Methods ---
    /**
     * Add a player to the game.
     *
     * @param playerId   The unique ID of the player.
     * @param playerName The name of the player.
     */
    public void addPlayer(String playerId, String playerName) {
        if (players == null) {
            players = new HashMap<>();
        }
        players.put(playerId, new Player(playerId, playerName, PlayerRole.PLAYER));
    }

    /**
     * Get the role of a player.
     *
     * @param playerId The ID of the player.
     * @return The role of the player (ARTIST, GUESSER, or HOST).
     */
    public PlayerRole getPlayerRole(String playerId) {
        Player player = players.get(playerId);
        return player != null ? player.getRole() : null;
    }


    /**
     * Reset the game state for a new round.
     */
    public void resetForNewRound() {
        this.currentWord = null; // Clear the current word
        // Other reset logic if necessary
    }
}
