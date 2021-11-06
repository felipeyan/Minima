package com.felipeyan.minima;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

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

    protected void generateDefaultPass() throws Exception {
        SharedPreferences.Editor editor = preferences.edit();
        // Encrypts and stores a string of 11 random characters
        editor.putString("userPw", encryption.encrypt(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 11), ""));
        editor.apply(); // Updates SharedPreferences
    }

    protected void storePIN(String pin) throws Exception { // Store encrypted PIN received from SettingsActivity
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userPIN", encryption.encrypt(pin, getPassword()));
        editor.apply();
    }

    protected void storeFont(String font) { // Stores the name of the font received in SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userFont", font);
        editor.apply();
    }

    protected String getPassword() throws Exception { // Returns the stored password that encrypts the notes
        return encryption.decrypt(preferences.getString("userPw", ""), "");
    }

    protected void storeOrder(String order) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("listOrder", order);
        editor.apply();
    }

    protected void storeDateTimeFormat(String dateTimeFormat) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("dateTimeFormat", dateTimeFormat);
        editor.apply();
    }

    protected String getPIN() throws Exception { // Returns the stored PIN that locks the application
        return encryption.decrypt(preferences.getString("userPIN", ""), getPassword());
    }

    protected void removePIN() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("userPIN");
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
        String font = preferences.getString("userFont", ""); // Receives the font stored in SharedPreferences

        if (font.isEmpty()) font = "Lato"; // If there is no value stored choose "Lato" as the default font

        return context.getResources().getIdentifier(font.toLowerCase().replaceAll(" ", "_"), "font", context.getPackageName());
    }

    protected void changeAppFont() {
        String storedFont = preferences.getString("userFont", ""); // Stored font
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

        switch (preferences.getString("listOrder", "")) {
            case "DESC": default:
                orderIcon.setImageResource(R.drawable.ic_down);
                break;
            case "ASC":
                orderIcon.setImageResource(R.drawable.ic_up);
                break;
        }
    }

    // Formats the date and time received from the database to the preferred format
    public String dateTimeDisplay(String dateTime) {
        // Value Received from Database: 20211231125900 (Year, Month, Day, Hours, Minutes, Seconds)
        String formattedValue = "";

        switch (preferences.getString("dateTimeFormat", "")) {
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
