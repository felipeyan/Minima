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

    public Preferences(Context originContext) {
        context = originContext;
        preferences = context.getSharedPreferences("userPref", MODE_PRIVATE);
    }

    protected void generateDefaultPass() throws Exception {
        SharedPreferences.Editor editor = preferences.edit();
        // Encrypts and stores a string of 11 random characters
        editor.putString("userPw", encryption.encrypt(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 11), ""));
        editor.apply(); // Updates SharedPreferences
    }

    protected String getPassword() throws Exception { // Returns the stored password that encrypts the notes
        return encryption.decrypt(preferences.getString("userPw", ""), "");
    }

    protected void storePIN(String pin) throws Exception { // Store encrypted PIN received from SettingsActivity
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userPIN", encryption.encrypt(pin, getPassword()));
        editor.apply();
    }

    protected String getPIN() throws Exception { // Returns the stored PIN that locks the application
        return encryption.decrypt(preferences.getString("userPIN", ""), getPassword());
    }

    protected void storeFont(String font) { // Stores the name of the font received in SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userFont", font);
        editor.apply();
    }

    protected int getFont() { // Returns the index of the name of the font stored in the fonts list
        String[] fonts = context.getResources().getStringArray(R.array.fonts); // Receives the font list
        String storedFont = preferences.getString("userFont", ""); // Receives the font stored in SharedPreferences
        int fontPos; // Font index in the fonts list

        for (fontPos = 0; fontPos < fonts.length; fontPos++) {
            if (fonts[fontPos].equals(storedFont)) { // if the fonts are the same
                return fontPos; // Return the corresponding position in the list
            } // Keep adding 1 to the fontPos variable until find the correct index
        }

        return fontPos;
    }

    protected void storeOrder(String order) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("listOrder", order);
        editor.apply();
    }

    public void changeAppFont(Context context) { // Modifies the style of the fontFamily property in the app theme
        String font = preferences.getString("userFont", ""); // Receives the font stored in SharedPreferences

        switch (font) { // Applies the style corresponding to the stored font name
            case "Crimson Text":
                context.getTheme().applyStyle(R.style.fontCrimsonText, true);
                break;
            case "Indie Flower":
                context.getTheme().applyStyle(R.style.fontIndieFlower, true);
                break;
            case "Josefin Sans":
                context.getTheme().applyStyle(R.style.fontJosefinSans, true);
                break;
            case "Lato": default: // Default font style
                context.getTheme().applyStyle(R.style.fontLato, true);
                break;
            case "Nunito":
                context.getTheme().applyStyle(R.style.fontNunito, true);
                break;
            case "Open Sans":
                context.getTheme().applyStyle(R.style.fontOpenSans, true);
                break;
            case "Roboto":
                context.getTheme().applyStyle(R.style.fontRoboto, true);
                break;
            case "Ubuntu":
                context.getTheme().applyStyle(R.style.fontUbuntu, true);
                break;
        }
    }

    public void changeViewFont(String type, View view) { // Modify the font of a specific view
        String font = preferences.getString("userFont", ""); // Receives the font stored in SharedPreferences

        if (font.isEmpty()) { // If there is no value stored
            font = "Lato"; // Choose Lato as the default font
        }

        // Gets the resource in R.font corresponding to the stored font (lowercase and no spaces)
        int fontID = context.getResources().getIdentifier(font.toLowerCase().replaceAll(" ", "_"), "font", context.getPackageName());

        switch (type) { // Type of View to be modified
            case "TextView": // AppCompatTextView
                AppCompatTextView textView = (AppCompatTextView) view; // Stores the View in the correct element
                textView.setTypeface(ResourcesCompat.getFont(context, fontID)); // Modifies View font
                break;
            case "EditText": // AppCompatEditText
                AppCompatEditText editText = (AppCompatEditText) view; // Stores the View in the correct element
                editText.setTypeface(ResourcesCompat.getFont(context, fontID)); // Modifies View font
                break;
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
}
