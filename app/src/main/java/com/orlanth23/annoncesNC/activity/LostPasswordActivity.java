package com.orlanth23.annoncesNC.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.database.DictionaryDAO;
import com.orlanth23.annoncesNC.dialogs.NoticeDialogFragment;
import com.orlanth23.annoncesNC.utility.Utility;
import com.orlanth23.annoncesNC.webservices.AccessPoint;
import com.orlanth23.annoncesNC.webservices.RetrofitService;
import com.orlanth23.annoncesNC.webservices.ReturnClass;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.orlanth23.annoncesNC.utility.Utility.SendDialogByFragmentManager;


public class LostPasswordActivity extends AppCompatActivity implements NoticeDialogFragment.NoticeDialogListener {

    private static final String tag = LostPasswordActivity.class.getName();

    // UI references.
    @Bind(R.id.email)
    AutoCompleteTextView mEmailView;
    @Bind(R.id.login_error)
    TextView errorMsg;

    private ProgressDialog prgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_password);
        ButterKnife.bind(this);

        // Création d'une progress dialog pour demander de patienter
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage(getString(R.string.dialog_msg_patience));

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(getString(R.string.action_lost_password));
        }

        // On récupère des données de la BD
        populateAutoComplete();
    }

    /**
     * Méthode de récupération des identifiants dans la base de données
     * <p/>
     * On va remplir automatiquement la zone mot de passe
     */
    private void populateAutoComplete() {
        mEmailView.setText(DictionaryDAO.getValueByKey(getApplicationContext(), DictionaryDAO.Dictionary.DB_CLEF_LOGIN));
    }


    /**
     * @param view
     */
    public void lostPassword(View view) {

        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().replace("'", "''");

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (!Utility.isNotNull(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!Utility.validate(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Si aucune erreur bloquante on continue et on appelle webservice pour savoir si l'utilisateur est connu
        if (cancel) {
            focusView.requestFocus();
        } else {

            // Création d'un RestAdapter pour le futur appel de mon RestService
            RetrofitService retrofitService = new RestAdapter.Builder().setEndpoint(AccessPoint.getDefaultServerEndpoint()).build().create(RetrofitService.class);
            Callback<ReturnClass> myCallback = new Callback<ReturnClass>() {
                @Override
                public void success(ReturnClass rs, Response response) {
                    prgDialog.hide();
                    if (rs.isStatus()) {

                        // Display successfully registered message using Toast
                        Toast.makeText(getApplicationContext(), getString(R.string.dialog_password_send), Toast.LENGTH_LONG).show();

                        // Si l'authentification a fonctionné, je peux quitter l'activité
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else {
                        errorMsg.setText(rs.getMsg());
                        Toast.makeText(getApplicationContext(), rs.getMsg(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    prgDialog.hide();
                    SendDialogByFragmentManager(getFragmentManager(), getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, tag);
                }
            };
            prgDialog.show();
            retrofitService.doLostPassword(email, myCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        prgDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        prgDialog.dismiss();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}

