package com.kontranik.offlinewebreader;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.content.ContentValues;

import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "offlinewebreader.db"; // название бд
    private static final int SCHEMA = 1; // версия базы данных
    static final String TABLE = "pages"; // название таблицы в бд
    // названия столбцов
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ORIGIN = "origin";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_CREATED = "created";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE + " (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT, "
                + COLUMN_ORIGIN + " TEXT, "
                + COLUMN_IMAGE + " BLOB, "
                + COLUMN_POSITION + " REAL, "
                + COLUMN_CREATED + " TEXT"
                + ");");


        // добавление начальных данных
        long d = new Date().getTime();
        db.execSQL("INSERT INTO "+ TABLE +" (" + COLUMN_NAME
                + ", " + COLUMN_ORIGIN  + ", " + COLUMN_CREATED + ") VALUES ('test1', 'http://test1', " + d + ");");
        db.execSQL("INSERT INTO "+ TABLE +" (" + COLUMN_NAME
                + ", " + COLUMN_ORIGIN  + ", " + COLUMN_CREATED + ") VALUES ('test2', 'http://test2', " + d + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);
        onCreate(db);
    }
}