package com.felipeyan.minima;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class UserPreferences {
    Encryption encryption;

    public static final String
        PREFERENCES = "userPref",
        APP_PASSWORD = "appPassword", APP_PIN = "appPIN", DATE_TIME_FORMAT = "dateTimeFormat",
        FONT_FAMILY = "fontFamily", FONT_SIZE = "fontSize", LIST_ORDER = "listOrder";

    public String appPassword;
    public String appPIN;
    public String dateTimeFormat;
    public String fontSize;
    public String fontFamily;
    public String listOrder;

    Context context;
    SharedPreferences preferences;

    public UserPreferences(Context context) {
        encryption = new Encryption();
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        this.context = context;

        setAppPassword(getAppPassword());
        setAppPIN(getAppPIN());
        setDateTimeFormat(getDateTimeFormat());
        setFontSize(getFontSize());
        setFontFamily(getFontFamily());
        setListOrder(getListOrder());
    }

    public void storePreference(String preference, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(preference, value);
        editor.apply();
    }

    public String getPreference(String preference) {
        return preferences.getString(preference, "");
    }

    public void storeEncryptedPreference(String preference, String value) {
        SharedPreferences.Editor editor = preferences.edit();

        if (getAppPassword().isEmpty()) {
            try {
                editor.putString(preference, encryption.encrypt(value, ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                editor.putString(preference, encryption.encrypt(value, getEncryptedPreference(APP_PASSWORD, false)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        editor.apply();
    }

    public String getEncryptedPreference(String preference, boolean usingPassword) {
        String decrypted = null;

        if (usingPassword) {
            try {
                decrypted = encryption.decrypt(getPreference(preference), encryption.decrypt(appPassword, ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                decrypted = encryption.decrypt(getPreference(preference), "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return decrypted;
    }

    public void removePreference(String preference) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(preference);
        editor.apply();
    }

    public int getStringArrayIndex(String preference) {
        String[] arrayPreference = new String[] {};
        int index;

        switch (preference) {
            case DATE_TIME_FORMAT:
                arrayPreference = context.getResources().getStringArray(R.array.date_time_formats);
                break;
            case FONT_FAMILY:
                arrayPreference = context.getResources().getStringArray(R.array.fonts);
                break;
            case FONT_SIZE:
                arrayPreference = context.getResources().getStringArray(R.array.font_size);
                break;
        }

        for (index = 0; index < arrayPreference.length; index++) {
            if (arrayPreference[index].equals(getPreference(preference))) return index;
        }

        return index;
    }

    public int fontSizeToSP() {
        int fontSize = 0;

        switch (getStringArrayIndex(FONT_SIZE)) {
            case 0:
                fontSize = 14;
                break;
            case 1:
                fontSize = 18;
                break;
            case 2:
                fontSize = 21;
                break;
        }

        return fontSize;
    }

    public int getFontResource() {
        return context.getResources().getIdentifier(fontFamily.toLowerCase().replaceAll(" ", "_"), "font", context.getPackageName());
    }

    public void setAppPassword(String appPassword) {
        this.appPassword = appPassword;
    }

    public void setAppPIN(String appPIN) {
        this.appPIN = appPIN;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public void setListOrder(String listOrder) {
        this.listOrder = listOrder;
    }

    public String getAppPassword() {
        if (getPreference(APP_PASSWORD).isEmpty()) try {
            storePreference(APP_PASSWORD, encryption.encrypt(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 11), ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.appPassword = getPreference(APP_PASSWORD);
        return appPassword;
    }

    public String getAppPIN() {
        this.appPIN = getPreference(APP_PIN);
        return appPIN;
    }

    public String getDateTimeFormat() {
        if (getPreference(DATE_TIME_FORMAT).isEmpty()) storePreference(DATE_TIME_FORMAT, context.getResources().getStringArray(R.array.date_time_formats)[0]);
        this.dateTimeFormat = getPreference(DATE_TIME_FORMAT);
        return dateTimeFormat;
    }

    public String getFontFamily() {
        if (getPreference(FONT_FAMILY).isEmpty()) storePreference(FONT_FAMILY, context.getResources().getStringArray(R.array.fonts)[3]);
        this.fontFamily = getPreference(FONT_FAMILY);
        return fontFamily;
    }

    public String getFontSize() {
        if (getPreference(FONT_SIZE).isEmpty()) storePreference(FONT_SIZE, context.getResources().getStringArray(R.array.font_size)[1]);
        this.fontSize = getPreference(FONT_SIZE);
        return fontSize;
    }

    public String getListOrder() {
        if (getPreference(LIST_ORDER).isEmpty()) storePreference(LIST_ORDER, "DESC");
        this.listOrder = getPreference(LIST_ORDER);
        return listOrder;
    }
}
