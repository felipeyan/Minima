package com.felipeyan.minima;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class NoteActivity extends AppCompatActivity {

    Encryption encryption = new Encryption();
    Database database = new Database(this);
    AppCompatEditText noteField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        noteField = findViewById(R.id.noteField);
        receivedData();
    }

    public void receivedData() {
        if (getIntent().hasExtra("selectedNote")) { // Check if received data when start the activity
            noteField.setText(getIntent().getStringExtra("selectedNote"));  // Sets the text received to the note field
        } else if (Intent.ACTION_SEND.equals(getIntent().getAction()) && getIntent().getType() != null) { // Check if received data from another application
            if (getIntent().getType().equals("text/plain")) { // Check if the received data is equal to a text
                noteField.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT)); // Sets the text received to the note field
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!noteField.getText().toString().isEmpty()) {
            if (getIntent().hasExtra("selectedNote")) { // Check if received data when start the activity
                // Checks if the field text is not the same as what was received at the beginning of the activity
                if (!noteField.getText().toString().equals(getIntent().getStringExtra("selectedNote"))) {
                    changeDialog(); // Calls function for note change dialog
                } else { // If there are no changes, close the activity
                    finish();
                }
            } else { // If there is no data received, it is a new note
                if (database.insertData(encryptedNote(noteField.getText().toString()))) { // Stores the encrypted value in the database
                    Toast.makeText(this, R.string.saved_note, Toast.LENGTH_SHORT).show(); // Display a success message
                    finish();
                } else { // If unable to store in database
                    Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show(); // Display a error message
                }
            }
        } else {
            finish();
        }
    }

    public void changeDialog() { // Open a dialog asking if you want to save changes
        new AlertDialog.Builder(this)
            .setMessage(R.string.save_changes)
            .setCancelable(true)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Update the new value in the database
                    if (database.updateData(getIntent().getStringExtra("selectedID"), encryptedNote(noteField.getText().toString()))) {
                        Toast.makeText(NoteActivity.this, R.string.updated_note, Toast.LENGTH_SHORT).show(); // Display a success message
                        finish();
                    } else {
                        Toast.makeText(NoteActivity.this, R.string.error_updating, Toast.LENGTH_SHORT).show(); // Display a error message
                    }
                }
            })
            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
    }

    protected String encryptedNote(String note) { // Returns received text encrypted with stored password
        try {
            note = encryption.encrypt(note, new Preferences(this).getPassword());
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show(); // Display a error message
        }

        return note;
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        database.close();
    }
}