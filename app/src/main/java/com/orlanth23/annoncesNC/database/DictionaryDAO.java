package com.orlanth23.annoncesnc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class DictionaryDAO {

    public DictionaryDAO() {
    }

    public static String getValueByKey(Context context, String clef) {
        String retour = null;
        Cursor cursor;

        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {Dictionary.COLUMN_CHAMP};

        String[] keys = {clef};

        // Récupération du login
        cursor = db.query(Dictionary.TABLE_NAME,
                columns,
                Dictionary.COLUMN_CLEF + "=?",
                keys,
                null, null, null);

        if (cursor.moveToFirst()) {
            retour = cursor.getString(cursor.getColumnIndexOrThrow(DictionaryDAO.Dictionary.COLUMN_CHAMP));
        }

        cursor.close();
        db.close();
        dbHelper.close();

        return retour;
    }

    public static boolean existDictionary(Context context, String clef) {
        Cursor cursor;
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Tableau des colonnes que je recherche
        String[] columns = {Dictionary.COLUMN_CLEF,
                DictionaryDAO.Dictionary.COLUMN_CHAMP};

        String[] keys = {clef};

        // Récupération du login
        cursor = db.query(Dictionary.TABLE_NAME,
                columns,
                DictionaryDAO.Dictionary.COLUMN_CLEF + "= ?",
                keys,
                null, null, null);

        boolean existe = cursor.moveToFirst();
        cursor.close();
        db.close();
        dbHelper.close();

        return existe;
    }

    public static boolean insertInto(Context context, String clef, String value) {
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Dictionary.COLUMN_CLEF, clef);
        values.put(Dictionary.COLUMN_CHAMP, value);

        long retour = db.insert(Dictionary.TABLE_NAME, null, values);

        db.close();
        dbHelper.close();

        return retour != -1;
    }

    public static boolean update(Context context, String clef, String value) {
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Dictionary.COLUMN_CLEF, clef);
        values.put(Dictionary.COLUMN_CHAMP, value);

        String[] keys = {clef};

        int retour = db.update(Dictionary.TABLE_NAME,
                values,
                DictionaryDAO.Dictionary.COLUMN_CLEF + "= ?",
                keys);

        db.close();
        dbHelper.close();

        return retour != 0;
    }

    /* Inner class that defines the table contents */
    public static abstract class Dictionary implements BaseColumns {
        public static final String TABLE_NAME = "dictionary";
        public static final String DB_CLEF_LOGIN = "LOGIN";
        public static final String DB_CLEF_MOT_PASSE = "MOT_DE_PASSE";
        public static final String DB_CLEF_ID_USER = "ID_USER";
        public static final String DB_CLEF_AUTO_CONNECT = "CONNECTION_AUTO";
        public static final String DB_CLEF_TELEPHONE = "TELEPHONE";
        public static final String DB_CLEF_DISPLAY_NAME = "DISPLAY_NAME";
        static final String COLUMN_CLEF = "CLEF";
        static final String COLUMN_CHAMP = "CHAMP";
    }
}
