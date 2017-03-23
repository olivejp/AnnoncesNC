package com.orlanth23.annoncesnc.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.database.DictionaryDAO;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.utility.PasswordEncryptionService;
import com.orlanth23.annoncesnc.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByFragmentManager;


public class LoginFirebaseActivity extends CustomRetrofitCompatActivity implements NoticeDialogFragment.NoticeDialogListener {

    private static final String TAG = LoginFirebaseActivity.class.getName();

    @BindView(R.id.email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.password)
    EditText mPasswordView;
    @BindView(R.id.checkBox_remember_me_login)
    CheckBox mCheckBoxRememberMe;
    @BindView(R.id.login_error)
    TextView errorMsg;
    @BindView(R.id.text_login_msg_accueil)
    TextView textLoginMsgAccueil;
    private Context mContext = this;
    private CustomRetrofitCompatActivity mActivity = this;
    private FirebaseAuth mAuth;
    private String password;
    private FirebaseUser firebaseUser;

    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        prgDialog.setMessage(getString(R.string.dialog_wait_login));
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

        // Get instance from FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
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

    private void goodFinishActivity() {
        Utility.hideKeyboard(mActivity);
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

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
        String email = mEmailView.getText().toString().replace("'", "''");
        password = mPasswordView.getText().toString().replace("'", "''");

        if (!Utility.checkWifiAndMobileData(this)) {
            // On envoie un message pour dire qu'on a pas de connexion réseau
            SendDialogByFragmentManager(getFragmentManager(),
                "Aucune connexion réseau disponible pour vous authentifier.",
                NoticeDialogFragment.TYPE_BOUTON_OK,
                NoticeDialogFragment.TYPE_IMAGE_ERROR,
                null);
            return;
        }

        if (!checkLogin(email, password)) {
            return;
        }

        // Création d'un nouveau listener pour la méthode SignIn
        OnCompleteListener<AuthResult> signInCompleteListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {

                        // Récupération du token de l'utilisateur
                        if (mCheckBoxRememberMe.isChecked()) {
                            Utility.saveAutoComplete(mActivity, firebaseUser.getUid(), firebaseUser.getEmail(), "", password);
                        }
                        Toast.makeText(mActivity, "Connecté avec le compte " + firebaseUser.getEmail() + " !", Toast.LENGTH_LONG).show();
                        goodFinishActivity();
                    }
                } else {
                    Exception e = task.getException();
                    if (e != null) {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                prgDialog.hide();
            }
        };

        // Si on a une connexion on tente de se connecter au serveur
        prgDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, signInCompleteListener);
    }


    @OnClick(R.id.lostPassword)
    public void callLostPasswordActivity() {
        Intent lostPasswordIntent = new Intent(getApplicationContext(), LostPasswordActivity.class);
        lostPasswordIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(lostPasswordIntent, 0);
    }

    @OnClick(R.id.login_btnRegister)
    public void callRegisterFirebaseActivity() {
        Intent registerIntent = new Intent(getApplicationContext(), RegisterFirebaseActivity.class);
        registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(registerIntent, RegisterFirebaseActivity.CODE_REGISTER_ACTIVITY);
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

