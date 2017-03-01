package com.orlanth23.annoncesnc.contract;

public class PhotoContract {
    public static final String TABLE_NAME = "photo";
    public static final String COL_ID_PHOTO = "idPhoto";
    public static final String COL_NOM_PHOTO = "nomPhoto";
    public static final String COL_ID_ANNONCE = "annonce_idannonce";

    public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" (" +
            COL_ID_PHOTO + " INTEGER NOT NULL AUTO_INCREMENT," +
            COL_ID_ANNONCE +" INTEGER NULL," +
            COL_NOM_PHOTO + " VARCHAR(400) NULL," +
            "  PRIMARY KEY("+COL_ID_PHOTO+"));";

}
