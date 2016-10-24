package com.orlanth23.annoncesNC.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orlanth23.annoncesNC.BuildConfig;
import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.dialogs.NoticeDialogFragment;
import com.orlanth23.annoncesNC.lists.ListeCategories;
import com.orlanth23.annoncesNC.lists.ListeStats;
import com.orlanth23.annoncesNC.webservices.AccessPoint;
import com.orlanth23.annoncesNC.webservices.RetrofitService;
import com.orlanth23.annoncesNC.webservices.ReturnWS;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.orlanth23.annoncesNC.utility.Utility.SendDialogByFragmentManager;
import static com.orlanth23.annoncesNC.utility.Utility.checkWifiAndMobileData;

public class FullscreenActivity extends CustomRetrofitCompatActivity implements NoticeDialogFragment.NoticeDialogListener {

    private static final String DIALOG_TAG_NO_CONNECTION = "NO_CONNECTION";
    private static final String DIALOG_TAG_NO_SERVER = "NO_SERVER";
    private static final String DIALOG_TAG_NO_CAT_LIST = "NO_CAT_LIST";
    private static final String DIALOG_TAG_NO_STATS = "NO_STATS";
    private static final String BUNDLE_INTENT_OK = "BUNDLE_INTENT_OK";

    @Bind(R.id.text_version)
    TextView text_version;
    @Bind(R.id.textTestServer)
    TextView textTestServer;
    @Bind(R.id.textGetInfos)
    TextView textGetInfos;
    @Bind(R.id.textGetStats)
    TextView textGetStats;
    @Bind(R.id.imgTestNetwork)
    ImageView imgTestNetwork;
    @Bind(R.id.imgTestServer)
    ImageView imgTestServer;
    @Bind(R.id.imgGetInfos)
    ImageView imgGetInfos;
    @Bind(R.id.imgGetStats)
    ImageView imgGetStats;

    private boolean P_OK = false;
    private AccessPoint mAccessPoint = AccessPoint.getInstance();

    private void startMainActivity(){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void tryConnectToBackUpServer(TextView textView, ImageView imageView,  int resIdMsgError){
        imageView.setImageResource(R.drawable.ic_remove_inverse);
        textView.setText(resIdMsgError);
        if (goToBackupServer()){
            retrofitService.checkConnection(checkConnection1Callback);
        }else{
            SendDialogByFragmentManager(getFragmentManager(), getString(R.string.error_no_server), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, DIALOG_TAG_NO_SERVER);
        }
    }

    private retrofit.Callback<ReturnWS> checkConnection1Callback = new retrofit.Callback<ReturnWS>() {
        @Override
        public void success(ReturnWS retour, Response response) {
            if (retour.statusValid()) {
                imgTestServer.setImageResource(R.drawable.ic_action_accept);
                imgGetInfos.setVisibility(View.VISIBLE);
                retrofitService.getListCategory(listCategorie2Callback);
            } else {
                tryConnectToBackUpServer(textTestServer, imgTestServer, R.string.error_no_server);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            tryConnectToBackUpServer(textTestServer, imgTestServer, R.string.error_no_server);
        }
    };

    private retrofit.Callback<ReturnWS> getNbAnnonce4Callback = new retrofit.Callback<ReturnWS>() {
        @Override
        public void success(ReturnWS retour, Response response) {
            if (retour.statusValid()) {
                P_OK = true;
                ListeStats.setNbAnnonces(Integer.valueOf(retour.getMsg()));
                imgGetStats.setImageResource(R.drawable.ic_action_accept);
                startMainActivity();
            } else {
                tryConnectToBackUpServer(textGetStats, imgGetStats, R.string.error_no_stats);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            tryConnectToBackUpServer(textGetStats, imgGetStats, R.string.error_no_stats);
        }
    };
    private retrofit.Callback<ReturnWS> getNbUser3Callback = new retrofit.Callback<ReturnWS>() {
        @Override
        public void success(ReturnWS retour, Response response) {
            if (retour.statusValid()) {
                ListeStats.setNbUsers(Integer.valueOf(retour.getMsg()));
                retrofitService.getNbAnnonce(getNbAnnonce4Callback);
            } else {
                tryConnectToBackUpServer(textGetInfos, imgGetInfos, R.string.error_no_stats);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            tryConnectToBackUpServer(textGetInfos, imgGetInfos, R.string.error_no_stats);
        }
    };
    private retrofit.Callback<ReturnWS> listCategorie2Callback = new retrofit.Callback<ReturnWS>() {
        @Override
        public void success(ReturnWS retour, Response response) {
            if (retour.statusValid()) {
                imgGetInfos.setImageResource(R.drawable.ic_action_accept);
                ListeCategories.setMyArrayListFromJson(retour.getMsg());
                imgGetStats.setVisibility(View.VISIBLE);
                retrofitService.getNbUser(getNbUser3Callback);
            } else {
                tryConnectToBackUpServer(textGetInfos, imgGetInfos, R.string.error_no_cat_list);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            tryConnectToBackUpServer(textGetInfos, imgGetInfos, R.string.error_no_cat_list);
        }
    };

    private void initRetrofitAccessPoint(){
        retrofitService = new RestAdapter.Builder().setEndpoint(mAccessPoint.getServerEndpoint()).build().create(RetrofitService.class);
    }

    private boolean goToBackupServer(){
        if (!mAccessPoint.isBackUp()) {
            mAccessPoint.changeToBackUpServer();
            initRetrofitAccessPoint();
            return true;
        } else {
            return false;
        }
    }

    private void testAllWebservices() {
        initRetrofitAccessPoint();

        imgTestNetwork.setVisibility(View.VISIBLE);
        if (checkWifiAndMobileData(this)) {
            imgTestNetwork.setImageResource(R.drawable.ic_action_accept);
            imgTestServer.setVisibility(View.VISIBLE);
            retrofitService.checkConnection(checkConnection1Callback);
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
        ListeCategories.getInstance();

        // A la création de l'activity, il faut toujours aller sur le serveur principal en priorité
        if (mAccessPoint.isBackUp()) {
            mAccessPoint.changeToPrincipalServer();
        }

        // Appel de tous les webservices
        testAllWebservices();
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
                finish();
                break;
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}
