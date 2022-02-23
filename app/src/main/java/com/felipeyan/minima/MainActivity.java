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
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Database database = new Database(this);
    Preferences preferences;
    DialogMenus dialogMenus;

    ArrayList<String> noteIDS, noteTEXTS, noteMOD;
    AppCompatTextView mainTitle, orderText;
    AppCompatImageView orderIcon;
    NoteAdapter noteAdapter;
    RecyclerView recyclerView;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);
        dialogMenus = new DialogMenus(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        searchView = findViewById(R.id.mainSV);
        recyclerView = findViewById(R.id.mainRV);
        mainTitle = findViewById(R.id.mainTitle);
        orderText = findViewById(R.id.orderText);
        orderIcon = findViewById(R.id.orderIcon);

        preferences.changeAppFont(); // Changes the Activity text font to the stored value
        preferences.changeViewFont(mainTitle, orderText); // Changes toolbar title and listing order indication text font
        preferences.changeOrderIcon(orderIcon); // Changes the listing order indication icon based on user preference

        styleSearch(); // Stylize SearchView
        verifyPassword(); // Calls the function that checks the stored password
        listNotes(preferences.getData("listOrder")); // Calls the RecyclerView creation function

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
        dialogMenus.popupMenu(view, R.menu.main_menu, new menuClick(getApplicationContext()));
    }

    public void changeOrder(View view) { // Action that changes the order of the note list
        // Checks the value stored in preferences
        switch (preferences.getData("listOrder")) {
            case "DESC": default: // If the value is DESC (system default)
                preferences.storeData("listOrder", "ASC"); // Stores the new value (ASC)
                Toast.makeText(this, R.string.list_order_asc, Toast.LENGTH_SHORT).show(); // Display the modification message
                listNotes("ASC"); // Recreates RecyclerView with the new display order (ascending)
                break;
            case "ASC": // If the value is ASC
                preferences.storeData("listOrder", "DESC"); // Stores the new value (DESC)
                Toast.makeText(this, R.string.list_order_desc, Toast.LENGTH_SHORT).show(); // Display the modification message
                listNotes("DESC"); // Recreates RecyclerView with the new display order (descending)
                break;
        }

        preferences.changeOrderIcon(orderIcon); // Displays the icon corresponding to the option
    }

    public static class menuClick implements PopupMenu.OnMenuItemClickListener {
        Context context;

        public menuClick(Context context) {
            this.context = context;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            String menuTitle = item.getTitle().toString();

            if (menuTitle.equals(context.getString(R.string.menu_settings))) {
                MainActivity.openSettings(context);
                return true;
            } else if (menuTitle.equals(context.getString(R.string.menu_about))) {
                MainActivity.openAbout(context);
                return true;
            } else {
                return false;
            }
        }
    }

    public void listNotes(String order) { // Creates the RecyclerView that displays the notes saved in the database
        // The string "order" indicates the order of values to the database (ASC = ascending, DESC = descending)
        noteIDS = database.getAllData("id", order); // Collects all note IDS
        noteTEXTS = database.getAllData("note", order); // Collect all note texts
        noteMOD = database.getAllData("mod_date", order); // Collects all last modified dates

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
        if (preferences.getData("userPw").isEmpty()) {
            try {
                preferences.storeEncryptedData("userPw", UUID.randomUUID().toString().replaceAll("-", "").substring(0, 11));
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean toggleSearchView() { // Hides the other Toolbar Views and displays the SearchView
        changeViewVisibility(findViewById(R.id.mainTitle), false); // Toolbar title
        changeViewVisibility(findViewById(R.id.mainTools), false); // Toolbar buttons
        changeViewVisibility(findViewById(R.id.mainSV), true); // Toolbar SearchView
        searchView.setIconified(false); // Shows the search bar instead of the default icon

        if (searchView.getVisibility() == View.GONE) { // If SearchView is hidden, also hide the keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }

        return true;
    }

    public void changeViewVisibility(View view, boolean reverse) {
        view.setVisibility(!reverse ? view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE : view.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
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
    public void onBackPressed() { // Display the error message
        Toast.makeText(this, R.string.cant_close_screen, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        database.close();
    }
}