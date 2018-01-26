package com.orlanth23.annoncesnc;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.orlanth23.annoncesnc.database.provider.ProviderContract;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ProviderTest {

    private Context mContext;

    private void deleteRecords(Uri uri) {
        mContext.getContentResolver().delete(
            uri,
            null,
            null
        );

        Cursor cursor = mContext.getContentResolver().query(
            uri,
            null,
            null,
            null,
            null
        );
        if (cursor != null) {
            assertEquals("Error: Records from URI " + uri.toString() + " not deleted table during delete", 0, cursor.getCount());
            cursor.close();
        } else {
            assertTrue(false);
        }
    }

    private void testInsertReadUriProvider(Uri uri, ContentValues contentValues) {

        /* Suppression des enregistrements précédents */
        deleteRecords(uri);

        /* add a ContentObserver */
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(uri, true, tco);

        /* try to insert */
        Uri categorieUri = mContext.getContentResolver().insert(uri, contentValues);

        /* verify that the notifyChange has been called */
        tco.waitForNotificationOrFail();

        /* unregister the contentObserver */
        mContext.getContentResolver().unregisterContentObserver(tco);

        long rowId = ContentUris.parseId(categorieUri);

        // Verify we got a row back.
        assertTrue(rowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
            uri,
            null, // leaving "columns" null just returns all the columns.
            null, // cols for "where" clause
            null, // values for "where" clause
            null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating URI ".concat(uri.toString()),
            cursor, contentValues);
    }

    @Before
    public void precondition() {
        mContext = InstrumentationRegistry.getTargetContext();
    }


    @Test
    public void deleteAllRecordsFromAnnonceProvider() {
        deleteRecords(ProviderContract.AnnonceEntry.CONTENT_URI);
    }

    @Test
    public void deleteAllRecordsFromPhotoProvider() {
        deleteRecords(ProviderContract.PhotoEntry.CONTENT_URI);
    }

    @Test
    public void deleteAllRecordsFromMessageProvider() {
        deleteRecords(ProviderContract.MessageEntry.CONTENT_URI);
    }

    @Test
    public void deleteAllRecordsFromUtilisateurProvider() {
        deleteRecords(ProviderContract.UtilisateurEntry.CONTENT_URI);
    }

    @Test
    public void testInsertReadPhotoProvider() {
        ContentValues testValuesPhotos = TestUtilities.createPhotoValues(1, 1234);
        testInsertReadUriProvider(ProviderContract.PhotoEntry.CONTENT_URI, testValuesPhotos);
    }

    @Test
    public void testInsertReadAnnonceWithNullIdProvider() {
        ContentValues testValuesAnnonceNull = TestUtilities.createAnnonceValuesNullId();
        testInsertReadUriProvider(ProviderContract.AnnonceEntry.CONTENT_URI, testValuesAnnonceNull);
    }

    @Test
    public void testInsertReadAnnonceProvider() {
        ContentValues testValuesAnnonce = TestUtilities.createAnnonceValues(1234);
        testInsertReadUriProvider(ProviderContract.AnnonceEntry.CONTENT_URI, testValuesAnnonce);
    }
}
