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
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    Export export;
    DialogMenus dialogMenus;
    UserPreferences preferences;
    ViewStyler viewStyler;

    Context context;
    AppCompatTextView settingsTitle;
    RecyclerView settingsRV;

    int reqCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        export = new Export(this);
        dialogMenus = new DialogMenus(this);
        preferences = new UserPreferences(this);
        viewStyler = new ViewStyler(this);

        context = this;
        settingsTitle = findViewById(R.id.settingsTitle);
        settingsRV = findViewById(R.id.settingsRV);
        settingsRV.setAdapter(new Adapter(context, "settings"));
    }

    @Override
    protected void onStart() {
        super.onStart();

        viewStyler.changeAppFont();
        viewStyler.changeViewFont(settingsTitle);
    }

    public String[] getSettings() {
        return getResources().getStringArray(R.array.settings);
    }

    public View.OnClickListener settingsItemClick(int item) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (item) {
                    case 0:
                        showPinDialog();
                        break;
                    case 1:
                        dialogMenus.preferenceSingleChoiceMenu(preferences.FONT_SIZE,
                            R.array.font_size, R.string.choose_font_size, R.string.changed_font_size, false);
                        break;
                    case 2:
                        dialogMenus.preferenceSingleChoiceMenu(preferences.FONT_FAMILY,
                            R.array.fonts, R.string.choose_font, R.string.changed_font, true);
                        break;
                    case 3:
                        dialogMenus.preferenceSingleChoiceMenu(preferences.DATE_TIME_FORMAT,
                                R.array.date_time_formats, R.string.date_time_formats, R.string.changed_date_time_format, false);
                        break;
                    case 4:
                        dialogMenus.popupMenu(view, R.menu.export_menu, new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                String itemTitle = item.getTitle().toString();
                                String[] menuOptions = getResources().getStringArray(R.array.export_options);

                                if (itemTitle.equals(menuOptions[0])) {
                                    exportNotesOption();
                                    return true;
                                } else if (itemTitle.equals(menuOptions[1])) {
                                    reqCode = 1;
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.setType("application/json");
                                    resultLauncher.launch(intent);
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });
                        break;
                    case 5:
                        dialogMenus.alertBuilder(R.string.proceed, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new Database(context).deleteDatabase();
                                Toast.makeText(context, R.string.database_deleted, Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    default:
                        Toast.makeText(context, "Item pressed: " + item, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }

    public void exportNotesOption() {
        if (export.checkStoragePermission()) {
            reqCode = 0;
            resultLauncher.launch(export.exportAsJSON());
        } else {
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
    }

    public void showPinDialog() {
        Dialog dialog = dialogMenus.customLayoutDialog(R.layout.item_pin, true);

        AppCompatTextView pinOK = dialog.findViewById(R.id.settingsValidatePIN);
        AppCompatTextView pinCancel = dialog.findViewById(R.id.settingsCancelPIN);
        pinOK.setOnClickListener(validatePIN(dialog));
        pinCancel.setOnClickListener(dialogMenus.dismissDialog(dialog));

        dialog.show();
    }

    public View.OnClickListener validatePIN(Dialog dialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatEditText pinInput = dialog.findViewById(R.id.settingsPIN);

                if (pinInput.getText().toString().isEmpty()) {
                    if (preferences.getAppPIN().isEmpty()) {
                        Toast.makeText(context, R.string.empty_pin, Toast.LENGTH_SHORT).show();
                    } else {
                        dialogMenus.alertBuilder(R.string.remove_pass, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                preferences.removePreference(preferences.APP_PIN);
                                Toast.makeText(context, R.string.removed_pass, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                } else if (pinInput.getText().toString().length() < 3) {
                    Toast.makeText(context, R.string.short_pin, Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        preferences.storeEncryptedPreference(preferences.APP_PIN, pinInput.getText().toString());
                        Toast.makeText(context, R.string.stored_pin, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 1:
                    reqCode = 0;
                    resultLauncher.launch(export.exportAsJSON());
                    break;
            }
        } else {
            switch (requestCode) {
                case 1:
                    Toast.makeText(context, R.string.storage_permission, Toast.LENGTH_SHORT).show(); // Display the error message
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("function", "updateList");
        setResult(RESULT_OK, intent);
        finish();
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                switch (reqCode) {
                    case 0:
                        if (result.getResultCode() == RESULT_OK) {
                            try {
                                export.writeInFile(Objects.requireNonNull(result.getData()).getData(), export.formatJSONFile());
                                Toast.makeText(context, R.string.exported_file, Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        } else if (result.getResultCode() != RESULT_CANCELED) {
                            Toast.makeText(context, R.string.error_exporting_file, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        if (result.getResultCode() == RESULT_OK) {
                            Database database = new Database(context);
                            Encryption encryption = new Encryption();
                            Uri filePath = Objects.requireNonNull(result.getData()).getData();
                            ArrayList<String> restoredNotes = export.getJSONArrayData(filePath, "note");
                            ArrayList<String> restoredMods = export.getJSONArrayData(filePath, "mod_date");

                            for (int i = 0; i < restoredMods.size(); i++) {
                                try {
                                    String encryptedNote = encryption.encrypt(restoredNotes.get(i), preferences.getEncryptedPreference(preferences.APP_PASSWORD, false));
                                    database.insertData(encryptedNote, restoredMods.get(i));
                                } catch (Exception e) {
                                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            Toast.makeText(context, R.string.restored_data_from_json, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    );
}
