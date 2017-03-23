/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orlanth23.annoncesnc.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.orlanth23.annoncesnc.provider.contract.AnnonceContract;
import com.orlanth23.annoncesnc.provider.contract.InfosServerContract;
import com.orlanth23.annoncesnc.provider.contract.MessageContract;
import com.orlanth23.annoncesnc.provider.contract.PhotoContract;
import com.orlanth23.annoncesnc.provider.contract.UtilisateurContract;

public class AnnoncesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "annonces.db";
    private static final int DATABASE_VERSION = 13;

    public AnnoncesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(UtilisateurContract.CREATE_TABLE);
        sqLiteDatabase.execSQL(AnnonceContract.CREATE_TABLE);
        sqLiteDatabase.execSQL(PhotoContract.CREATE_TABLE);
        sqLiteDatabase.execSQL(MessageContract.CREATE_TABLE);
        sqLiteDatabase.execSQL(InfosServerContract.CREATE_TABLE);
        sqLiteDatabase.execSQL(InfosServerContract.FIRST_INSERT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MessageContract.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PhotoContract.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AnnonceContract.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UtilisateurContract.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + InfosServerContract.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
