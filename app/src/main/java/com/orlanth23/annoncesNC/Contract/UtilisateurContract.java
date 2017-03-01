package com.orlanth23.annoncesnc.contract;

public class UtilisateurContract {
    public static final String TABLE_NAME = "utilisateur";
    public static final String COL_ID_UTILISATEUR = "idutilisateur";
    public static final String COL_TELEPHONE_UTILISATEUR = "telephoneUtilisateur";
    public static final String COL_EMAIL_UTILISATEUR = "emailUtilisateur";
    public static final String COL_DATE_CREATION_UTILISATEUR = "dateCreationUtilisateur";
    public static final String COL_PASSWORD_UTILISATEUR = "passwordUtilisateur";
    public static final String COL_DATE_LAST_CONNEXION = "dateLastConnexion";
    public static final String COL_ADMIN_UTILISATEUR = "adminUtilisateur";
    public static final String COL_STATUT_UTILISATEUR = "statutUtilisateur";

    public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" ("+
            COL_ID_UTILISATEUR + " INTEGER NOT NULL AUTO_INCREMENT,"+
            COL_TELEPHONE_UTILISATEUR + " INTEGER NULL,"+
            COL_EMAIL_UTILISATEUR + " VARCHAR(200) NULL,"+
            COL_DATE_CREATION_UTILISATEUR + " TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),"+
            COL_PASSWORD_UTILISATEUR + " VARCHAR(100) NULL,"+
            COL_DATE_LAST_CONNEXION + " INTEGER NULL,"+
            COL_ADMIN_UTILISATEUR + " VARCHAR(1) NULL DEFAULT 'N',"+
            COL_STATUT_UTILISATEUR + " VARCHAR(1) NULL DEFAULT 'V',"+
            "  PRIMARY KEY("+COL_ID_UTILISATEUR+"));";
}
