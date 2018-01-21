package com.orlanth23.annoncesnc.list;

import android.content.Context;
import android.database.Cursor;

import com.orlanth23.annoncesnc.provider.ProviderContract.InfosServerEntry;
import com.orlanth23.annoncesnc.provider.contract.InfosServerContract;

import static com.orlanth23.annoncesnc.provider.AnnoncesProvider.sSelectionInfosServer;

public class ListeStats {

    private static ListeStats INSTANCE = null;
    private static Integer nbAnnonces;
    private static Integer nbUtilisateurs;

    public static ListeStats getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new ListeStats();
            getDataFromProvider(context);
        }
        return INSTANCE;
    }

    public static void getDataFromProvider(Context context){
        // Récupération des données à partir du contentProvider
        Cursor cursor = context.getContentResolver().query(InfosServerEntry.CONTENT_URI, null, sSelectionInfosServer, null, null);
        if (cursor!= null) {
            if (cursor.moveToFirst()) {
                nbAnnonces = cursor.getInt(cursor.getColumnIndex(InfosServerContract.COL_NB_ANNONCE));
                nbUtilisateurs = cursor.getInt(cursor.getColumnIndex(InfosServerContract.COL_NB_UTILISATEUR));
            }
            cursor.close();
        }
    }

    public Integer getNbAnnonces() {
        return nbAnnonces;
    }

    public void setNbAnnonces(Integer nbAnnonces) {
        ListeStats.nbAnnonces = nbAnnonces;
    }

    public Integer getNbUtilisateurs() {
        return nbUtilisateurs;
    }

    public void setNbUtilisateurs(Integer nbUtilisateurs) {
        ListeStats.nbUtilisateurs = nbUtilisateurs;
    }
}
