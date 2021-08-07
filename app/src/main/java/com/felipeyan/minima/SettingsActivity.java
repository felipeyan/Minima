package com.felipeyan.minima;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    Export export;
    ListView settingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        export = new Export(this);
        settingsList = findViewById(R.id.settingsLV);
        settingsList.setAdapter(new ArrayAdapter<>(this,
                R.layout.item_option, R.id.settingsTV, // List item layout
                getResources().getStringArray(R.array.settings))); // List of options

        // When an option item is pressed
        settingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 3: // Export all notes in TXT option
                        if (export.checkStoragePermission()) {  // Checks if the application has permission to store files
                            if (export.exportTXT()) { // If exported the file successfully, show a message
                                Toast.makeText(SettingsActivity.this, R.string.exported_file, Toast.LENGTH_SHORT).show();
                            } else { // Shows an error message
                                Toast.makeText(SettingsActivity.this, R.string.error_exporting_file, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[] {
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, 1); // Request storage access permission
                        }
                        break;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Checks if the result of a permission request was allowed
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 1: // Request code for storage access
                    if (export.exportTXT()) { // Checks if the file export was successful
                        Toast.makeText(SettingsActivity.this, R.string.exported_file, Toast.LENGTH_SHORT).show(); // Display a success message
                    } else { // Display the error message
                        Toast.makeText(SettingsActivity.this, R.string.error_exporting_file, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        } else {
            switch (requestCode) {
                case 1: // Request code for storage access
                    Toast.makeText(this, R.string.storage_permission, Toast.LENGTH_SHORT).show(); // Display the error message
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}