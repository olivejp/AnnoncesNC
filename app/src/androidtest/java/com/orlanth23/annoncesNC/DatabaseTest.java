package com.orlanth23.annoncesnc;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.orlanth23.annoncesnc.provider.AnnoncesDbHelper;
import com.orlanth23.annoncesnc.provider.contract.AnnonceContract;
import com.orlanth23.annoncesnc.provider.contract.CategorieContract;
import com.orlanth23.annoncesnc.provider.contract.MessageContract;
import com.orlanth23.annoncesnc.provider.contract.PhotoContract;
import com.orlanth23.annoncesnc.provider.contract.UtilisateurContract;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    @Before
    public void deleteDb(){
        Context appContext = InstrumentationRegistry.getTargetContext();
        appContext.deleteDatabase(AnnoncesDbHelper.DATABASE_NAME);
    }

    @Test
    public void testOpenDatabase() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = new AnnoncesDbHelper(appContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        ArrayList<String> myListNomTable = new ArrayList<>();
        myListNomTable.add(CategorieContract.TABLE_NAME);
        myListNomTable.add(AnnonceContract.TABLE_NAME);
        myListNomTable.add(UtilisateurContract.TABLE_NAME);
        myListNomTable.add(MessageContract.TABLE_NAME);
        myListNomTable.add(PhotoContract.TABLE_NAME);

        HashMap<String, Boolean> myMap = new HashMap<>();
        for (String value : myListNomTable) {
            myMap.put(value, Boolean.FALSE);
        }

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        while( c.moveToNext() ){
            String nomTable = c.getString(0);
            if (myMap.containsKey(nomTable)){
                myMap.put(nomTable, Boolean.TRUE);
            }
        }

        for (String value :myListNomTable) {
            assertEquals(Boolean.TRUE, myMap.get(value));
        }
        c.close();
    }
}
