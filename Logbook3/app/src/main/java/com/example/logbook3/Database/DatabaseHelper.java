package com.example.logbook3.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "taskManager";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_COMPLETED = "completed";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_COMPLETED + " INTEGER DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    public long addTask(String name) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        return getWritableDatabase().insert(TABLE_TASKS, null, values);
    }

    public Cursor getAllTasks() {
        return getReadableDatabase().query(TABLE_TASKS, null, null, null, null, null, null);
    }

    public int updateTaskCompletion(int id, boolean completed) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETED, completed ? 1 : 0);
        return getWritableDatabase().update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteTask(int id) {
        getWritableDatabase().delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int updateTaskName(int id, String newName) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName);
        return getWritableDatabase().update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }
}
