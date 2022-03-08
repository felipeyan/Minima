package com.felipeyan.minima;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Database database;
    DialogMenus dialogMenus;
    UserPreferences preferences;
    ViewStyler viewStyler;

    FloatingActionButton mainFAB;
    NestedScrollView mainScroll;
    AppCompatTextView mainTitle, orderText;
    AppCompatImageView orderIcon;
    Adapter adapter;
    Pages pages;
    RecyclerView recyclerView;
    SearchView searchView;

    public int requestCode, currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = new Database(this);
        dialogMenus = new DialogMenus(this);
        pages = new Pages(this);
        preferences = new UserPreferences(this);
        viewStyler = new ViewStyler(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mainFAB = findViewById(R.id.mainFAB);
        mainScroll = findViewById(R.id.mainScroll);
        searchView = findViewById(R.id.mainSV);
        recyclerView = findViewById(R.id.mainRV);
        mainTitle = findViewById(R.id.mainTitle);
        orderText = findViewById(R.id.orderText);
        orderIcon = findViewById(R.id.orderIcon);

        mainScroll.setOnScrollChangeListener(scrollListener());
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
                if (newText.length() > 3) adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    public void openMenu(View view) {
        requestCode = 1;
        resultLauncher.launch(new Intent(this, SettingsActivity.class));
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

        viewStyler.changeOrderIcon(orderIcon); // Displays the icon corresponding to the option
        listNotes();
    }

    public void listNotes() { // Creates the RecyclerView that displays the notes saved in the database
        pages.startValues(currentPage);
        pages.displayPages();
        adapter = new Adapter(this, "notes");
        recyclerView.setAdapter(adapter);
    }

    public void addNote(View view) { // Launches the "Note" screen
        startActivity(new Intent(this, NoteActivity.class));
    }

    public void openAbout(Context context) { // Launches the "About" screen
        Toast.makeText(context, R.string.menu_about, Toast.LENGTH_SHORT).show();
    }

    public boolean toggleSearchView() { // Hides the other Toolbar Views and displays the SearchView
        viewStyler.changeViewVisibility(findViewById(R.id.pagesRV), false);
        viewStyler.changeViewVisibility(findViewById(R.id.mainTitle), false); // Toolbar title
        viewStyler.changeViewVisibility(findViewById(R.id.mainTools), false); // Toolbar buttons
        viewStyler.changeViewVisibility(findViewById(R.id.mainSV), true); // Toolbar SearchView
        searchView.setIconified(false); // Shows the search bar instead of the default icon

        if (searchView.getVisibility() == View.GONE) { // If SearchView is hidden, also hide the keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            listNotes();
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

    public void showSearchView(View view) {
        toggleSearchView(); }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, R.string.cant_close_screen, Toast.LENGTH_SHORT).show();
    }

    public NestedScrollView.OnScrollChangeListener scrollListener() {
        return new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (!mainScroll.canScrollVertically(1)) {
                    mainFAB.setVisibility(View.GONE);
                } else {
                    mainFAB.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                switch (requestCode) {
                    case 1:
                        if (result.getResultCode() == RESULT_OK) {
                            switch (Objects.requireNonNull(result.getData()).getStringExtra("function")) {
                                case "updateList":
                                    currentPage = 0;
                                    listNotes();
                                    break;
                            }
                        }
                        break;
                }
            }
    });

    @Override
    protected void onDestroy () {
        super.onDestroy();
        database.close();
    }
}