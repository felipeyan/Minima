package com.felipeyan.minima;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Export {
    Encryption encryption = new Encryption();
    Context context;

    public Export(Context originContext) {
        context = originContext;
    }

    public boolean checkStoragePermission() { // Returns true if the app has permission to store files
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean exportTXT() { // Function to export all notes in TXT
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Minima");

            if (!root.exists()) { // If destination directory does not exist
                root.mkdir(); // Create it
            }

            FileWriter writer = new FileWriter(new File(root, fileName()));

            writer.append(getNotes()); // Write the received notes into the file
            writer.flush();
            writer.close();

            return true;
        } catch (IOException e) {
            return false; // Returns false if operation is not successful
        }
    }

    public String fileName() { // Returns the filename based on the current year, month and day
        return new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + "notes.txt";
    }

    public StringBuilder getNotes() { // Returns a String with all notes formatted and decrypted
        StringBuilder notes = new StringBuilder();
        int notePos = 0;

        for (String note: new Database(context).getAllData(context, "note", false)) {
            notes.append("### Entry number ").append(notePos).append(" ###\n") // Separates the notes using the variable notePos as an index
                    .append(encryption.decryptNote(context, note)) // Add the decrypted note
                    .append("\n\n"); // Skip two lines
            notePos++; // Add one to the index
        }

        return notes; // Return all notes in a String
    }
}
