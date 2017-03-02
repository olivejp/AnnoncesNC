package com.orlanth23.annoncesnc;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;

import com.orlanth23.annoncesnc.dto.StatutPhoto;
import com.orlanth23.annoncesnc.provider.contract.AnnonceContract;
import com.orlanth23.annoncesnc.provider.contract.PhotoContract;

import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TestUtilities {

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                "' did not match the expected value '" +
                expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createPhotoValues(Integer idAnnonce, @Nullable Integer photoId) {
        ContentValues testValues = new ContentValues();
        if (photoId != null) {
            testValues.put(PhotoContract._ID, photoId);
        }
        testValues.put(PhotoContract.COL_NOM_PHOTO, "snow.jpg");
        testValues.put(PhotoContract.COL_STATUT_PHOTO, StatutPhoto.ToSend.valeur());
        testValues.put(PhotoContract.COL_ID_ANNONCE, idAnnonce);
        return testValues;
    }

    static ContentValues createAnnonceValuesNullId() {
        ContentValues testValues = new ContentValues();
        testValues.put(AnnonceContract.COL_CONTACT_MEL, "0");
        testValues.put(AnnonceContract.COL_CONTACT_MSG, "0");
        testValues.put(AnnonceContract.COL_CONTACT_TEL, "0");
        testValues.put(AnnonceContract.COL_ID_ANNONCE_SERVER, "1");
        testValues.put(AnnonceContract.COL_TITRE_ANNONCE, "Titre d'annonce");
        testValues.put(AnnonceContract.COL_DESCRIPTION_ANNONCE, "Description d'annonce");
        testValues.put(AnnonceContract.COL_PRIX_ANNONCE, "123456");
        testValues.put(AnnonceContract.COL_ID_UTILISATEUR, "1");
        testValues.put(AnnonceContract.COL_ID_CATEGORY, "1");
        testValues.put(AnnonceContract.COL_STATUT_ANNONCE, "V");
        return testValues;
    }

    static ContentValues createAnnonceValues(long annonceId) {
        ContentValues testValues = new ContentValues();
        testValues.put(AnnonceContract._ID, annonceId);
        testValues.put(AnnonceContract.COL_CONTACT_MEL, "0");
        testValues.put(AnnonceContract.COL_CONTACT_MSG, "0");
        testValues.put(AnnonceContract.COL_CONTACT_TEL, "0");
        testValues.put(AnnonceContract.COL_ID_ANNONCE_SERVER, "1");
        testValues.put(AnnonceContract.COL_TITRE_ANNONCE, "Titre d'annonce");
        testValues.put(AnnonceContract.COL_DESCRIPTION_ANNONCE, "Description d'annonce");
        testValues.put(AnnonceContract.COL_PRIX_ANNONCE, "123456");
        testValues.put(AnnonceContract.COL_ID_UTILISATEUR, "1");
        testValues.put(AnnonceContract.COL_ID_CATEGORY, "1");
        testValues.put(AnnonceContract.COL_STATUT_ANNONCE, "V");
        return testValues;
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }
}
