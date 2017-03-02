package com.orlanth23.annoncesnc.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.orlanth23.annoncesnc.provider.contract.AnnonceContract;
import com.orlanth23.annoncesnc.provider.contract.MessageContract;
import com.orlanth23.annoncesnc.provider.contract.PhotoContract;
import com.orlanth23.annoncesnc.provider.contract.UtilisateurContract;

public class AnnoncesProvider extends ContentProvider {

    public static final String sSelectionAnnonceById =
        AnnonceContract.TABLE_NAME + "." + AnnonceContract._ID + " = ?";

    public static final String sSelectionAnnoncesByStatut =
        AnnonceContract.TABLE_NAME + "." + AnnonceContract.COL_STATUT_ANNONCE + " = ?";

    static final int ANNONCE = 200;
    static final int UTILISATEUR = 300;
    static final int MESSAGE = 400;
    static final int PHOTO = 500;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private AnnoncesDbHelper mOpenHelper;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ProviderContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, ProviderContract.PATH_ANNONCES, ANNONCE);
        matcher.addURI(authority, ProviderContract.PATH_UTILISATEURS, UTILISATEUR);
        matcher.addURI(authority, ProviderContract.PATH_MESSAGES, MESSAGE);
        matcher.addURI(authority, ProviderContract.PATH_PHOTOS, PHOTO);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new AnnoncesDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case MESSAGE: {
                tableName = MessageContract.TABLE_NAME;
                break;
            }
            case UTILISATEUR: {
                tableName = UtilisateurContract.TABLE_NAME;
                break;
            }
            case PHOTO: {
                tableName = PhotoContract.TABLE_NAME;
                break;
            }
            case ANNONCE: {
                tableName = AnnonceContract.TABLE_NAME;
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor = mOpenHelper.getReadableDatabase().query(
            tableName,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder);

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
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
            case MESSAGE: {
                long _id = db.insert(MessageContract.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ContentUris.withAppendedId(ProviderContract.MessageEntry.CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case UTILISATEUR: {
                long _id = db.insert(UtilisateurContract.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ContentUris.withAppendedId(ProviderContract.UtilisateurEntry.CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PHOTO: {
                long _id = db.insert(PhotoContract.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ContentUris.withAppendedId(ProviderContract.PhotoEntry.CONTENT_URI, _id);
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
            case UTILISATEUR:
                rowsDeleted = db.delete(
                    UtilisateurContract.TABLE_NAME, selection, selectionArgs);
                break;
            case PHOTO:
                rowsDeleted = db.delete(
                    PhotoContract.TABLE_NAME, selection, selectionArgs);
                break;
            case MESSAGE:
                rowsDeleted = db.delete(
                    MessageContract.TABLE_NAME, selection, selectionArgs);
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
            case MESSAGE:
                rowsUpdated = db.update(MessageContract.TABLE_NAME, values, selection,
                    selectionArgs);
                break;
            case PHOTO:
                rowsUpdated = db.update(PhotoContract.TABLE_NAME, values, selection,
                    selectionArgs);
                break;
            case UTILISATEUR:
                rowsUpdated = db.update(UtilisateurContract.TABLE_NAME, values, selection,
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
