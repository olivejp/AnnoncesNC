package com.orlanth23.annoncesnc.contract;

public class AnnonceContract {
    public static final String TABLE_NAME = "annonce";
    public static final String COL_ID_ANNONCE = "idannonce";
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
            COL_ID_ANNONCE + " INTEGER NOT NULL AUTO_INCREMENT," +
            COL_ID_CATEGORY + " INTEGER NULL," +
            COL_ID_UTILISATEUR + " INTEGER NOT NULL," +
            COL_TITRE_ANNONCE + " VARCHAR(100) NULL," +
            COL_DESCRIPTION_ANNONCE + " VARCHAR(1000) NULL," +
            COL_DATE_PUBLICATION + " TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP()," +
            COL_PRIX_ANNONCE + " INTEGER NULL," +
            COL_STATUT_ANNONCE + " VARCHAR(1) NULL DEFAULT 'V'," +
            COL_CONTACT_TEL + " BOOL NULL DEFAULT 1," +
            COL_CONTACT_MEL + " BOOL NULL DEFAULT 1," +
            COL_CONTACT_MSG + " BOOL NULL DEFAULT 0," +
            "PRIMARY KEY("+COL_ID_ANNONCE+"));";
}
