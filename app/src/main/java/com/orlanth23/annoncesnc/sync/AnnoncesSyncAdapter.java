package com.orlanth23.annoncesnc.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orlanth23.annoncesnc.dto.StatutAnnonce;
import com.orlanth23.annoncesnc.list.ListeCategories;
import com.orlanth23.annoncesnc.list.ListeStats;
import com.orlanth23.annoncesnc.provider.contract.AnnonceContract;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.RetrofitService;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.orlanth23.annoncesnc.provider.AnnoncesProvider.sSelectionAnnoncesByStatut;
import static com.orlanth23.annoncesnc.provider.ProviderContract.AnnonceEntry;

public class AnnoncesSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = AnnoncesSyncAdapter.class.getName();

    private static final Gson gson = new Gson();
    private static final RetrofitService retrofitService = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitService.class);
    private ContentResolver mContentResolver;
    private Callback<ReturnWS> callbackPostAnnonce = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS rs = response.body();
                if (rs.statusValid()) {
                    // Si la mise à jour à bien eu lieu sur le serveur, on va mettre à jour l'annonce dans notre ContentProvider
                    Integer idLocal = rs.getIdLocal();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AnnonceContract.COL_STATUT_ANNONCE, StatutAnnonce.Valid.valeur());
                    String where = AnnonceContract._ID + " = ?";
                    String[] whereArgs = new String[]{String.valueOf(idLocal)};
                    mContentResolver.update(AnnonceEntry.CONTENT_URI, contentValues, where, whereArgs);
                } else {
                    Toast.makeText(getContext(), rs.getMsg(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            t.printStackTrace();
        }
    };
    private Callback<ReturnWS> callbackGetNbAnnonce = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS rs = response.body();
                if (rs.statusValid()) {
                    ListeStats.setNbAnnonces(Integer.valueOf(rs.getMsg()));
                }
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            Log.d(TAG, "callbackGetNbAnnonce failed");
        }
    };

    private Callback<ReturnWS> callbackGetNbUtilisateur = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS rs = response.body();
                if (rs.statusValid()) {
                    ListeStats.setNbUsers(Integer.valueOf(rs.getMsg()));
                }
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            Log.d(TAG, "callbackGetNbUtilisateur failed");
        }
    };

    private Callback<ReturnWS> callbackGetListCategorie = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS returnWS = response.body();
                if (returnWS.statusValid()) {
                    ListeCategories.setNbAnnonceFromJson(returnWS.getMsg());
                }
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            Log.d(TAG, "callbackGetListCategorie failed");
        }
    };


    public AnnoncesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public AnnoncesSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        // On envoie les annonces qui sont en attente
        postAnnonceEnAttente();

        // Tentative de récupération du nombre d'annonce par catégorie
        Call<ReturnWS> callGetListCategorie = retrofitService.getListCategory();
        callGetListCategorie.enqueue(callbackGetListCategorie);

        // Récupération du nombre d'annonces
        Call<ReturnWS> callGetNbAnnonce = retrofitService.getNbAnnonce();
        callGetNbAnnonce.enqueue(callbackGetNbAnnonce);

        // Récupération du nombre d'utilisateur
        Call<ReturnWS> callGetNbUtilisateur = retrofitService.getNbUser();
        callGetNbUtilisateur.enqueue(callbackGetNbUtilisateur);

        Log.d(TAG, "JPO ***** onPerformSync");
    }

    private void postAnnonceEnAttente() {
        // Lecture des annonces postées Hors Connexion qui sont maintenant à envoyer
        Cursor cursorAnnoncesToSend;
        String[] selectionArgs = new String[]{StatutAnnonce.ToPost.valeur()};
        cursorAnnoncesToSend = mContentResolver.query(AnnonceEntry.CONTENT_URI, null, sSelectionAnnoncesByStatut, selectionArgs, null);

        if (cursorAnnoncesToSend != null) {
            while (cursorAnnoncesToSend.moveToNext()) {
                int indexColIdLocal = cursorAnnoncesToSend.getColumnIndex(AnnonceContract._ID);
                int indexColIdCategory = cursorAnnoncesToSend.getColumnIndex(AnnonceContract.COL_ID_CATEGORY);
                int indexColIdUtilisateur = cursorAnnoncesToSend.getColumnIndex(AnnonceContract.COL_ID_UTILISATEUR);
                int indexColTitreAnnonce = cursorAnnoncesToSend.getColumnIndex(AnnonceContract.COL_TITRE_ANNONCE);
                int indexColDescriptionAnnonce = cursorAnnoncesToSend.getColumnIndex(AnnonceContract.COL_DESCRIPTION_ANNONCE);
                int indexColPrixAnnonce = cursorAnnoncesToSend.getColumnIndex(AnnonceContract.COL_PRIX_ANNONCE);

                Integer idCategory = cursorAnnoncesToSend.getInt(indexColIdCategory);
                Integer idUtilisateur = cursorAnnoncesToSend.getInt(indexColIdUtilisateur);
                String titreAnnonce = cursorAnnoncesToSend.getString(indexColTitreAnnonce);
                String descriptionAnnonce = cursorAnnoncesToSend.getString(indexColDescriptionAnnonce);
                Integer prixAnnonce = cursorAnnoncesToSend.getInt(indexColPrixAnnonce);
                Integer idLocal = cursorAnnoncesToSend.getInt(indexColIdLocal);

                retrofitService.postAnnonce(idCategory,idUtilisateur, titreAnnonce, descriptionAnnonce, prixAnnonce, idLocal).enqueue(callbackPostAnnonce);
           }
            cursorAnnoncesToSend.close();
        }
    }
}
