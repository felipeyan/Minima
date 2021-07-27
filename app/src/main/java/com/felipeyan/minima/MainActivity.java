package com.felipeyan.minima;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Database database = new Database(this);
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        verifyPassword(); // Calls the function that checks the stored password
        listNotes(); // Calls the RecyclerView creation function
    }

    public void openMenu(View view) { // Displays the menu after clicking the 3-dot icon
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenu().add(R.string.menu_settings); // Settings screen option
        menu.getMenu().add(R.string.menu_about); // About screen option

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals(getString(R.string.menu_settings))) {
                    openSettings();
                    return true;
                } else if (item.getTitle().toString().equals(getString(R.string.menu_about))) {
                    openAbout();
                    return true;
                } else {
                    return false;
                }
            }
        });

        menu.show();
    }

    public void listNotes() { // Creates the RecyclerView that displays the notes saved in the database
        ArrayList<String> noteIDS = database.getAllData(this, "id");
        ArrayList<String> noteTEXTS = database.getAllData(this, "note");

        recyclerView = findViewById(R.id.mainRV);
        recyclerView.setAdapter(new NoteAdapter(this, noteIDS, noteTEXTS));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void addNote(View view) { // Launches the "Note" screen
        startActivity(new Intent(this, NoteActivity.class));
    }

    public void openSettings() { // Launches the "Settings" screen
        Toast.makeText(this, R.string.menu_settings, Toast.LENGTH_SHORT).show();
    }

    public void openAbout() { // Launches the "About" screen
        Toast.makeText(this, R.string.menu_about, Toast.LENGTH_SHORT).show();
    }

    public void verifyPassword() { // Check if you have a password for note encryption, if not, create and store a new one
        if (getSharedPreferences("userPref", MODE_PRIVATE).getString("userPw", "").isEmpty()) {
            try {
                new Preferences(this).generateDefaultPass();
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();;
            }
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        database.close();
    }
}