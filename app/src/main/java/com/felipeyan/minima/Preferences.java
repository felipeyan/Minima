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

    protected int getFontResource() {
        String font = preferences.getString("userFont", ""); // Receives the font stored in SharedPreferences

        if (font.isEmpty()) font = "Lato"; // If there is no value stored choose "Lato" as the default font

        return context.getResources().getIdentifier(font.toLowerCase().replaceAll(" ", "_"), "font", context.getPackageName());
    }

    protected void storeOrder(String order) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("listOrder", order);
        editor.apply();
    }

    public void changeAppFont() {
        String storedFont = preferences.getString("userFont", ""); // Stored font
        String fontResource = "fontLato"; // Default app font

        // If the stored font is not empty, concatenate "font" with the name of the stored font
        if (!storedFont.isEmpty()) fontResource = "font" + storedFont.replaceAll(" ", "");

        // Apply font style by collecting R.style resource
        context.getTheme().applyStyle(context.getResources().getIdentifier(fontResource, "style", context.getPackageName()), true);
    }

    public void changeViewFont(View... viewID) { // Modify the font of a view
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
}
