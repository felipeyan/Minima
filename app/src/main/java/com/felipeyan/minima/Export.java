package com.felipeyan.minima;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Export {
    Encryption encryption = new Encryption();
    Context context;

    public Export(Context context) {
        this.context = context;
    }

    public boolean checkStoragePermission() { // Returns true if the app has permission to store files
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public Intent exportAsTXT() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, fileName());

        return intent;
    }

    public String fileName() { // Returns the filename based on the current date and time in String format
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()) + "notes.txt";
    }

    public StringBuilder getNotes() { // Returns a String with all notes formatted and decrypted
        StringBuilder notes = new StringBuilder();
        int notePos = 0;

        for (String note: new Database(context).getAllData("note", "ASC")) {
            notes.append("### Entry number ").append(notePos).append(" ###\n") // Separates the notes using the variable notePos as an index
                    .append(encryption.decryptNote(context, note)) // Add the decrypted note
                    .append("\n\n"); // Skip two lines
            notePos++; // Add one to the index
        }

        return notes; // Return all notes in a String
    }
}
