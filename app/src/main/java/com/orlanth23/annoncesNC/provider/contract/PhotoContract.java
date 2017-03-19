package com.orlanth23.annoncesnc.provider.contract;

import android.provider.BaseColumns;

public class PhotoContract implements BaseColumns {
    public static final String TABLE_NAME = "photo";
    public static final String COL_ID_PHOTO_SERVER = "idPhoto";
    public static final String COL_NOM_PHOTO = "nomPhoto";
    public static final String COL_ID_ANNONCE = "annonce_idannonce";
    public static final String COL_STATUT_PHOTO = "statutPhoto";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_ID_ANNONCE + " INTEGER NULL," +
            COL_ID_PHOTO_SERVER + " INTEGER NULL," +
            COL_STATUT_PHOTO + " TEXT NULL," +
            COL_NOM_PHOTO + " TEXT NULL);";

}
