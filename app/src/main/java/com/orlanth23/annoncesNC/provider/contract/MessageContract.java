package com.orlanth23.annoncesnc.provider.contract;

import android.provider.BaseColumns;

public class MessageContract implements BaseColumns {
    public static final String TABLE_NAME = "message";
    public static final String COL_ID_ANNONCE = "idannonce";
    public static final String COL_ID_SENDER = "idSender";
    public static final String COL_ID_RECEIVER = "idReceiver";
    public static final String COL_MESSAGE = "message";
    public static final String COL_DATE_MESSAGE = "dateMessage";

    public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" (" +
            _ID + " INTEGER PRIMARY KEY," +
            COL_ID_ANNONCE + " INTEGER NOT NULL," +
            COL_ID_SENDER + " INTEGER NOT NULL," +
            COL_ID_RECEIVER + " INTEGER NOT NULL," +
            COL_MESSAGE + " TEXT NULL," +
            COL_DATE_MESSAGE + " TIMESTAMP);";
}
