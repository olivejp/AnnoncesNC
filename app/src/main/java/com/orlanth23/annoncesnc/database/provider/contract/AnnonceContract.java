package com.orlanth23.annoncesnc.database.provider.contract;

import android.provider.BaseColumns;

/**
 * Attention ne pas confondre idAnnonce et _ID qui sont deux colonnes bien distinctes.
 * _ID est l'identifiant de notre annonce dans le ContentProvider alors que la colonne idAnnonce est
 * celle que l'on récupère après que l'annonce ait été envoyée sur le serveur
 * <p>
 * Tant que l'annonce n'est pas envoyée (statutAnnonce = "T"), la colonne idAnnonce devrait toujours
 * être à *blank. Dès que l'annonce a été envoyée on va mettre à jour cette valeur.
 **/
public class AnnonceContract implements BaseColumns {
    public static final String TABLE_NAME = "annonce";
    public static final String COL_UUID_ANNONCE = "UUIDAnnonce";
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

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY, " +
            COL_UUID_ANNONCE + " TEXT NOT NULL, " +
            COL_ID_CATEGORY + " INTEGER NOT NULL, " +
            COL_ID_UTILISATEUR + " TEXT NOT NULL, " +
            COL_TITRE_ANNONCE + " TEXT NULL, " +
            COL_DESCRIPTION_ANNONCE + " TEXT NULL, " +
            COL_DATE_PUBLICATION + " TIMESTAMP, " +
            COL_PRIX_ANNONCE + " INTEGER NULL, " +
            COL_STATUT_ANNONCE + " TEXT NULL, " +
            COL_CONTACT_TEL + " BOOL NULL, " +
            COL_CONTACT_MEL + " BOOL NULL, " +
            COL_CONTACT_MSG + " BOOL NULL);";
}
