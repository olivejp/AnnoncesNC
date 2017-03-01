package com.orlanth23.annoncesnc.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.orlanth23.annoncesnc.list.ListeCategories;
import com.orlanth23.annoncesnc.list.ListeStats;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.RetrofitService;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AnnoncesSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = AnnoncesSyncAdapter.class.getName();

    private Context mContext;
    private ContentResolver mContentResolver;
    private Gson gson;

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
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public AnnoncesSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        RetrofitService retrofitService = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitService.class);

        // Tentative de récupération du nombre d'annonce par catégorie
        Call<ReturnWS> callGetListCategorie = retrofitService.getListCategory();
        callGetListCategorie.enqueue(callbackGetListCategorie);

        // Récupération du nombre d'annonces
        Call<ReturnWS> callGetNbAnnonce = retrofitService.getNbAnnonce();
        callGetNbAnnonce.enqueue(callbackGetNbAnnonce);

        // Récupération du nombre d'utilisateur
        Call<ReturnWS> callGetNbUtilisateur = retrofitService.getNbUser();
        callGetNbUtilisateur.enqueue(callbackGetNbUtilisateur);

    }
}
