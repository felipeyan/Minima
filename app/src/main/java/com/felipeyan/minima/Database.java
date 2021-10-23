package com.felipeyan.minima;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Database extends SQLiteOpenHelper {

    Context context;

    public static final String DATABASE_NAME = "db_notes";
    public static final  String TABLE_NAME = "tb_notes";
    public static final String ID = "id";
    public static final String NOTE = "note";
    public static final String MOD_DATE = "mod_date";

    public Database(Context originContext) { // Database creation
        super(originContext, DATABASE_NAME, null, 1);
        context = originContext;
    }

    @Override
    public void onCreate(SQLiteDatabase db) { // Table creation
        // The "id" column has auto-increment and is used as a parameter for counting notes
        // The "note" column stores encrypted note texts
        // The "mod_note" column stores the date and time when it was last changed
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NOTE + " TEXT, " + MOD_DATE + " VARCHAR(14))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("note", note); // Stores the text received by the function call
        contentValues.put("mod_date", formatDate()); // Added by the date and time formatting function

        return db.insert(TABLE_NAME, null, contentValues) != -1;
    }

    public boolean updateData(String id, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("note", note);
        contentValues.put("mod_date", formatDate()); // The date and time column is updated
        // Updates the note based on the ID received in the function call
        db.update(TABLE_NAME, contentValues, "id = ?", new String[] { id });

        return true;
    }

    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Deletes the note based on the ID received in the function call
        return db.delete(TABLE_NAME, "id = ?", new String[] { id });
    }

    public ArrayList<String> getAllData(Context context, String column, String order) {
        SQLiteDatabase db = this.getWritableDatabase();

        // The String "column" indicates which column of the table will be searched
        // The String "order" indicates the order of values: ascending (ASC) or descending (DESC)

        ArrayList<String> values = new ArrayList<>();

        try {
            Cursor cursor = db.rawQuery("SELECT " + column + " FROM " + TABLE_NAME + " ORDER BY " + MOD_DATE + " " + order, null);
            int index = cursor.getColumnIndex(column);
            cursor.moveToFirst();

            while (!cursor.isAfterLast() && cursor.getCount() > 0) {
                values.add(cursor.getString(index));
                cursor.moveToNext();
            }

            cursor.close(); // End search
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }

        return values; // Returns collected values
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

    public String formatDate() { // Returns the current date and time in String format
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        return simpleDateFormat.format(new Date());
    }
}
