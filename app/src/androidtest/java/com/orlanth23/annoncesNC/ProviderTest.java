package com.orlanth23.annoncesNC;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.orlanth23.annoncesNC.provider.ProviderContract;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ProviderTest {

    private Context mContext;

    private void deleteRecords(){
        mContext.getContentResolver().delete(
                ProviderContract.CategorieEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                ProviderContract.CategorieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Categorie table during delete", 0, cursor.getCount());
        cursor.close();
    }

    @Before
    public void precondition(){
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void deleteAllRecordsFromProvider() {
        deleteRecords();
    }

    @Test
    public void testInsertReadProvider() {

        /* Suppression des enregistrements précédents */
        deleteRecords();

        ContentValues testValues = TestUtilities.createCategorieValues(1234);

        /* add a ContentObserver */
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ProviderContract.CategorieEntry.CONTENT_URI, true, tco);

        /* try to insert */
        Uri categorieUri = mContext.getContentResolver().insert(ProviderContract.CategorieEntry.CONTENT_URI, testValues);

        /* verify that the notifyChange has been called */
        tco.waitForNotificationOrFail();

        /* unregister the contentObserver */
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(categorieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                ProviderContract.CategorieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.",
                cursor, testValues);
    }
}
