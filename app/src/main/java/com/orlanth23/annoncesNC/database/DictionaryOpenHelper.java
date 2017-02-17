package com.orlanth23.annoncesnc.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DictionaryOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "annoncesNCSQLite.db";

    private static final String DICTIONARY_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DictionaryDAO.Dictionary.TABLE_NAME;

    private static final String DICTIONARY_TABLE_CREATE =
            "CREATE TABLE " + DictionaryDAO.Dictionary.TABLE_NAME + " (" +
                    DictionaryDAO.Dictionary.COLUMN_CLEF + " TEXT, " +
                    DictionaryDAO.Dictionary.COLUMN_CHAMP + " TEXT);";

    DictionaryOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DICTIONARY_DELETE_ENTRIES);
        onCreate(db);
    }
}
