package com.orlanth23.annoncesNC.provider.contract;

import android.provider.BaseColumns;

public class PhotoContract implements BaseColumns{
    public static final String TABLE_NAME = "photo";
    public static final String COL_NOM_PHOTO = "nomPhoto";
    public static final String COL_ID_ANNONCE = "annonce_idannonce";

    public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" (" +
            _ID + " INTEGER PRIMARY KEY," +
            COL_ID_ANNONCE +" INTEGER NULL," +
            COL_NOM_PHOTO + " TEXT NULL);";

}
