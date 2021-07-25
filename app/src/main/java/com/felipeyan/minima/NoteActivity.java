package com.felipeyan.minima;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.os.Bundle;

public class NoteActivity extends AppCompatActivity {

    AppCompatEditText noteField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        noteField = findViewById(R.id.noteField);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}