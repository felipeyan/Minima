package com.felipeyan.minima;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        recyclerView = findViewById(R.id.mainRV);
    }

    public void openMenu(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenu().add(R.string.menu_settings);
        menu.getMenu().add(R.string.menu_about);

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

    public void addNote(View view) {
        startActivity(new Intent(this, NoteActivity.class));
    }

    public void openSettings() {
        Toast.makeText(this, R.string.menu_settings, Toast.LENGTH_SHORT).show();
    }

    public void openAbout() {
        Toast.makeText(this, R.string.menu_about, Toast.LENGTH_SHORT).show();
    }
}