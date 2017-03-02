package com.orlanth23.annoncesnc.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orlanth23.annoncesnc.BuildConfig;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.list.ListeCategories;
import com.orlanth23.annoncesnc.list.ListeStats;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByFragmentManager;
import static com.orlanth23.annoncesnc.utility.Utility.checkWifiAndMobileData;

public class FullscreenActivity extends CustomRetrofitCompatActivity implements NoticeDialogFragment.NoticeDialogListener {

    private static final String DIALOG_TAG_NO_CONNECTION = "NO_CONNECTION";
    private static final String DIALOG_TAG_NO_SERVER = "NO_SERVER";
    private static final String DIALOG_TAG_NO_CAT_LIST = "NO_CAT_LIST";
    private static final String DIALOG_TAG_NO_STATS = "NO_STATS";
    private static final String BUNDLE_INTENT_OK = "BUNDLE_INTENT_OK";

    @BindView(R.id.text_version)
    TextView text_version;
    @BindView(R.id.textTestServer)
    TextView textTestServer;
    @BindView(R.id.textGetInfos)
    TextView textGetInfos;
    @BindView(R.id.textGetStats)
    TextView textGetStats;
    @BindView(R.id.imgTestNetwork)
    ImageView imgTestNetwork;
    @BindView(R.id.imgTestServer)
    ImageView imgTestServer;
    @BindView(R.id.imgGetInfos)
    ImageView imgGetInfos;
    @BindView(R.id.imgGetStats)
    ImageView imgGetStats;

    private boolean P_OK = false;
    private Callback<ReturnWS> getNbAnnonce4Callback = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS retour = response.body();
                if (retour.statusValid()) {
                    P_OK = true;
                    ListeStats.setNbAnnonces(Integer.valueOf(retour.getMsg()));
                    imgGetStats.setImageResource(R.drawable.ic_action_accept);
                    startMainActivity();
                }else{
                    onFailureCallback( R.string.error_no_stats, imgGetStats);
                }
            }else{
                onFailureCallback( R.string.error_no_stats, imgGetStats);
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            onFailureCallback( R.string.error_no_stats, imgGetStats);
        }
    };
    private Callback<ReturnWS> getNbUser3Callback = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS retour = response.body();
                if (retour.statusValid()) {
                    ListeStats.setNbUsers(Integer.valueOf(retour.getMsg()));
                    Call<ReturnWS> callGetNbAnnonce = retrofitService.getNbAnnonce();
                    callGetNbAnnonce.enqueue(getNbAnnonce4Callback);
                }else{
                    onFailureCallback(R.string.error_no_stats, null);
                }
            }else{
                onFailureCallback(R.string.error_no_stats, null);
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            onFailureCallback(R.string.error_no_stats, null);
        }
    };
    private Callback<ReturnWS> listCategorie2Callback = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS retour = response.body();
                if (retour.statusValid()) {
                    imgGetInfos.setImageResource(R.drawable.ic_action_accept);
                    ListeCategories.setNbAnnonceFromJson(retour.getMsg());
                    imgGetStats.setVisibility(View.VISIBLE);
                    Call<ReturnWS> callGetNbUser = retrofitService.getNbUser();
                    callGetNbUser.enqueue(getNbUser3Callback);
                }else{
                    onFailureCallback(R.string.error_no_cat_list, imgGetInfos);
                }
            }else{
                onFailureCallback(R.string.error_no_cat_list, imgGetInfos);
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            onFailureCallback(R.string.error_no_cat_list, imgGetInfos);
        }
    };
    private Callback<ReturnWS> checkConnection1Callback = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS retour = response.body();
                if (retour.statusValid()) {
                    imgTestServer.setImageResource(R.drawable.ic_action_accept);
                    imgGetInfos.setVisibility(View.VISIBLE);
                    Call<ReturnWS> callGetListCategorie = retrofitService.getListCategory();
                    callGetListCategorie.enqueue(listCategorie2Callback);
                }else{
                    onFailureCallback(R.string.error_no_server, imgTestServer);
                }
            }else{
                onFailureCallback(R.string.error_no_server, imgTestServer);
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            onFailureCallback(R.string.error_no_server, imgTestServer);
        }
    };

    private void startMainActivity(){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void onFailureCallback(@StringRes int intRes, @Nullable ImageView imageView){
        if (imageView != null) {
            imageView.setImageResource(R.drawable.ic_remove_inverse);
        }
        SendDialogByFragmentManager(getFragmentManager(), getString(intRes), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, DIALOG_TAG_NO_STATS);
    }

    private void testAllWebservices() {
        imgTestNetwork.setVisibility(View.VISIBLE);
        if (checkWifiAndMobileData(this)) {
            imgTestNetwork.setImageResource(R.drawable.ic_action_accept);
            imgTestServer.setVisibility(View.VISIBLE);
            retrofitService.checkConnection().enqueue(checkConnection1Callback);
        } else {
            imgTestNetwork.setImageResource(R.drawable.ic_remove_inverse);
            SendDialogByFragmentManager(getFragmentManager(), getString(R.string.error_no_wifi), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, DIALOG_TAG_NO_CONNECTION);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        ButterKnife.bind(this);

        text_version.setText(BuildConfig.VERSION_NAME);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Récupération du paramètre
        if (savedInstanceState != null) {
            P_OK = savedInstanceState.getBoolean(BUNDLE_INTENT_OK);
        }

        // Instanciation des singletons
        ListeStats.getInstance();
        ListeCategories.getInstance(this);

        // Appel de tous les webservices
        // testAllWebservices();

        // Démarrage de l'activity principale
        startMainActivity();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_INTENT_OK, P_OK);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_NO_CONNECTION:
            case DIALOG_TAG_NO_SERVER:
            case DIALOG_TAG_NO_CAT_LIST:
            case DIALOG_TAG_NO_STATS:
            default:
                finish();
                break;
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        throw new UnsupportedOperationException();
    }
}
