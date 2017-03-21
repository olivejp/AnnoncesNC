package com.orlanth23.annoncesnc.list;

import android.content.Context;

import com.orlanth23.annoncesnc.provider.ProviderContract;

public class ListeStats {

    private static ListeStats INSTANCE = null;
    private static Integer nbAnnonces;
    private static Integer nbUsers;

    public static ListeStats getInstance(Context context) {
        if (INSTANCE == null) {
            // ToDo finir l'appel du Query
            context.getContentResolver().query(ProviderContract.InfosServerEntry.CONTENT_URI, projection, selection, selectionArgs, sortBy);
            INSTANCE = new ListeStats();
        }
        return INSTANCE;
    }

    public static Integer getNbAnnonces() {
        return nbAnnonces;
    }

    public static void setNbAnnonces(Integer nbAnnonces) {
        ListeStats.nbAnnonces = nbAnnonces;
    }

    public static Integer getNbUsers() {
        return nbUsers;
    }

    public static void setNbUsers(Integer nbUsers) {
        ListeStats.nbUsers = nbUsers;
    }
}
