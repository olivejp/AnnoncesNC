package com.orlanth23.annoncesnc.contract;

public class CategorieContract {
    public static final String TABLE_NAME = "categorie";
    public static final String COL_ID_CATEGORIE = "idcategorie";
    public static final String COL_NOM_CATEGORIE = "nomCategorie";
    public static final String COL_COULEUR_CATEGORIE = "couleurCategorie";

    public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" (" +
            COL_ID_CATEGORIE + " INTEGER NOT NULL AUTO_INCREMENT," +
            COL_NOM_CATEGORIE + " VARCHAR(50) NULL," +
            COL_COULEUR_CATEGORIE + " VARCHAR(10) NULL," +
            "  PRIMARY KEY("+COL_ID_CATEGORIE+"));";
}
