package com.example.gamecenter.database;

public class UserDBItem {
    private String username;
    private String password;

    public UserDBItem() {}

    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
}
