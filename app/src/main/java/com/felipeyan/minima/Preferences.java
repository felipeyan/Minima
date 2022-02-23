package com.felipeyan.minima;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class Preferences {
    Encryption encryption = new Encryption();

    Context context;
    SharedPreferences preferences;

    public Preferences(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("userPref", MODE_PRIVATE);
    }

    protected void storeExtra(Intent intent, String[] key, String[] value) {
        for (int i = 0; i < key.length; i++) {
            intent.putExtra(key[i], value[i]);
        }

        context.startActivity(intent);
    }

    protected String getData(String preference) {
        return preferences.getString(preference, "");
    }

    protected String getEncryptedData(String preference, boolean usingPassword) throws Exception {
        if (usingPassword) {
            return encryption.decrypt(getData(preference), encryption.decrypt(getData("userPw"), ""));
        } else {
            return encryption.decrypt(getData(preference), "");
        }
    }

    protected void storeData(String preference, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(preference, value);
        editor.apply();
    }

    protected void storeEncryptedData(String preference, String value) throws Exception {
        SharedPreferences.Editor editor = preferences.edit();

        if (getData("userPw").isEmpty()) {
            editor.putString(preference, encryption.encrypt(value, ""));
        } else {
            editor.putString(preference, encryption.encrypt(value, getEncryptedData("userPw", false)));
        }

        editor.apply();
    }

    protected void removeData(String preference) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(preference);
        editor.apply();
    }

    // Returns the index of a value in a string-array
    protected int getStringArrayIndex(String[] array, String preferenceName) {
        String storedValue = preferences.getString(preferenceName, ""); // Stores the value saved in preferences

        if (storedValue.isEmpty()) {
            switch (preferenceName) {
                case "userFont": // Font default value
                    storedValue = "Lato";
                    break;
                case "fontSize": // Font size default value (Medium)
                    storedValue = context.getResources().getStringArray(R.array.font_size)[1];
                    break;
                case "dateTimeFormat": // Default date and time format value
                    storedValue = "12/31/2021 12h59";
                    break;
            }
        }

        int arrayPos;

        for (arrayPos = 0; arrayPos < array.length; arrayPos++) {
            if (array[arrayPos].equals(storedValue)) {
                return arrayPos;
            }
        }

        return arrayPos;
    }

    protected int getFontResource() {
        String font = getData("userFont"); // Receives the font stored in SharedPreferences

        if (font.isEmpty()) font = "Lato"; // If there is no value stored choose "Lato" as the default font

        return context.getResources().getIdentifier(font.toLowerCase().replaceAll(" ", "_"), "font", context.getPackageName());
    }

    protected void changeAppFont() {
        String storedFont = getData("userFont"); // Stored font
        String fontResource = "fontLato"; // Default app font

        // If the stored font is not empty, concatenate "font" with the name of the stored font
        if (!storedFont.isEmpty()) fontResource = "font" + storedFont.replaceAll(" ", "");

        // Apply font style by collecting R.style resource
        context.getTheme().applyStyle(context.getResources().getIdentifier(fontResource, "style", context.getPackageName()), true);
    }

    protected void changeViewFont(View... viewID) { // Modify the font of a view
        for (View view : viewID) { // For each View received
            switch (view.getClass().toString()) { // Type of View to be modified
                case "class androidx.appcompat.widget.AppCompatTextView": // AppCompatTextView
                    AppCompatTextView textView = (AppCompatTextView) view; // Stores the View in the correct element
                    textView.setTypeface(ResourcesCompat.getFont(context, getFontResource())); // Modifies View font
                    break;
                case "class androidx.appcompat.widget.AppCompatEditText": // AppCompatEditText
                    AppCompatEditText editText = (AppCompatEditText) view; // Stores the View in the correct element
                    editText.setTypeface(ResourcesCompat.getFont(context, getFontResource())); // Modifies View font
                    break;
            }
        }
    }

    public void changeOrderIcon(View view) {
        AppCompatImageView orderIcon = (AppCompatImageView) view;

        switch (getData("listOrder")) {
            case "DESC": default:
                orderIcon.setImageResource(R.drawable.ic_down);
                break;
            case "ASC":
                orderIcon.setImageResource(R.drawable.ic_up);
                break;
        }
    }

    public int getFontSize() {
        int fontPreference = getStringArrayIndex(context.getResources().getStringArray(R.array.font_size), "fontSize");

        switch (fontPreference) {
            case 0: // Small
                return 14;
            case 1: default: // Medium
                return 18;
            case 2: // Large
                return 21;
        }
    }

    // Formats the date and time received from the database to the preferred format
    public String dateTimeDisplay(String dateTime) {
        // Value Received from Database: 20211231125900 (Year, Month, Day, Hours, Minutes, Seconds)
        String formattedValue = "";

        switch (getData("dateTimeFormat")) {
            case "12/31/2021 12h59": default:
                formattedValue =
                        dateTime.substring(4, 6) + "/" + // Month
                        dateTime.substring(6, 8) + "/" + // Day
                        dateTime.substring(0, 4) + " "; // Year
                break;
            case "31/12/2021 12h59":
                formattedValue =
                        dateTime.substring(6, 8) + "/" + // Day
                        dateTime.substring(4, 6) + "/" + // Month
                        dateTime.substring(0, 4) + " "; // Year
                break;
            case "2021/12/31 12h59":
                formattedValue =
                        dateTime.substring(0, 4) + "/" + // Year
                        dateTime.substring(4, 6) + "/" + // Month
                        dateTime.substring(6, 8) + " "; // Day
                break;
        }

        formattedValue += dateTime.substring(8, 10) + "h" + dateTime.substring(10, 12); // Time
        return formattedValue;
    }
}
