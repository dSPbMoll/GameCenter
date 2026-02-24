package com.example.gamecenter.session;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {

    public static final String SESSION_PREFS = "SESSION_PREFS";
    public static final String KEY_USER_ID = "USER_ID";
    public static final String KEY_USERNAME = "USERNAME";

    private SessionManager() {}

    public static int getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public static void saveSession(Context context, int userId, String username) {
        SharedPreferences prefs = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }
}