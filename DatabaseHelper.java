package com.offlineai.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.offlineai.app.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "offlineai.db";
    private static final int DB_VERSION = 1;

    // Tables
    private static final String TABLE_HISTORY = "chat_history";
    private static final String TABLE_SESSIONS = "chat_sessions";

    // History columns
    private static final String COL_ID = "id";
    private static final String COL_SESSION_ID = "session_id";
    private static final String COL_TEXT = "text";
    private static final String COL_TYPE = "type";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_IMAGE_PATH = "image_path";

    // Session columns
    private static final String COL_TITLE = "title";
    private static final String COL_PREVIEW = "preview";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SESSIONS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_PREVIEW + " TEXT, " +
                COL_TIMESTAMP + " INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SESSION_ID + " INTEGER, " +
                COL_TEXT + " TEXT, " +
                COL_TYPE + " INTEGER, " +
                COL_IMAGE_PATH + " TEXT, " +
                COL_TIMESTAMP + " INTEGER, " +
                "FOREIGN KEY(" + COL_SESSION_ID + ") REFERENCES " + TABLE_SESSIONS + "(" + COL_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        onCreate(db);
    }

    // --- Session Methods ---

    public long createSession(String title, String preview) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_PREVIEW, preview);
        values.put(COL_TIMESTAMP, System.currentTimeMillis());
        return db.insert(TABLE_SESSIONS, null, values);
    }

    public void updateSession(long sessionId, String preview) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PREVIEW, preview);
        values.put(COL_TIMESTAMP, System.currentTimeMillis());
        db.update(TABLE_SESSIONS, values, COL_ID + "=?", new String[]{String.valueOf(sessionId)});
    }

    public List<long[]> getAllSessions() {
        // Returns [id, timestamp]
        List<long[]> sessions = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SESSIONS, null, null, null, null, null, COL_TIMESTAMP + " DESC");
        while (c.moveToNext()) {
            sessions.add(new long[]{c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                    c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP))});
        }
        c.close();
        return sessions;
    }

    public String[] getSessionInfo(long sessionId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SESSIONS, null, COL_ID + "=?",
                new String[]{String.valueOf(sessionId)}, null, null, null);
        if (c.moveToFirst()) {
            String title = c.getString(c.getColumnIndexOrThrow(COL_TITLE));
            String preview = c.getString(c.getColumnIndexOrThrow(COL_PREVIEW));
            c.close();
            return new String[]{title, preview};
        }
        c.close();
        return new String[]{"Chat", ""};
    }

    public void deleteSession(long sessionId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_HISTORY, COL_SESSION_ID + "=?", new String[]{String.valueOf(sessionId)});
        db.delete(TABLE_SESSIONS, COL_ID + "=?", new String[]{String.valueOf(sessionId)});
    }

    // --- Message Methods ---

    public void saveMessage(long sessionId, ChatMessage msg) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SESSION_ID, sessionId);
        values.put(COL_TEXT, msg.getText());
        values.put(COL_TYPE, msg.getType());
        values.put(COL_IMAGE_PATH, msg.getImagePath());
        values.put(COL_TIMESTAMP, msg.getTimestamp());
        db.insert(TABLE_HISTORY, null, values);
    }

    public List<ChatMessage> getSessionMessages(long sessionId) {
        List<ChatMessage> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_HISTORY, null, COL_SESSION_ID + "=?",
                new String[]{String.valueOf(sessionId)}, null, null, COL_TIMESTAMP + " ASC");
        while (c.moveToNext()) {
            String text = c.getString(c.getColumnIndexOrThrow(COL_TEXT));
            int type = c.getInt(c.getColumnIndexOrThrow(COL_TYPE));
            String imagePath = c.getString(c.getColumnIndexOrThrow(COL_IMAGE_PATH));
            messages.add(new ChatMessage(text, type, imagePath));
        }
        c.close();
        return messages;
    }

    public Cursor getHistorySessions() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT s." + COL_ID + ", s." + COL_TITLE + ", s." + COL_PREVIEW +
                ", s." + COL_TIMESTAMP + " FROM " + TABLE_SESSIONS + " s " +
                "ORDER BY s." + COL_TIMESTAMP + " DESC", null);
    }
}
