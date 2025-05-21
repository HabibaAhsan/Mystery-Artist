package com.example.mysteryartist;

public class Guess {
    private String word;
    private String playerId;
    private String playerName;


    public Guess() { } // Required for Firebase

    public Guess(String word, String playerId,String Name) {
        this.word = word;
        this.playerId = playerId;
        this.playerName=Name;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

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
}
