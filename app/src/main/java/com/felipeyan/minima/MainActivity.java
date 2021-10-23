 package com.felipeyan.minima;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Database database = new Database(this);
    ArrayList<String> noteIDS, noteTEXTS, noteMOD;
    AppCompatTextView mainTitle;
    NoteAdapter noteAdapter;
    RecyclerView recyclerView;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If a PIN password exists, go to PinActivity
        if (!getSharedPreferences("userPref", MODE_PRIVATE).getString("userPIN", "").isEmpty()) {
            startActivity(new Intent(this, PinActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        searchView = findViewById(R.id.mainSV);
        recyclerView = findViewById(R.id.mainRV);
        mainTitle = findViewById(R.id.mainTitle);

        // Changes the Activity text font to the stored value
        new Preferences(this).changeAppFont(this);
        // Changes toolbar title font
        new Preferences(this).changeViewFont("TextView", mainTitle);

        styleSearch(); // Stylize SearchView
        verifyPassword(); // Calls the function that checks the stored password
        listNotes(); // Calls the RecyclerView creation function

        searchView.setOnCloseListener(this::toggleSearchView); // When the close search button is pressed

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                noteAdapter.getFilter().filter(query); // Search the note when the keyboard search icon is pressed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                noteAdapter.getFilter().filter(newText); // Search the note when text is modified
                return false;
            }
        });
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
        noteIDS = database.getAllData(this, "id", "DESC"); // Collects all note IDS
        noteTEXTS = database.getAllData(this, "note", "DESC"); // Collect all note texts
        noteMOD = database.getAllData(this, "mod_date", "DESC"); // Collects all last modified dates

        noteAdapter = new NoteAdapter(this, noteIDS, noteTEXTS, noteMOD);
        recyclerView.setAdapter(noteAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void addNote(View view) { // Launches the "Note" screen
        startActivity(new Intent(this, NoteActivity.class));
    }

    public void openSettings() { // Launches the "Settings" screen
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void openAbout() { // Launches the "About" screen
        Toast.makeText(this, R.string.menu_about, Toast.LENGTH_SHORT).show();
    }

    public void verifyPassword() { // Check if you have a password for note encryption, if not, create and store a new one
        if (getSharedPreferences("userPref", MODE_PRIVATE).getString("userPw", "").isEmpty()) {
            try {
                new Preferences(this).generateDefaultPass();
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean toggleSearchView() { // Hides the other Toolbar Views and displays the SearchView
        findViewById(R.id.mainTitle).setVisibility(findViewById(R.id.mainTitle).getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE); // Toolbar title
        findViewById(R.id.mainTools).setVisibility(findViewById(R.id.mainTools).getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE); // Toolbar buttons
        findViewById(R.id.mainSV).setVisibility(findViewById(R.id.mainSV).getVisibility() == View.GONE ? View.VISIBLE : View.GONE); // Toolbar SearchView
        searchView.setIconified(false); // Shows the search bar instead of the default icon

        if (searchView.getVisibility() == View.GONE) { // If SearchView is hidden, also hide the keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }

        return true;
    }

    public void styleSearch() {
        SearchView.SearchAutoComplete svText = searchView.findViewById(androidx.appcompat.R.id.search_src_text); // SearchView text field
        ImageView svClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn); // Close search button
        svText.setHintTextColor(getResources().getColor(R.color.gray)); // SearchView hint color
        svText.setTextColor(getResources().getColor(R.color.white)); // SearchView text color
        svClose.setColorFilter(ContextCompat.getColor(this, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN); // SearchView close button color
    }

    public void showSearchView(View view) { // When the search icon is pressed
        toggleSearchView(); // Show search layout
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        database.close();
    }
}