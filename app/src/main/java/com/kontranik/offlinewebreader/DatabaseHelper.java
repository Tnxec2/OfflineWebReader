package com.kontranik.offlinewebreader;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.content.ContentValues;

import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "offlinewebreader.db";
    private static final int SCHEMA = 2;
    static final String TABLE = "pages";
    //
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FILENAME = "filename";
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
                + COLUMN_FILENAME + " TEXT, "
                + COLUMN_ORIGIN + " TEXT, "
                + COLUMN_IMAGE + " BLOB, "
                + COLUMN_POSITION + " REAL, "
                + COLUMN_CREATED + " TEXT"
                + ");");


        // insert test data
        long d = new Date().getTime();
        db.execSQL("INSERT INTO "+ TABLE +" ("
                + COLUMN_NAME + ", "
                + COLUMN_FILENAME + ", "
                + COLUMN_ORIGIN  + ", "
                + COLUMN_CREATED + ") VALUES ('test1', 'savedarchiv_1.xml', 'http://test1', " + d + ");");
        db.execSQL("INSERT INTO "+ TABLE +" ("
                + COLUMN_NAME + ", "
                + COLUMN_FILENAME + ", "
                + COLUMN_ORIGIN  + ", "
                + COLUMN_CREATED + ") VALUES ('test2', 'savedarchiv_2.xml', 'http://test2', " + d + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);
        onCreate(db);
    }
}