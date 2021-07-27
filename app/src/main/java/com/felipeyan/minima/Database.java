package com.felipeyan.minima;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class Database extends SQLiteOpenHelper {

    Context context;

    public static final String DATABASE_NAME = "db_notes";
    public static final  String TABLE_NAME = "tb_notes";
    public static final String ID = "id";
    public static final String NOTE = "note";

    public Database(Context originContext) {
        super(originContext, DATABASE_NAME, null, 1);
        context = originContext;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NOTE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("note", note);

        return db.insert(TABLE_NAME, null, contentValues) != -1;
    }

    public boolean updateData(String id, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("note", note);
        db.update(TABLE_NAME, contentValues, "id = ?", new String[] { id });

        return true;
    }

    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "id = ?", new String[] { id });
    }

    public ArrayList<String> getAllData(Context context, String column) {
        SQLiteDatabase db = this.getWritableDatabase();

        ArrayList<String> values = new ArrayList<>();

        try {
            Cursor cursor = db.rawQuery("SELECT " + column + " FROM " + TABLE_NAME, null);
            int index = cursor.getColumnIndex(column);
            cursor.moveToFirst();

            while (!cursor.isAfterLast() && cursor.getCount() > 0) {
                values.add(cursor.getString(index));
                cursor.moveToNext();
            }

            cursor.close();
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }

        Collections.reverse(values);

        return values;
    }

    public void deleteDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @Override
    public synchronized void close () {
        SQLiteDatabase db = this.getWritableDatabase();

        if (db != null) {
            db.close();
            super.close();
        }
    }
}
