package com.orlanth23.annoncesnc.database.provider.contract;

import android.provider.BaseColumns;

public class MessageContract implements BaseColumns {
    public static final String TABLE_NAME = "message";
    public static final String COL_UUID_MESSAGE = "UUIDMessage";
    public static final String COL_ID_ANNONCE = "idannonce";
    public static final String COL_ID_SENDER = "idSender";
    public static final String COL_ID_RECEIVER = "idReceiver";
    public static final String COL_MESSAGE = "message";
    public static final String COL_DATE_MESSAGE = "dateMessage";
    public static final String COL_STATUT_MESSAGE = "statutMessage";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            COL_UUID_MESSAGE + " TEXT NOT NULL," +
            COL_ID_ANNONCE + " TEXT NOT NULL," +
            COL_ID_SENDER + " TEXT NOT NULL," +
            COL_ID_RECEIVER + " TEXT NOT NULL," +
            COL_MESSAGE + " TEXT NULL," +
            COL_STATUT_MESSAGE + " TEXT NULL," +
            COL_DATE_MESSAGE + " TIMESTAMP);";
}
