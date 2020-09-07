package com.kontranik.offlinewebreader;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAdapter {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public DatabaseAdapter(Context context){
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public DatabaseAdapter open(){
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    private Cursor getAllEntries(){
        String[] columns = new String[] {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_NAME,
                DatabaseHelper.COLUMN_FILENAME,
                DatabaseHelper.COLUMN_ORIGIN,
                DatabaseHelper.COLUMN_IMAGE,
                DatabaseHelper.COLUMN_POSITION,
                DatabaseHelper.COLUMN_CREATED
        };
        return  database.query(DatabaseHelper.TABLE, columns, null, null, null, null, null);
    }

    public List<OfflinePage> getPages(){
        ArrayList<OfflinePage> pages = new ArrayList<>();
        Cursor cursor = getAllEntries();
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                String filename = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FILENAME));
                String origin = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ORIGIN));
                byte[] image = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE));
                float position = cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.COLUMN_POSITION));
                Long created = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED));
                pages.add(new OfflinePage(id, origin, name, filename, image, position, created));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return  pages;
    }

    public long getCount(){
        return DatabaseUtils.queryNumEntries(database, DatabaseHelper.TABLE);
    }

    public OfflinePage getEntry(long id){
        OfflinePage entry = null;
        String query = String.format("SELECT * FROM %s WHERE %s=?",DatabaseHelper.TABLE, DatabaseHelper.COLUMN_ID);
        Cursor cursor = database.rawQuery(query, new String[]{ String.valueOf(id)});
        if(cursor.moveToFirst()){
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            String filename = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FILENAME));
            String origin = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ORIGIN));
            byte[] imagename = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE));
            float position = cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.COLUMN_POSITION));
            Long created = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED));
            entry = new OfflinePage(id, origin, name, filename, imagename, position, created);
        }
        cursor.close();
        return  entry;
    }

    public long insert(OfflinePage entry){

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NAME, entry.getName());
        cv.put(DatabaseHelper.COLUMN_FILENAME, entry.getFilename());
        cv.put(DatabaseHelper.COLUMN_ORIGIN, entry.getOrigin());
        cv.put(DatabaseHelper.COLUMN_IMAGE, entry.getImage());
        cv.put(DatabaseHelper.COLUMN_POSITION, entry.getPosition());
        cv.put(DatabaseHelper.COLUMN_CREATED, entry.getCreated());

        return  database.insert(DatabaseHelper.TABLE, null, cv);
    }

    public long delete(long userId){

        String whereClause = "_id = ?";
        String[] whereArgs = new String[]{String.valueOf(userId)};
        return database.delete(DatabaseHelper.TABLE, whereClause, whereArgs);
    }

    public long update(OfflinePage entry){

        String whereClause = DatabaseHelper.COLUMN_ID + "=" + String.valueOf(entry.getId());
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NAME, entry.getName());
        cv.put(DatabaseHelper.COLUMN_FILENAME, entry.getFilename());
        cv.put(DatabaseHelper.COLUMN_ORIGIN, entry.getOrigin());
        cv.put(DatabaseHelper.COLUMN_IMAGE, entry.getImage());
        cv.put(DatabaseHelper.COLUMN_POSITION, entry.getPosition());
        cv.put(DatabaseHelper.COLUMN_CREATED, entry.getCreated());
        return database.update(DatabaseHelper.TABLE, cv, whereClause, null);
    }
}