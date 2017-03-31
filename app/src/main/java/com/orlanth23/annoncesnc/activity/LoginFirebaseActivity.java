package com.orlanth23.annoncesnc.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.database.DictionaryDAO;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.dto.Utilisateur;
import com.orlanth23.annoncesnc.interfaces.CustomLostPasswordCallback;
import com.orlanth23.annoncesnc.interfaces.CustomUserSignCallback;
import com.orlanth23.annoncesnc.service.UserService;
import com.orlanth23.annoncesnc.utility.PasswordEncryptionService;
import com.orlanth23.annoncesnc.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByFragmentManager;

public class LoginFirebaseActivity extends CustomCompatActivity
    implements NoticeDialogFragment.NoticeDialogListener, CustomUserSignCallback, CustomLostPasswordCallback {

    private static final String TAG = LoginFirebaseActivity.class.getName();

    @BindView(R.id.email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.password)
    EditText mPasswordView;
    @BindView(R.id.login_error)
    TextView errorMsg;
    @BindView(R.id.text_login_msg_accueil)
    TextView textLoginMsgAccueil;
    @BindView(R.id.img_profile)
    ImageView imageProfile;

    private CustomCompatActivity mActivity = this;
    private String mPassword;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_2);
        ButterKnife.bind(this);
        changeActionBarTitle(R.string.action_log_in, true);
        populateAutoComplete();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            switch (bundle.getInt(MainActivity.PARAM_REQUEST_CODE)) {
                case MainActivity.CODE_POST_NOT_LOGGED:
                    textLoginMsgAccueil.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            textLoginMsgAccueil.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RegisterFirebaseActivity.CODE_REGISTER_ACTIVITY:
                if (resultCode == RESULT_OK) {
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

    private void populateAutoComplete() {
        String encryptedPassword = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_MOT_PASSE);
        if (encryptedPassword != null) {
            String decryptedPassword = PasswordEncryptionService.desDecryptIt(encryptedPassword);
            mPasswordView.setText(decryptedPassword);
        }

        String email = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_EMAIL);
        if (email != null) {
            mEmailView.setText(email);
        }
    }

    private void goodFinishActivity() {
        Utility.hideKeyboard(mActivity);
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void getDataFromView(){
        mEmail = mEmailView.getText().toString().replace("'", "''");
        mPassword = mPasswordView.getText().toString().replace("'", "''");
    }

    /**
     * Verification que les champs soient bien renseignes avant de passer à l'authentification.
     * On vérifie que l'email soit assez long >= 4 caractères
     * On vérifie que le champ email ne soit pas vide
     * On vérifie que l'email est valide
     *
     * @param email             L'email de l'utilisateur
     * @param decryptedPassword Mot de passe en clair
     * @return True si toute les vérifications sont passées sans problème
     * False sinon
     */
    private boolean checkLogin(String email, String decryptedPassword) {
        boolean condition;
        mEmailView.setError(null);
        mPasswordView.setError(null);

        condition = !TextUtils.isEmpty(decryptedPassword) && !Utility.isPasswordValid(decryptedPassword);
        if (Utility.isTextViewOnError(condition, mPasswordView, getString(R.string.error_invalid_password), true)) {
            return false;
        }

        condition = !Utility.isNotNull(email);
        if (Utility.isTextViewOnError(condition, mEmailView, getString(R.string.error_field_required), true)) {
            return false;
        }

        condition = !Utility.validateEmail(email);
        return !Utility.isTextViewOnError(condition, mEmailView, getString(R.string.error_invalid_email), true);
    }

    @OnClick(R.id.login_btnLogin)
    public void attemptLogin() {
        Utility.hideKeyboard(this);

        getDataFromView();

        // On envoie un message d'erreur s'il n'y a pas de connexion
        if (!Utility.checkWifiAndMobileData(this)) {
            // On envoie un message pour dire qu'on a pas de connexion réseau
            SendDialogByFragmentManager(getFragmentManager(),
                "Aucune connexion réseau disponible pour vous authentifier.",
                NoticeDialogFragment.TYPE_BOUTON_OK,
                NoticeDialogFragment.TYPE_IMAGE_ERROR,
                null);
            return;
        }

        if (!checkLogin(mEmail, mPassword)) {
            return;
        }

        // Si on a une connexion on tente de se connecter au serveur
        prgDialog.setMessage("Authentification en cours.");
        prgDialog.show();
        UserService.sign(mAuth, mDatabase, mActivity, mEmail, mPassword, this);
    }

    @OnClick(R.id.lostPassword)
    public void callLostPassword() {
        Utility.hideKeyboard(this);

        getDataFromView();

        if (Utility.checkWifiAndMobileData(this)){
            prgDialog.setMessage("Envoi d'un message sur votre adresse mail.");
            prgDialog.show();
            UserService.lostPassword(mAuth, mActivity, mEmail, this);
        } else {
            SendDialogByFragmentManager(getFragmentManager(),
                "Aucune connexion réseau disponible pour vous authentifier.",
                NoticeDialogFragment.TYPE_BOUTON_OK,
                NoticeDialogFragment.TYPE_IMAGE_ERROR,
                null);
        }
    }

    @OnClick(R.id.login_btnRegister)
    public void callRegisterFirebaseActivity() {
        if (Utility.checkWifiAndMobileData(this)) {
            getDataFromView();

            Bundle bundle = new Bundle();
            bundle.putString(RegisterFirebaseActivity.EMAIL, mEmail);

            Intent registerIntent = new Intent(getApplicationContext(), RegisterFirebaseActivity.class);
            registerIntent.putExtras(bundle);
            registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(registerIntent, RegisterFirebaseActivity.CODE_REGISTER_ACTIVITY);
        } else {
            // On envoie un message pour dire qu'on a pas de connexion réseau
            SendDialogByFragmentManager(getFragmentManager(),
                "Aucune connexion réseau disponible pour vous authentifier.",
                NoticeDialogFragment.TYPE_BOUTON_OK,
                NoticeDialogFragment.TYPE_IMAGE_ERROR,
                null);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) { }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) { }

    @Override
    public void onCompleteUserSign(Utilisateur user) {
        prgDialog.dismiss();
        CurrentUser.getInstance().setUser(user);
        goodFinishActivity();
    }

    @Override
    public void onCancelledUserSign() {
        prgDialog.dismiss();
    }

    @Override
    public void onFailureUserSign() {
        prgDialog.dismiss();
    }

    @Override
    public void onCompleteLostPassword() {
        prgDialog.dismiss();
        goodFinishActivity();
    }

    @Override
    public void onFailureLostPassword() {
        prgDialog.dismiss();
    }
}

