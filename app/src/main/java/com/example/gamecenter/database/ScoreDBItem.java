package com.example.gamecenter.database;

public class ScoreDBItem {
    private int id;
    private int userid;
    private String datetime;
    private int score;
    private String game;

    public ScoreDBItem() {}

    public void setId(int id) {
        this.id = id;
    }
    public void setUserid(int userid) {
        this.userid = userid;
    }
    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public void setGame(String game) {
        this.game = game;
    }
    public int getId() {
        return id;
    }
    public int getUserid() {
        return userid;
    }
    public String getDatetime() {
        return datetime;
    }
    public int getScore() {
        return score;
    }
    public String getGame() {
        return game;
    }

}
