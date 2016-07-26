package com.orlanth23.annoncesNC.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orlanth23.annoncesNC.BuildConfig;
import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.dialogs.NoticeDialogFragment;
import com.orlanth23.annoncesNC.dto.Categorie;
import com.orlanth23.annoncesNC.lists.ListeCategories;
import com.orlanth23.annoncesNC.lists.ListeStats;
import com.orlanth23.annoncesNC.webservices.AccessPoint;
import com.orlanth23.annoncesNC.webservices.RetrofitService;
import com.orlanth23.annoncesNC.webservices.ReturnClass;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.orlanth23.annoncesNC.utility.Utility.SendDialogByFragmentManager;
import static com.orlanth23.annoncesNC.utility.Utility.checkNetwork;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements NoticeDialogFragment.NoticeDialogListener {

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
    private RetrofitService retrofitService;
    private retrofit.Callback<ReturnClass> callbackCheckConnection = new retrofit.Callback<ReturnClass>() {
        // ------------------------------------------------
        // Vérification de la connection au serveur
        @Override
        public void success(ReturnClass returnClass, Response response) {
            if (returnClass.isStatus()) {
                imgTestServer.setImageResource(R.drawable.ic_action_accept);
                imgGetInfos.setVisibility(View.VISIBLE);
                retrofitService.listcategorie(callbackListCategorie);
            } else {
                if (backupService(imgTestServer, textTestServer, R.string.text_test_backup_server)){
                    retrofitService.checkConnection(callbackCheckConnection);
                }else{
                    SendDialogByFragmentManager(getFragmentManager(), getString(R.string.error_no_server), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, DIALOG_TAG_NO_SERVER);
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (backupService(imgTestServer, textTestServer, R.string.text_test_backup_server)){
                retrofitService.checkConnection(callbackCheckConnection);
            }else{
                SendDialogByFragmentManager(getFragmentManager(), getString(R.string.error_no_server), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, DIALOG_TAG_NO_SERVER);
            }
        }
    };
    private retrofit.Callback<ReturnClass> callbackGetNbAnnonce = new retrofit.Callback<ReturnClass>() {
        @Override
        public void success(ReturnClass rs, Response response) {
            if (rs.isStatus()) {
                P_OK = true;
                ListeStats.setNbAnnonces(Integer.valueOf(rs.getMsg()));

                imgGetStats.setImageResource(R.drawable.ic_action_accept);

                // Lancement de l'activité principale
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else {
                if (backupService(imgGetStats, textGetStats, R.string.text_test_backup_server)){
                    retrofitService.checkConnection(callbackCheckConnection);
                }else{
                    SendDialogByFragmentManager(getFragmentManager(), getString(R.string.error_no_stats), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, DIALOG_TAG_NO_STATS);
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (backupService(imgGetStats, textGetStats, R.string.text_test_backup_server)){
                retrofitService.getNbAnnonce(callbackGetNbAnnonce);
            }else{
                SendDialogByFragmentManager(getFragmentManager(), getString(R.string.error_no_stats), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, DIALOG_TAG_NO_STATS);
            }
        }
    };
    private retrofit.Callback<ReturnClass> callbackGetNbUser = new retrofit.Callback<ReturnClass>() {
        @Override
        public void success(ReturnClass rs, Response response) {
            if (rs.isStatus()) {
                ListeStats.setNbUsers(Integer.valueOf(rs.getMsg()));

                // ------------------------------------------------
                // Récupération du nombre d'annonce
                retrofitService.getNbAnnonce(callbackGetNbAnnonce);
            } else {
                if (backupService(imgGetStats, textGetStats, R.string.text_test_backup_server)){
                    retrofitService.checkConnection(callbackCheckConnection);
                }else{
                    SendDialogByFragmentManager(getFragmentManager(), getString(R.string.error_no_stats), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, DIALOG_TAG_NO_STATS);
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (backupService(imgGetStats, textGetStats, R.string.text_test_backup_server)){
                retrofitService.getNbUser(callbackGetNbUser);
            }else{
                SendDialogByFragmentManager(getFragmentManager(), getString(R.string.error_no_stats), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, DIALOG_TAG_NO_STATS);
            }
        }
    };
    private retrofit.Callback<ReturnClass> callbackListCategorie = new retrofit.Callback<ReturnClass>() {

        // ------------------------------------------------
        // Récupération de la liste de catégorie
        @Override
        public void success(ReturnClass rs, Response response) {
            if (rs.isStatus()) {
                imgGetInfos.setImageResource(R.drawable.ic_action_accept);

                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Categorie>>() {
                }.getType();
                ArrayList<Categorie> categories = gson.fromJson(rs.getMsg(), listType);

                // On réceptionne la liste des catégories dans l'instance ListeCategories
                ListeCategories.setMyArrayList(categories);

                // ------------------------------------------------
                // Récupération du nombre d'utilisateur
                imgGetStats.setVisibility(View.VISIBLE);
                retrofitService.getNbUser(callbackGetNbUser);
            } else {
                if (backupService(imgGetInfos, textGetInfos, R.string.text_test_backup_server)){
                    retrofitService.checkConnection(callbackCheckConnection);
                }else{
                    SendDialogByFragmentManager(getFragmentManager(), getString(R.string.error_no_cat_list), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, DIALOG_TAG_NO_CAT_LIST);
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (backupService(imgGetInfos, textGetInfos, R.string.text_test_backup_server)){
                retrofitService.listcategorie(callbackListCategorie);
            }else{
                SendDialogByFragmentManager(getFragmentManager(), getString(R.string.error_no_cat_list), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, DIALOG_TAG_NO_CAT_LIST);
            }
        }
    };

    private void definitionRetrofitBuilder(){
        retrofitService = new RestAdapter.Builder().setEndpoint(AccessPoint.getDefaultServerEndpoint()).build().create(RetrofitService.class);
    }

    private boolean backupService(ImageView imgView, TextView textView, int resourceString){
        imgView.setImageResource(R.drawable.ic_remove_inverse);
        if (!AccessPoint.isBackUp()) {
            // -----------------------------------------------
            // TENTATIVE DE CONNEXION AU SERVEUR DE SECOURS
            AccessPoint.getBackUpServer();
            textView.setText(resourceString);
            definitionRetrofitBuilder();
            return true;
        } else {
            imgView.setImageResource(R.drawable.ic_remove_inverse);
            return false;
        }
    }

    private void callWebservices() {

        // Instanciation d'un RestAdapter
        definitionRetrofitBuilder();

        // ------------------------------------------------
        // Vérification de la connection au réseau
        imgTestNetwork.setVisibility(View.VISIBLE);
        if (checkNetwork(this)) {
            imgTestNetwork.setImageResource(R.drawable.ic_action_accept);
            imgTestServer.setVisibility(View.VISIBLE);
            retrofitService.checkConnection(callbackCheckConnection);
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

        // On désactive l'action bar
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
        AccessPoint.getInstance();

        // A la création de l'activity, il faut toujours aller sur le serveur principal en priorité
        if (AccessPoint.isBackUp()) {
            AccessPoint.getPrincipalServer();
        }

        // Appel de tous les webservices
        callWebservices();
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
                finish();
                break;
            case DIALOG_TAG_NO_SERVER:
                finish();
                break;
            case DIALOG_TAG_NO_CAT_LIST:
                finish();
                break;
            case DIALOG_TAG_NO_STATS:
                finish();
                break;
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}
