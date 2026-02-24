package com.example.gamecenter.score;

public class GameScore {
    private String username;
    private String datetime;
    private int imageResource;
    private int score;
    private String game;

    public GameScore(String username, String datetime, int imageResource, int score, String game) {
        this.username = username;
        this.datetime = datetime;
        this.imageResource = imageResource;
        this.score = score;
        this.game = game;
    }

    public String getUsername() {
        return username;
    }
    public String getDatetime() {
        return datetime;
    }
    public int getImageResource() {
        return imageResource;
    }
    public int getScore() {
        return score;
    }
    public String getGame() {
        return game;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public void setGame(String game) {
        this.game = game;
    }


}
