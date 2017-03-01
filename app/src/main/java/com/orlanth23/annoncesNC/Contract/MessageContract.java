package com.orlanth23.annoncesnc.contract;

public class MessageContract {
    public static final String TABLE_NAME = "message";
    public static final String COL_ID_MESSAGE = "idMessage";
    public static final String COL_ID_ANNONCE = "idannonce";
    public static final String COL_ID_SENDER = "idSender";
    public static final String COL_ID_RECEIVER = "idReceiver";
    public static final String COL_MESSAGE = "message";
    public static final String COL_DATE_MESSAGE = "dateMessage";

    public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" (" +
            COL_ID_MESSAGE + " INTEGER UNSIGNED NOT NULL AUTO_INCREMENT," +
            COL_ID_ANNONCE + " INTEGER NOT NULL," +
            COL_ID_SENDER + " INTEGER NOT NULL," +
            COL_MESSAGE + " VARCHAR(1000) NULL," +
            COL_DATE_MESSAGE + " TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP()," +
            "  PRIMARY KEY("+COL_ID_MESSAGE+"));";
}
