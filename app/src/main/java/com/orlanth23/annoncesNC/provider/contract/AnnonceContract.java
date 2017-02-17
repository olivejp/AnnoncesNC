package com.orlanth23.annoncesnc.provider.contract;

import android.provider.BaseColumns;

public class AnnonceContract implements BaseColumns {
    public static final String TABLE_NAME = "annonce";
    public static final String COL_ID_UTILISATEUR = "utilisateur_idutilisateur";
    public static final String COL_ID_CATEGORY = "categorie_idcategorie";
    public static final String COL_TITRE_ANNONCE = "titreAnnonce";
    public static final String COL_DESCRIPTION_ANNONCE = "descriptionAnnonce";
    public static final String COL_DATE_PUBLICATION = "datePublicationAnnonce";
    public static final String COL_PRIX_ANNONCE = "prixAnnonce";
    public static final String COL_STATUT_ANNONCE = "statutAnnonce";
    public static final String COL_CONTACT_TEL = "contactByTel";
    public static final String COL_CONTACT_MEL = "contactByEmail";
    public static final String COL_CONTACT_MSG = "contactByMsg";

    public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" (" +
            _ID + " INTEGER PRIMARY KEY," +
            COL_ID_CATEGORY + " INTEGER NOT NULL," +
            COL_ID_UTILISATEUR + " INTEGER NOT NULL," +
            COL_TITRE_ANNONCE + " TEXT NULL," +
            COL_DESCRIPTION_ANNONCE + " TEXT NULL," +
            COL_DATE_PUBLICATION + " TIMESTAMP," +
            COL_PRIX_ANNONCE + " INTEGER NULL," +
            COL_STATUT_ANNONCE + " TEXT NULL DEFAULT 'V'," +
            COL_CONTACT_TEL + " BOOL NULL DEFAULT 1," +
            COL_CONTACT_MEL + " BOOL NULL DEFAULT 1," +
            COL_CONTACT_MSG + " BOOL NULL DEFAULT 0);";
}
