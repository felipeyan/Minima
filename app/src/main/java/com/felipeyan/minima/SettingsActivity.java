package com.felipeyan.minima;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;

public class SettingsActivity extends AppCompatActivity {
    Export export = new Export(this); // Files export class
    Preferences preferences; // SharedPreferences management class
    DialogMenus dialogMenus = new DialogMenus(this); // AlertDialogs class

    Context context;
    AppCompatTextView settingsTitle;
    ListView settingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        context = this;
        preferences = new Preferences(this);

        settingsTitle = findViewById(R.id.settingsTitle);
        settingsList = findViewById(R.id.settingsLV);
        settingsList.setAdapter(new ArrayAdapter<>(this,
            R.layout.item_option, R.id.settingsTV, // List item layout
            getResources().getStringArray(R.array.settings))); // List of options

        settingsList.setOnItemClickListener(new settingsItemClick()); // When an option item is pressed
    }

    @Override
    protected void onStart() {
        super.onStart();

        preferences.changeAppFont(); // Changes the Activity text font to the stored value
        preferences.changeViewFont(settingsTitle); // Changes toolbar title font
    }

    public class settingsItemClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0: // Set PIN password option
                    showPinDialog(); // Shows the PIN configuration dialog
                    break;
                case 1: // Option to choose note font size
                    dialogMenus.singleChoiceMenu(R.array.font_size, R.string.choose_font_size, "fontSize", R.string.changed_font_size, false);
                    break;
                case 2: // Option to choose the app's font
                    dialogMenus.singleChoiceMenu(R.array.fonts, R.string.choose_font, "userFont", R.string.changed_font, true);
                    break;
                case 3: // Export all notes in TXT option
                    if (export.checkStoragePermission()) {  // Checks if the application has permission to store files
                        resultLauncher.launch(export.exportAsTXT());
                    } else {
                        ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, 1); // Request storage access permission
                    }
                    break;
                case 4: // Option to choose the date and time format
                    dialogMenus.singleChoiceMenu(R.array.date_time_formats, R.string.date_time_formats, "dateTimeFormat", R.string.changed_date_time_format, false);
                    break;
                case 5: // Delete database option
                    dialogMenus.new dialogBuilder(R.string.proceed,
                        dialogMenus.new dialogClick("deleteDBPositive"));
                    break;
                default:
                    Toast.makeText(context, "Item pressed: " + position, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        Uri uri = result.getData().getData();
                        OutputStream outputStream = getContentResolver().openOutputStream(uri);
                        outputStream.write(export.getNotes().toString().getBytes());
                        outputStream.close();

                        Toast.makeText(context, R.string.exported_file, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else if (result.getResultCode() != RESULT_CANCELED) {
                    Toast.makeText(context, R.string.error_exporting_file, Toast.LENGTH_SHORT).show();
                }
            }
        }
    );

    public void showPinDialog() { // PIN configuration dialog
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.item_pin); // Dialog layout
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); // Transparent background
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); // Show keyboard when starting dialog
        dialog.setCancelable(true); // When pressed outside the layout

        AppCompatTextView pinOK = dialog.findViewById(R.id.settingsValidatePIN); // OK
        AppCompatTextView pinCancel = dialog.findViewById(R.id.settingsCancelPIN); // Cancel
        pinOK.setOnClickListener(new validatePIN(dialog)); // When OK is pressed
        pinCancel.setOnClickListener(new DialogMenus.dismissDialog(dialog)); // When cancel is pressed

        dialog.show(); // Displays the dialog
    }

    public class validatePIN implements View.OnClickListener {
        Dialog dialog;
        AppCompatEditText pinInput;

        public validatePIN(Dialog dialog) {
            this.dialog = dialog;

            pinInput = dialog.findViewById(R.id.settingsPIN);
        }

        @Override
        public void onClick(View v) {
            if (pinInput.getText().toString().isEmpty()) { // Displays an error message if the PIN is empty
                if (preferences.getData("userPIN").isEmpty()) {
                    Toast.makeText(context, R.string.empty_pin, Toast.LENGTH_SHORT).show();
                } else {
                    dialogMenus.new dialogBuilder(R.string.remove_pass, dialogMenus.new dialogClick("removePassPositive"));
                }
            } else if (pinInput.getText().toString().length() < 3) { // Displays an error message if the PIN is less than 3 characters
                Toast.makeText(context, R.string.short_pin, Toast.LENGTH_SHORT).show();
            } else { // if it's all right
                try { // Encrypts and stores the entered PIN
                    preferences.storeEncryptedData("userPIN", pinInput.getText().toString());
                    Toast.makeText(context, R.string.stored_pin, Toast.LENGTH_SHORT).show(); // Display a success message
                    dialog.dismiss(); // Closes the dialog
                } catch (Exception e) { // Display the error message
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Checks if the result of a permission request was allowed
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 1: // Request code for storage access
                    resultLauncher.launch(export.exportAsTXT());
                    break;
            }
        } else {
            switch (requestCode) {
                case 1: // Request code for storage access
                    Toast.makeText(context, R.string.storage_permission, Toast.LENGTH_SHORT).show(); // Display the error message
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
