package com.orlanth23.annoncesNC.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.orlanth23.annoncesNC.provider.contract.AnnonceContract;
import com.orlanth23.annoncesNC.provider.contract.CategorieContract;

public class AnnoncesProvider extends ContentProvider {

    private AnnoncesDbHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final int CATEGORIE = 100;
    static final int ANNONCE = 200;
    static final int UTILISATEUR = 300;
    static final int MESSAGE = 400;
    static final int PHOTO = 500;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ProviderContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, ProviderContract.PATH_CATEGORIES, CATEGORIE);
        matcher.addURI(authority, ProviderContract.PATH_ANNONCES, ANNONCE);
        matcher.addURI(authority, ProviderContract.PATH_UTILISATEURS, UTILISATEUR);
        matcher.addURI(authority, ProviderContract.PATH_MESSAGES, MESSAGE);
        matcher.addURI(authority, ProviderContract.PATH_PHOTOS, PHOTO);
        return matcher;
    }

    public static final String sSelectionAnnonceById =
            AnnonceContract.TABLE_NAME + "." + AnnonceContract._ID + " = ?";

    @Override
    public boolean onCreate() {
        mOpenHelper = new AnnoncesDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case ANNONCE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        AnnonceContract.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case CATEGORIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CategorieContract.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CATEGORIE:
                return ProviderContract.CategorieEntry.CONTENT_TYPE;
            case ANNONCE:
                return ProviderContract.AnnonceEntry.CONTENT_TYPE;
            case UTILISATEUR:
                return ProviderContract.UtilisateurEntry.CONTENT_TYPE;
            case MESSAGE:
                return ProviderContract.MessageEntry.CONTENT_TYPE;
            case PHOTO:
                return ProviderContract.PhotoEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ANNONCE: {
                long _id = db.insert(AnnonceContract.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ContentUris.withAppendedId(ProviderContract.AnnonceEntry.CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CATEGORIE: {
                long _id = db.insert(CategorieContract.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ContentUris.withAppendedId(ProviderContract.CategorieEntry.CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if (null == selection) selection = "1";
        switch (match) {
            case ANNONCE:
                rowsDeleted = db.delete(
                        AnnonceContract.TABLE_NAME, selection, selectionArgs);
                break;
            case CATEGORIE:
                rowsDeleted = db.delete(
                        CategorieContract.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ANNONCE:
                rowsUpdated = db.update(AnnonceContract.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CATEGORIE:
                rowsUpdated = db.update(CategorieContract.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
