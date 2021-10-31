 package com.felipeyan.minima;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.app.Activity;
import android.content.Context;
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
    AppCompatTextView mainTitle, orderText;
    AppCompatImageView orderIcon;
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
        orderText = findViewById(R.id.orderText);
        orderIcon = findViewById(R.id.orderIcon);

        // Changes the Activity text font to the stored value
        new Preferences(this).changeAppFont(this);
        // Changes toolbar title font
        new Preferences(this).changeViewFont("TextView", mainTitle);
        // Changes the listing order indication text
        new Preferences(this).changeViewFont("TextView", orderText);
        // Changes the listing order indication icon based on user preference
        new Preferences(this).changeOrderIcon(orderIcon);

        styleSearch(); // Stylize SearchView
        verifyPassword(); // Calls the function that checks the stored password
        listNotes(getSharedPreferences("userPref", MODE_PRIVATE).getString("listOrder", "")); // Calls the RecyclerView creation function

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
        menu.getMenuInflater().inflate(R.menu.main_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(new menuClick(getApplicationContext()));
        menu.show();
    }

    public void changeOrder(View view) { // Action that changes the order of the note list
        // Checks the value stored in preferences
        switch (getSharedPreferences("userPref", MODE_PRIVATE).getString("listOrder", "")) {
            case "DESC": default: // If the value is DESC (system default)
                new Preferences(this).storeOrder("ASC"); // Stores the new value (ASC)
                Toast.makeText(this, R.string.list_order_asc, Toast.LENGTH_SHORT).show(); // Display the modification message
                listNotes("ASC"); // Recreates RecyclerView with the new display order (ascending)
                break;
            case "ASC": // If the value is ASC
                new Preferences(this).storeOrder("DESC"); // Stores the new value (DESC)
                Toast.makeText(this, R.string.list_order_desc, Toast.LENGTH_SHORT).show(); // Display the modification message
                listNotes("DESC"); // Recreates RecyclerView with the new display order (descending)
                break;
        }

        new Preferences(this).changeOrderIcon(orderIcon); // Displays the icon corresponding to the option
    }

    public static class menuClick implements PopupMenu.OnMenuItemClickListener {
        Context context;

        public menuClick(Context context) {
            this.context = context;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getTitle().toString().equals(context.getString(R.string.menu_settings))) {
                MainActivity.openSettings(context);
                return true;
            } else if (item.getTitle().toString().equals(context.getString(R.string.menu_about))) {
                MainActivity.openAbout(context);
                return true;
            } else {
                return false;
            }
        }
    }

    public void listNotes(String order) { // Creates the RecyclerView that displays the notes saved in the database
        // The string "order" indicates the order of values to the database (ASC = ascending, DESC = descending)
        noteIDS = database.getAllData(this, "id", order); // Collects all note IDS
        noteTEXTS = database.getAllData(this, "note", order); // Collect all note texts
        noteMOD = database.getAllData(this, "mod_date", order); // Collects all last modified dates

        noteAdapter = new NoteAdapter(this, noteIDS, noteTEXTS, noteMOD);
        recyclerView.setAdapter(noteAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void addNote(View view) { // Launches the "Note" screen
        startActivity(new Intent(this, NoteActivity.class));
    }

    public static void openSettings(Context context) { // Launches the "Settings" screen
        context.startActivity(new Intent(context, SettingsActivity.class));
    }

    public static void openAbout(Context context) { // Launches the "About" screen
        Toast.makeText(context, R.string.menu_about, Toast.LENGTH_SHORT).show();
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