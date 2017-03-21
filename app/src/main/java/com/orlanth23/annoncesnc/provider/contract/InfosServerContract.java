package com.orlanth23.annoncesnc.provider.contract;

import android.provider.BaseColumns;

public class InfosServerContract implements BaseColumns {
    public static final String TABLE_NAME = "infosServer";
    public static final String COL_NB_UTILISATEUR = "nbUtilisateur";
    public static final String COL_NB_ANNONCE = "nbAnnonce";


    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_NB_UTILISATEUR + " INTEGER NOT NULL, " +
        COL_NB_ANNONCE + " INTEGER NOT NULL;";

    public static final String FIRST_INSERT_TABLE = "INSERT INTO " + TABLE_NAME + " (" +
        _ID + ", " + COL_NB_UTILISATEUR + ", " + COL_NB_ANNONCE + ") VALUES (1, 0, 0)";
}
