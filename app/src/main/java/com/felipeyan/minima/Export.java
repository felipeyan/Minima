package com.felipeyan.minima;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public Intent exportAsJSON() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_TITLE, fileName());

        return intent;
    }

    public void shareText(String note) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, encryption.decryptNote(context, note));
        context.startActivity(Intent.createChooser(intent, null));
    }

    public String fileName() { // Returns the filename based on the current date and time in String format
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()) + "notes.json";
    }

    public void writeInFile(Uri filePath, StringBuilder builder) throws IOException {
        OutputStream outputStream = context.getContentResolver().openOutputStream(filePath);
        outputStream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }

    public String readFile(Uri filePath) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(filePath);
        int file = inputStream.available();
        byte[] bufferData = new byte[file];
        inputStream.read(bufferData);
        inputStream.close();

        return new String(bufferData, StandardCharsets.UTF_8);
    }

    public StringBuilder formatJSONFile() {
        Database database = new Database(context);
        ArrayList<String> notesArray = database.getAllData("note", "ASC");
        ArrayList<String> modsArray = database.getAllData("mod_date", "ASC");
        StringBuilder builder = new StringBuilder();

        builder.append("{\n\t\"notes\": [\n");

        for (int i = 0; i < notesArray.size(); i++) {
            String note = encryption.decryptNote(context, notesArray.get(i));

            note = note.replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");

            builder.append("\t\t{\n\t\t\t\"note\": \"")
                .append(note)
                .append("\",\n\t\t\t\"mod_date\": \"")
                .append(modsArray.get(i))
                .append("\"")
                .append(i == (notesArray.size() - 1) ? "\n\t\t}\n" : "\n\t\t},\n");
        }

        builder.append("\t]\n}");
        return builder;
    }

    public ArrayList<String> getJSONArrayData(Uri filePath, String value) {
        ArrayList<String> array = new ArrayList<>();
        String json = null;

        try {
            json = readFile(filePath);

            try {
                JSONObject object = new JSONObject(json);
                JSONArray jsonArray = object.getJSONArray("notes");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject data = jsonArray.getJSONObject(i);
                    array.add(data.getString(value));
                }
            } catch (JSONException e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }

        return array;
    }
}
