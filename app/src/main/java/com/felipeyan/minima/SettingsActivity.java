package com.felipeyan.minima;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    Context context;
    Export export;
    AppCompatTextView settingsTitle;
    ListView settingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        context = this;
        export = new Export(this);
        settingsTitle = findViewById(R.id.settingsTitle);
        settingsList = findViewById(R.id.settingsLV);
        settingsList.setAdapter(new ArrayAdapter<>(this,
                R.layout.item_option, R.id.settingsTV, // List item layout
                getResources().getStringArray(R.array.settings))); // List of options

        settingsList.setOnItemClickListener(new settingsItemClick(this)); // When an option item is pressed
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Changes the Activity text font to the stored value
        new Preferences(this).changeAppFont();
        // Changes toolbar title font
        new Preferences(this).changeViewFont(settingsTitle);
    }

    public class settingsItemClick implements AdapterView.OnItemClickListener {
        Activity activity;
        Export export;

        public settingsItemClick(Activity activity) {
            this.activity = activity;

            export = new Export(activity);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0: // Set PIN password option
                    showPinDialog(); // Shows the PIN configuration dialog
                    break;
                case 1: // Option to choose the app's font
                    String[] fonts = getResources().getStringArray(R.array.fonts); // Stores the fonts list

                    new singleChoiceMenu(context, R.string.choose_font, fonts, new Preferences(context).getStringArrayIndex(fonts, "userFont"), new singleChoiceMenuClick(context, fonts, "fontMenu"));
                    break;
                case 2: // Export all notes in TXT option
                    if (export.checkStoragePermission()) {  // Checks if the application has permission to store files
                        if (export.exportTXT()) { // If exported the file successfully, show a message
                            Toast.makeText(activity, R.string.exported_file, Toast.LENGTH_SHORT).show();
                        } else { // Shows an error message
                            Toast.makeText(activity, R.string.error_exporting_file, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        ActivityCompat.requestPermissions(activity, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, 1); // Request storage access permission
                    }
                    break;
                case 3: // Option to choose the date and time format
                    String[] dateTimeFormats = getResources().getStringArray(R.array.date_time_formats); // Stores list of date and time formats

                    new singleChoiceMenu(context, R.string.date_time_formats, dateTimeFormats, new Preferences(context).getStringArrayIndex(dateTimeFormats, "dateTimeFormat"), new singleChoiceMenuClick(context, dateTimeFormats, "dateTimeMenu"));
                    break;
                case 4: // Delete database option
                    deleteDatabase(); // Calls the dialog function to delete the database
                    break;
                default:
                    Toast.makeText(activity, "Item pressed: " + position, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    // Single choice RadioGroup menu builder
    public static class singleChoiceMenu extends AlertDialog.Builder {
        public singleChoiceMenu(@NonNull Context context, int title, String[] items, int checkedItem, DialogInterface.OnClickListener dialogClick) {
            super(context, R.style.dialogStyle);

            setTitle(title);
            setSingleChoiceItems(items, checkedItem, dialogClick);
            show();
        }
    }

    // Click action for RadioGroup single choice menus
    public static class singleChoiceMenuClick implements DialogInterface.OnClickListener {
        Context context;
        String[] options;
        String menuName;

        public singleChoiceMenuClick(Context context, String[] options, String menuName) {
            this.context = context;
            this.options = options;
            this.menuName = menuName; // Used to indicate which menu is being called
        }

        @Override
        public void onClick(DialogInterface dialog, int i) {
            switch (menuName) {
                case "fontMenu": // Click action for font choice menu
                    new Preferences(context).storeFont(options[i]); // Stores the new font selected in preferences
                    Toast.makeText(context, R.string.changed_font, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    ((Activity) context).recreate();
                    break;
                case "dateTimeMenu": // Click action for date and time format choice menu
                    new Preferences(context).storeDateTimeFormat(options[i]); // Stores the new date and time format in preferences
                    Toast.makeText(context, R.string.changed_date_time_format, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    }

    public void deleteDatabase() {
        new AlertDialog.Builder(context)
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
                    new Database(context).deleteDatabase();
                    Toast.makeText(context, R.string.database_deleted, Toast.LENGTH_SHORT).show();
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

    public void showPinDialog() { // PIN configuration dialog
        Dialog dialog = new Dialog(SettingsActivity.this);
        dialog.setContentView(R.layout.item_pin); // Dialog layout
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); // Transparent background
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); // Show keyboard when starting dialog
        dialog.setCancelable(true); // When pressed outside the layout

        AppCompatTextView pinOK = dialog.findViewById(R.id.settingsValidatePIN); // OK
        AppCompatTextView pinCancel = dialog.findViewById(R.id.settingsCancelPIN); // Cancel
        pinOK.setOnClickListener(new validatePIN(dialog)); // When OK is pressed
        pinCancel.setOnClickListener(new dismissDialog(dialog)); // When cancel is pressed

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
                Toast.makeText(SettingsActivity.this, R.string.empty_pin, Toast.LENGTH_SHORT).show();
            } else if (pinInput.getText().toString().length() < 3) { // Displays an error message if the PIN is less than 3 characters
                Toast.makeText(SettingsActivity.this, R.string.short_pin, Toast.LENGTH_SHORT).show();
            } else { // if it's all right
                try { // Encrypts and stores the entered PIN
                    new Preferences(SettingsActivity.this).storePIN(pinInput.getText().toString());
                    Toast.makeText(SettingsActivity.this, R.string.stored_pin, Toast.LENGTH_SHORT).show(); // Display a success message
                    dialog.dismiss(); // Closes the dialog
                } catch (Exception e) { // Display the error message
                    Toast.makeText(SettingsActivity.this, e.toString(), Toast.LENGTH_SHORT).show();;
                }
            }
        }
    }

    public static class dismissDialog implements View.OnClickListener {
        Dialog dialog;

        public dismissDialog(Dialog dialog) {
            this.dialog = dialog; // Receive the dialog
        }

        @Override
        public void onClick(View v) {
            dialog.dismiss(); // Closes the dialog
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
