package com.orlanth23.annoncesNC.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.database.DictionaryDAO;
import com.orlanth23.annoncesNC.dialogs.NoticeDialogFragment;
import com.orlanth23.annoncesNC.dto.CurrentUser;
import com.orlanth23.annoncesNC.dto.Utilisateur;
import com.orlanth23.annoncesNC.utility.PasswordEncryptionService;
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


public class LoginActivity extends AppCompatActivity implements NoticeDialogFragment.NoticeDialogListener {

    private static final String tag = LoginActivity.class.getName();

    // UI references.
    @Bind(R.id.email)
    AutoCompleteTextView mEmailView;
    @Bind(R.id.password)
    EditText mPasswordView;
    @Bind(R.id.checkBox_remember_me_login)
    CheckBox mCheckBoxRememberMe;
    @Bind(R.id.login_error)
    TextView errorMsg;
    @Bind(R.id.text_login_msg_accueil)
    TextView textLoginMsgAccueil;

    private AppCompatActivity mActivity = this;
    private ProgressDialog prgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Bundle b = getIntent().getExtras();
        if (b != null){
            switch (b.getInt(MainActivity.PARAM_REQUEST_CODE)){
                case MainActivity.CODE_POST_NOT_LOGGED:
                    textLoginMsgAccueil.setVisibility(View.VISIBLE);
                    break;
            }
        }else{
            textLoginMsgAccueil.setVisibility(View.GONE);
        }

        // Création d'une progress dialog pour demander de patienter
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage(getString(R.string.dialog_wait_login));

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(getString(R.string.action_log_in));
        }

        // On récupère des données de la BD
        populateAutoComplete();
    }

    /**
     * @param view
     */
    public void lostPassword(View view) {
        Intent lostPasswordIntent = new Intent(getApplicationContext(), LostPasswordActivity.class);
        lostPasswordIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(lostPasswordIntent, 0);
    }

    /**
     * @param view
     */
    public void registerUser(View view) {
        Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(registerIntent, RegisterActivity.CODE_REGISTER_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RegisterActivity.CODE_REGISTER_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    // On retourne un résultat RESULT_OK
                    Intent returnIntent = new Intent();
                    if (getParent() == null) {
                        setResult(resultCode, returnIntent);
                    } else {
                        getParent().setResult(resultCode, returnIntent);
                    }
                    finish();
                }
        }
    }


    /**
     * Méthode de récupération des identifiants dans la base de données
     * <p/>
     * On va remplir automatiquement la zone mot de passe et email
     */
    private void populateAutoComplete() {
        // On décrypte le mot de passe, s'il y en a un
        String encryptedPassword = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_PASSWORD);
        if (encryptedPassword != null) {
            String decryptedPassword = PasswordEncryptionService.desDecryptIt(encryptedPassword);
            mPasswordView.setText(decryptedPassword);
        }

        // On met les données dans les zones écrans
        String email = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_LOGIN);
        if (email != null) {
            mEmailView.setText(email);
        }

        String autoReconnect = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_AUTO_CONNECT);
        if (autoReconnect != null) {
            mCheckBoxRememberMe.setChecked(autoReconnect.equals("O"));
        } else {
            mCheckBoxRememberMe.setChecked(false);
        }
    }

    /**
     * @param view
     */
    public void attemptLogin(View view) {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().replace("'", "''");
        String decryptedPassword = mPasswordView.getText().toString().replace("'", "''");

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(decryptedPassword) && !Utility.isPasswordValid(decryptedPassword)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

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

                        // Si on a coché la case pour se souvenir de l'utilisateur
                        if (mCheckBoxRememberMe.isChecked()) {
                            Utility.saveAutoComplete(mActivity, mEmailView, mPasswordView, mCheckBoxRememberMe);
                        }

                        // Récupération de l'utilisateur renvoyé par le webservice
                        Gson gson = new Gson();
                        Utilisateur user = gson.fromJson(rs.getMsg(), Utilisateur.class);

                        // Récupération de l'utilisateur comme étant l'utilisateur courant
                        CurrentUser.getInstance().setIdUTI(user.getIdUTI());
                        CurrentUser.getInstance().setEmailUTI(user.getEmailUTI());
                        CurrentUser.getInstance().setTelephoneUTI(user.getTelephoneUTI());
                        CurrentUser.setConnected(true);

                        // Display successfully registered message using Toast
                        Toast.makeText(mActivity, "Connecté avec le compte " + CurrentUser.getInstance().getEmailUTI() + " !", Toast.LENGTH_LONG).show();

                        // On range le clavier
                        Utility.hideKeyboard(mActivity);

                        // Si l'authentification a fonctionné, je peux quitter l'activité
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else {
                        errorMsg.setText(rs.getMsg());
                        Toast.makeText(mActivity, rs.getMsg(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    prgDialog.hide();
                    SendDialogByFragmentManager(getFragmentManager(), getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, tag);
                }
            };
            prgDialog.show();
            String encryptedPassword = PasswordEncryptionService.desEncryptIt(decryptedPassword);
            retrofitService.doLogin(email, encryptedPassword, myCallback);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

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
}

