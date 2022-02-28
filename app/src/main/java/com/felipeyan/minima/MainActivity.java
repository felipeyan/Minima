package com.felipeyan.minima;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
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

public class MainActivity extends AppCompatActivity {
    Database database;
    DialogMenus dialogMenus;
    UserPreferences preferences;
    ViewStyler viewStyler;

    AppCompatTextView mainTitle, orderText;
    AppCompatImageView orderIcon;
    Adapter adapter;
    RecyclerView recyclerView;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = new Database(this);
        dialogMenus = new DialogMenus(this);
        preferences = new UserPreferences(this);
        viewStyler = new ViewStyler(this);

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

        viewStyler.changeAppFont(); // Changes the Activity text font to the stored value
        viewStyler.changeViewFont(mainTitle, orderText); // Changes toolbar title and listing order indication text font
        viewStyler.changeOrderIcon(orderIcon); // Changes the listing order indication icon based on user preference

        styleSearch(); // Stylize SearchView
        listNotes(); // Calls the RecyclerView creation function

        searchView.setOnCloseListener(this::toggleSearchView); // When the close search button is pressed
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    public void openMenu(View view) { // Displays the menu after clicking the 3-dot icon
        dialogMenus.popupMenu(view, R.menu.main_menu, menuClick());
    }

    public void changeOrder(View view) { // Action that changes the order of the note list
        // Checks the value stored in preferences
        switch (preferences.getListOrder()) {
            case "DESC": default: // If the value is DESC (system default)
                preferences.storePreference(preferences.LIST_ORDER, "ASC"); // Stores the new value (ASC)
                Toast.makeText(this, R.string.list_order_asc, Toast.LENGTH_SHORT).show(); // Display the modification message
                break;
            case "ASC": // If the value is ASC
                preferences.storePreference(preferences.LIST_ORDER, "DESC"); // Stores the new value (DESC)
                Toast.makeText(this, R.string.list_order_desc, Toast.LENGTH_SHORT).show(); // Display the modification message
                break;
        }

        listNotes();
        viewStyler.changeOrderIcon(orderIcon); // Displays the icon corresponding to the option
    }

    public PopupMenu.OnMenuItemClickListener menuClick() {
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String menuTitle = item.getTitle().toString();
                String[] menuOptions = getResources().getStringArray(R.array.main);

                if (menuTitle.equals(menuOptions[0])) {
                    openSettings(MainActivity.this);
                    return true;
                } else if (menuTitle.equals(menuOptions[1])) {
                    openAbout(MainActivity.this);
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    public void listNotes() { // Creates the RecyclerView that displays the notes saved in the database
        new Data(this);
        adapter = new Adapter(this, "notes");
        recyclerView.setAdapter(adapter);
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

    public boolean toggleSearchView() { // Hides the other Toolbar Views and displays the SearchView
        viewStyler.changeViewVisibility(findViewById(R.id.mainTitle), false); // Toolbar title
        viewStyler.changeViewVisibility(findViewById(R.id.mainTools), false); // Toolbar buttons
        viewStyler.changeViewVisibility(findViewById(R.id.mainSV), true); // Toolbar SearchView
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
    public void onBackPressed() { // Display the error message
        Toast.makeText(this, R.string.cant_close_screen, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        database.close();
    }
}