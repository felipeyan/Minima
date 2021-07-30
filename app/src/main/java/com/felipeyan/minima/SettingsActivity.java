package com.felipeyan.minima;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    ListView settingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsList = findViewById(R.id.settingsLV);
        settingsList.setAdapter(new ArrayAdapter<>(this,
                R.layout.item_option, R.id.settingsTV, // List item layout
                getResources().getStringArray(R.array.settings))); // List of options

        // When an option item is pressed
        settingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 5: // Delete database option
                        deleteDatabase(); // Calls the dialog function to delete the database
                        break;
                    default:
                        Toast.makeText(SettingsActivity.this, "Item pressed: " + position, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    public void deleteDatabase() {
        new AlertDialog.Builder(SettingsActivity.this)
            .setMessage(R.string.proceed)
            .setCancelable(true)
            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss(); // Closes the dialog
                }
            })
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Database(SettingsActivity.this).deleteDatabase();
                    Toast.makeText(SettingsActivity.this, R.string.database_deleted, Toast.LENGTH_SHORT).show();
                }
            }).show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}