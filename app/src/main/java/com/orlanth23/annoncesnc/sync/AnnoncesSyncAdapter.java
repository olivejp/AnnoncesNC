package com.orlanth23.annoncesnc.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orlanth23.annoncesnc.dto.Categorie;
import com.orlanth23.annoncesnc.list.ListeCategories;
import com.orlanth23.annoncesnc.provider.ProviderContract;
import com.orlanth23.annoncesnc.provider.contract.CategorieContract;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.RetrofitService;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AnnoncesSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = AnnoncesSyncAdapter.class.getName();

    ContentResolver mContentResolver;
    private RetrofitService retrofitService;
    private Gson gson;


    private Callback<ReturnWS> callbackGetListCategorie = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS returnWS = response.body();
                if (returnWS.statusValid()) {

                    // On met à jour le singleton qui contient la liste des catégories
                    ListeCategories.setMyArrayListFromJson(returnWS.getMsg());

                    // Récupération de la liste JSON pour la mettre dans un ContentValue
                    Type listType = new TypeToken<ArrayList<Categorie>>() {
                    }.getType();
                    ArrayList<Categorie> categories = gson.fromJson(returnWS.getMsg(), listType);

                    mContentResolver.delete(ProviderContract.CategorieEntry.CONTENT_URI, null, null);
                    for (Categorie categorie :
                            categories) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(CategorieContract._ID, categorie.getIdCAT());
                        contentValues.put(CategorieContract.COL_NOM_CATEGORIE, categorie.getNameCAT());
                        contentValues.put(CategorieContract.COL_COULEUR_CATEGORIE, categorie.getNameCAT());
                        contentValues.put(CategorieContract.COL_NB_ANNONCE, categorie.getNbAnnonceCAT());
                        mContentResolver.insert(ProviderContract.CategorieEntry.CONTENT_URI, contentValues);
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            Log.d(TAG, t.getMessage());
        }
    };


    public AnnoncesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public AnnoncesSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs, ContentResolver mContentResolver) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        gson = new Gson();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        retrofitService = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitService.class);
        Call<ReturnWS> callGetListCategory = retrofitService.getListCategory();
        callGetListCategory.enqueue(callbackGetListCategorie);
    }
}
