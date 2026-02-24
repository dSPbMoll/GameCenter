package com.example.gamecenter.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
    private static final String SCORE_TABLE_CREATE ="CREATE TABLE " + SCORE_TABLE + " (" +
            SCORE_ID + " INTEGER PRIMARY KEY, " +
            SCORE_USERID + " INTEGER," +
            SCORE_DATETIME + " DATETIME," +
            SCORE_SCORE + " INTEGER," +
            SCORE_GAME + " TEXT CHECK(" + SCORE_GAME + " IN ('_2048', 'JUICE_DUNGEON_2'))" + ");";

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
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SCORE_TABLE);
        onCreate(db);
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

    public long countScore(){
        if (mReadableDB == null) {
            mReadableDB = getReadableDatabase();
        }
        return DatabaseUtils.queryNumEntries(mReadableDB, USER_TABLE);
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

    public int updateScore(int id, int userId, String datetime, int score, String game) {
        int mNumberOfRowsUpdated = -1;

        try {
            if (mWritableDB == null) {
                mWritableDB = getWritableDatabase();
            }

            ContentValues values = new ContentValues();
            values.put(SCORE_USERID, userId);
            values.put(SCORE_DATETIME, datetime);
            values.put(SCORE_SCORE, score);
            values.put(SCORE_GAME, game);

            mNumberOfRowsUpdated = mWritableDB.update(SCORE_TABLE,
                    values,
                    // selection criteria for row (the _id column)
                    USER_ID + " = ?",
                    //selection args; value of id
                    new String[]{String.valueOf(id)});
        } catch(Exception e) {
            Log.d (TAG, "UPDATE EXCEPTION: " + e.getMessage());
        }
        return mNumberOfRowsUpdated;
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
}
