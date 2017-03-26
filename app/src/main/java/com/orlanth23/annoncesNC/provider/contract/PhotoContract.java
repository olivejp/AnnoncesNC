package com.orlanth23.annoncesnc.provider.contract;

import android.provider.BaseColumns;

public class PhotoContract implements BaseColumns {
    public static final String TABLE_NAME = "photo";
    public static final String COL_UUID_PHOTO = "UUID_photo";
    public static final String COL_CHEMIN_LOCAL_PHOTO = "localPathPhoto";
    public static final String COL_CHEMIN_FIREBASE_PHOTO = "firebasePathPhoto";
    public static final String COL_UUID_ANNONCE = "UUID_annonce";
    public static final String COL_STATUT_PHOTO = "statutPhoto";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY, " +
            COL_UUID_PHOTO + " TEXT NOT NULL," +
            COL_UUID_ANNONCE + " TEXT NULL," +
            COL_STATUT_PHOTO + " TEXT NULL," +
            COL_CHEMIN_FIREBASE_PHOTO + " TEXT NULL," +
            COL_CHEMIN_LOCAL_PHOTO + " TEXT NULL);";

}
