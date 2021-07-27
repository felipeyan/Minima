package com.felipeyan.minima;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class Preferences {
    Encryption encryption = new Encryption();

    Context context;
    SharedPreferences preferences;

    public Preferences(Context originContext) {
        context = originContext;
        preferences = context.getSharedPreferences("userPref", Context.MODE_PRIVATE);
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
}
