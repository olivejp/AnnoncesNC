package com.orlanth23.annoncesnc.provider.contract;

import android.provider.BaseColumns;

public class UtilisateurContract implements BaseColumns{
    public static final String TABLE_NAME = "utilisateur";
    public static final String COL_TELEPHONE_UTILISATEUR = "telephoneUtilisateur";
    public static final String COL_EMAIL_UTILISATEUR = "emailUtilisateur";
    public static final String COL_DATE_CREATION_UTILISATEUR = "dateCreationUtilisateur";
    public static final String COL_PASS_UTILISATEUR = "passwordUtilisateur";
    public static final String COL_DATE_LAST_CONNEXION = "dateLastConnexion";
    public static final String COL_ADMIN_UTILISATEUR = "adminUtilisateur";
    public static final String COL_STATUT_UTILISATEUR = "statutUtilisateur";

    public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" ("+
            _ID + " INTEGER PRIMARY KEY,"+
            COL_TELEPHONE_UTILISATEUR + " INTEGER NULL,"+
            COL_EMAIL_UTILISATEUR + " TEXT NULL,"+
            COL_DATE_CREATION_UTILISATEUR + " TIMESTAMP,"+
            COL_PASS_UTILISATEUR + " TEXT NULL,"+
            COL_DATE_LAST_CONNEXION + " INTEGER NULL,"+
            COL_ADMIN_UTILISATEUR + " TEXT NULL DEFAULT 'N',"+
            COL_STATUT_UTILISATEUR + " TEXT NULL DEFAULT 'V');";
}
