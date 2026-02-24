package com.example.gamecenter.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GameCenterOpenHelper extends SQLiteOpenHelper {
    // It's a good idea to always define a log tag like this.
    private static final String TAG = GameCenterOpenHelper.class.getSimpleName();
    // has to be 1 first time or app will crash
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "GameCenter";

    // ====================== USER TABLE =======================
    private static final String USER_TABLE = "USER";
    public static final String USER_ID = "id";
    public static final String USER_USERNAME = "username";
    public static final String USER_PASSWORD = "password";
    private static final String[] USER_COLUMNS = {USER_ID, USER_USERNAME, USER_PASSWORD};
    private static final String USER_TABLE_CREATE =
            "CREATE TABLE " + USER_TABLE + " (" +
                    USER_ID + " INTEGER PRIMARY KEY, " +
                    // id will auto-increment if no value passed
                    USER_USERNAME + " TEXT," +
                    USER_PASSWORD + " TEXT" + ");";

    // ====================== SCORE TABLE =======================
    private static final String SCORE_TABLE = "SCORE";
    public static final String SCORE_ID = "id";
    public static final String SCORE_USERID = "userid";
    public static final String SCORE_SCORE = "score";
    public static final String SCORE_DATETIME = "datetime";
    public static final String SCORE_GAME = "game";
    private static final String[] SCORE_COLUMNS = {SCORE_ID, SCORE_USERID, SCORE_DATETIME, SCORE_SCORE, SCORE_GAME};
    private static final String SCORE_TABLE_CREATE =
            "CREATE TABLE " + SCORE_TABLE + " (" +
                    SCORE_ID + " INTEGER PRIMARY KEY, " +
                    SCORE_USERID + " INTEGER NOT NULL, " +
                    SCORE_DATETIME + " TEXT NOT NULL, " +
                    SCORE_SCORE + " INTEGER NOT NULL, " +
                    SCORE_GAME + " TEXT NOT NULL CHECK(" + SCORE_GAME + " IN ('_2048', 'JUICE_DUNGEON_2')), " +
                    "FOREIGN KEY(" + SCORE_USERID + ") REFERENCES " + USER_TABLE + "(" + USER_ID + ") ON DELETE CASCADE" +
                    ");";

    // ========================= DATA BASES =========================
    private SQLiteDatabase mWritableDB;
    private SQLiteDatabase mReadableDB;

    public GameCenterOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(USER_TABLE_CREATE);
        db.execSQL(SCORE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(GameCenterOpenHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + SCORE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Necesario para que SQLite haga cumplir las FK en Android
        db.setForeignKeyConstraintsEnabled(true);
    }

    public ScoreDBItem queryScore(int position) {
        String query = "SELECT * FROM " + SCORE_TABLE +
                " ORDER BY " + SCORE_SCORE + " DESC " +
                "LIMIT " + position + ",20";

        Cursor cursor = null;
        ScoreDBItem entry = new ScoreDBItem();

        try {
            if (mReadableDB == null) {
                mReadableDB = getReadableDatabase();
            }
            cursor = mReadableDB.rawQuery(query, null);
            cursor.moveToFirst();

            entry.setId(cursor.getInt(cursor.getColumnIndexOrThrow(SCORE_ID)));
            entry.setUserid(cursor.getInt(cursor.getColumnIndexOrThrow(SCORE_USERID)));
            entry.setDatetime(cursor.getString(cursor.getColumnIndexOrThrow(SCORE_DATETIME)));
            entry.setScore(cursor.getInt(cursor.getColumnIndexOrThrow(SCORE_SCORE)));
            entry.setGame(cursor.getString(cursor.getColumnIndexOrThrow(SCORE_GAME)));

        } catch (Exception e) {
            Log.d(TAG, "EXCEPTION! " + e);

        } finally {
            cursor.close();
            return entry;
        }
    }

    public long insertScore(int userId, String datetime, int score, String game){
        long newId = 0;

        ContentValues values = new ContentValues();
        values.put(SCORE_USERID, userId);
        values.put(SCORE_DATETIME, datetime);
        values.put(SCORE_SCORE, score);
        values.put(SCORE_GAME, game);

        try {
            if (mWritableDB == null) {
                mWritableDB = getWritableDatabase();
            }

            newId = mWritableDB.insert(SCORE_TABLE, null, values);

        } catch (Exception e) {
            Log.d(TAG, "INSERT EXCEPTION! " + e.getMessage());

        }

        return newId;
    }

    public Cursor queryTopScoresByGame(String gameName, int limit) {
        String query = "SELECT * FROM " + SCORE_TABLE +
                " WHERE " + SCORE_GAME + " = ?" +
                " ORDER BY " + SCORE_SCORE + " DESC" +
                " LIMIT ?";

        Cursor cursor = null;
        try {
            if (mReadableDB == null) {
                mReadableDB = getReadableDatabase();
            }
            cursor = mReadableDB.rawQuery(query, new String[]{gameName, String.valueOf(limit)});
        } catch (Exception e) {
            Log.e(TAG, "Error consultando ranking: " + e.getMessage());
        }
        return cursor;
    }

    public int deleteScore(int id) {
        int deleted = 0;

        try {
            if (mWritableDB == null) {
                mWritableDB = getWritableDatabase();
            }

            deleted = mWritableDB.delete(SCORE_TABLE,
                    SCORE_ID + " = ? ", new String[]{String.valueOf(id)});

        } catch (Exception e) {
            Log.d (TAG, "DELETE EXCEPTION! " + e.getMessage());
        }
        return deleted;
    }

    public Cursor searchByGame(String gameName) {
        String[] columns = SCORE_COLUMNS;

        String selection = SCORE_GAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + gameName + "%"};

        Cursor cursor = null;
        try {
            if (mReadableDB == null) { mReadableDB = getReadableDatabase(); }

            cursor = mReadableDB.query(SCORE_TABLE, columns, selection, selectionArgs, null, null, SCORE_SCORE + " DESC");
        } catch (Exception e) {
            Log.d(TAG, "SEARCH EXCEPTION: " + e.getMessage());
        }
        return cursor;
    }

    public ScoreDBItem queryLastScoreByGame(String gameName) {
        String query = "SELECT * FROM " + SCORE_TABLE +
                " WHERE " + SCORE_GAME + " = ?" +
                " ORDER BY " + SCORE_DATETIME + " DESC " +
                " LIMIT 1";

        Cursor cursor = null;
        ScoreDBItem entry = null;

        try {
            if (mReadableDB == null) {
                mReadableDB = getReadableDatabase();
            }

            cursor = mReadableDB.rawQuery(query, new String[]{gameName});

            if (cursor != null && cursor.moveToFirst()) {
                entry = new ScoreDBItem();
                entry.setId(cursor.getInt(cursor.getColumnIndexOrThrow(SCORE_ID)));
                entry.setUserid(cursor.getInt(cursor.getColumnIndexOrThrow(SCORE_USERID)));
                entry.setDatetime(cursor.getString(cursor.getColumnIndexOrThrow(SCORE_DATETIME)));

                entry.setScore(cursor.getInt(cursor.getColumnIndexOrThrow(SCORE_SCORE)));
                entry.setGame(cursor.getString(cursor.getColumnIndexOrThrow(SCORE_GAME)));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error consultando última puntuación: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return entry;
    }

    // USERS

    public long registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username vacío");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("password vacío");
        }

        username = username.trim();

        // Si ya existe, no registramos
        if (userExists(username)) {
            throw new IllegalStateException("El usuario ya existe");
        }

        long newId = -1;
        try {
            if (mWritableDB == null) {
                mWritableDB = getWritableDatabase();
            }

            ContentValues values = new ContentValues();
            values.put(USER_USERNAME, username);
            values.put(USER_PASSWORD, password);

            newId = mWritableDB.insertOrThrow(USER_TABLE, null, values);
        } catch (SQLException e) {
            Log.e(TAG, "REGISTER SQL EXCEPTION: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "REGISTER EXCEPTION: " + e.getMessage());
        }

        return newId;
    }

    public boolean userExists(String username) {
        if (username == null) return false;

        username = username.trim();

        Cursor cursor = null;
        try {
            if (mReadableDB == null) {
                mReadableDB = getReadableDatabase();
            }

            cursor = mReadableDB.query(
                    USER_TABLE,
                    new String[]{USER_ID},
                    USER_USERNAME + " = ?",
                    new String[]{username},
                    null,
                    null,
                    null
            );

            return cursor != null && cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, "userExists EXCEPTION: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public int loginUser(String username, String password) {
        if (username == null || password == null) return -1;

        username = username.trim();
        if (username.isEmpty() || password.isEmpty()) return -1;

        Cursor cursor = null;
        try {
            if (mReadableDB == null) {
                mReadableDB = getReadableDatabase();
            }

            String selection = USER_USERNAME + " = ? AND " + USER_PASSWORD + " = ?";
            String[] selectionArgs = new String[]{username, password};

            cursor = mReadableDB.query(
                    USER_TABLE,
                    new String[]{USER_ID, USER_USERNAME},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID));
            }
        } catch (Exception e) {
            Log.e(TAG, "LOGIN EXCEPTION: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return -1;
    }

    public String getUsernameById(int userId) {
        Cursor cursor = null;
        try {
            if (mReadableDB == null) {
                mReadableDB = getReadableDatabase();
            }

            cursor = mReadableDB.query(
                    USER_TABLE,
                    new String[]{USER_USERNAME},
                    USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(USER_USERNAME));
            }
        } catch (Exception e) {
            Log.e(TAG, "getUsernameById EXCEPTION: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public Cursor queryTopScoresWithUsernameByGame(String gameName, int limit) {
        // Traemos username desde USER y los campos relevantes de SCORE
        // + IMPORTANT: también traemos s.id para poder borrar luego por swipe
        String query =
                "SELECT " +
                        "s." + SCORE_ID + " AS id, " +                  // <-- añadido
                        "u." + USER_USERNAME + " AS username, " +
                        "s." + SCORE_DATETIME + " AS datetime, " +
                        "s." + SCORE_SCORE + " AS score, " +
                        "s." + SCORE_GAME + " AS game " +
                        "FROM " + SCORE_TABLE + " s " +
                        "JOIN " + USER_TABLE + " u " +
                        "ON s." + SCORE_USERID + " = u." + USER_ID + " " +
                        "WHERE s." + SCORE_GAME + " = ? " +
                        "ORDER BY s." + SCORE_SCORE + " DESC " +
                        "LIMIT ?";

        Cursor cursor = null;
        try {
            if (mReadableDB == null) {
                mReadableDB = getReadableDatabase();
            }
            cursor = mReadableDB.rawQuery(query, new String[]{gameName, String.valueOf(limit)});
        } catch (Exception e) {
            Log.e(TAG, "queryTopScoresWithUsernameByGame EXCEPTION: " + e.getMessage());
        }
        return cursor;
    }
}
