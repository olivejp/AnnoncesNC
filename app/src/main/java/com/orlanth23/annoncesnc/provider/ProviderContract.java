package com.orlanth23.annoncesnc.provider;

import android.content.ContentResolver;
import android.net.Uri;

public class ProviderContract {

    public static final String CONTENT_AUTHORITY = "com.orlanth23.annoncesnc";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // public static final String PATH_CATEGORIES = "categories";
    public static final String PATH_ANNONCES        = "annonces";
    public static final String PATH_MESSAGES        = "messages";
    public static final String PATH_UTILISATEURS    = "utilisateurs";
    public static final String PATH_PHOTOS          = "photos";
    public static final String PATH_INFOS_SERVER    = "infos_server";

    public static final class AnnonceEntry {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ANNONCES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ANNONCES;
    }

    public static final class UtilisateurEntry{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_UTILISATEURS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_UTILISATEURS;
    }

    public static final class MessageEntry {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MESSAGES;
    }

    public static final class PhotoEntry {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PHOTOS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHOTOS;
    }

    public static final class InfosServerEntry {
        public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_INFOS_SERVER).build();

        public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INFOS_SERVER;
    }
}
