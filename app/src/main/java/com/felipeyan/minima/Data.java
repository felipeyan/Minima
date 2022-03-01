package com.felipeyan.minima;

import android.content.Context;

import java.util.ArrayList;

public class Data {
    Database database;
    UserPreferences preferences;

    public int dataCount;
    public ArrayList<String> noteIds, noteTexts, noteMods;

    public Data(Context context) {
        this.database = new Database(context);
        this.preferences = new UserPreferences(context);

        getDataCount();
        getNoteIds();
        getNoteTexts();
        getNoteMods();
    }

    public void setDataRange(int start, int end) {
        setNoteIds(new ArrayList<>(getNoteIds().subList(start, end)));
        setNoteTexts(new ArrayList<>(getNoteTexts().subList(start, end)));
        setNoteMods(new ArrayList<>(getNoteMods().subList(start, end)));
    }

    public int getDataCount() {
        dataCount = database.getDataCount();
        return dataCount;
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
