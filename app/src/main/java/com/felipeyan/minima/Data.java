package com.felipeyan.minima;

import android.content.Context;

import java.util.ArrayList;

public class Data {
    Database database;
    UserPreferences preferences;

    public ArrayList<String> noteIds, noteTexts, noteMods;

    public Data(Context context) {
        this.database = new Database(context);
        this.preferences = new UserPreferences(context);

        getNoteIds();
        getNoteTexts();
        getNoteMods();
    }

    public ArrayList<String> getNoteIds() {
        noteIds = database.getAllData(database.ID, preferences.getListOrder());
        return noteIds;
    }

    public ArrayList<String> getNoteTexts() {
        noteTexts = database.getAllData(database.NOTE, preferences.getListOrder());
        return noteTexts;
    }

    public ArrayList<String> getNoteMods() {
        noteMods = database.getAllData(database.MOD_DATE, preferences.getListOrder());
        return noteMods;
    }

    public void setNoteIds(ArrayList<String> noteIds) {
        this.noteIds = noteIds;
    }

    public void setNoteTexts(ArrayList<String> noteTexts) {
        this.noteTexts = noteTexts;
    }

    public void setNoteMods(ArrayList<String> noteMods) {
        this.noteMods = noteMods;
    }
}
