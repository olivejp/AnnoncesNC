package com.orlanth23.annoncesnc.provider.contract;

import android.provider.BaseColumns;

public class CategorieContract implements BaseColumns {
    public static final String TABLE_NAME = "categorie";
    public static final String COL_NOM_CATEGORIE = "nomCategorie";
    public static final String COL_COULEUR_CATEGORIE = "couleurCategorie";
    public static final String COL_NB_ANNONCE = "nombreAnnonceCategorie";

    public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" (" +
            _ID + " INTEGER PRIMARY KEY," +
            COL_NOM_CATEGORIE + " TEXT NOT NULL," +
            COL_COULEUR_CATEGORIE + " TEXT NOT NULL, " +
            COL_NB_ANNONCE + " INTEGER NOT NULL);";
}
