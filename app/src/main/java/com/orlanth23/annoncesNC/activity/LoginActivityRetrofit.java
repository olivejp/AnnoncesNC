package com.orlanth23.annoncesNC.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
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
import com.orlanth23.annoncesNC.webservices.ReturnWS;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.orlanth23.annoncesNC.utility.Utility.SendDialogByFragmentManager;


public class LoginActivityRetrofit extends CustomRetrofitCompatActivity implements NoticeDialogFragment.NoticeDialogListener {

    private static final String tag = LoginActivityRetrofit.class.getName();

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

    private CustomRetrofitCompatActivity mActivity = this;

    private Callback<ReturnWS> loginCallback = new Callback<ReturnWS>() {
        @Override
        public void success(ReturnWS retour, Response response) {
            prgDialog.hide();
            if (retour.statusValid()) {
                if (mCheckBoxRememberMe.isChecked()) {
                    Utility.saveAutoComplete(mActivity, mEmailView, mPasswordView, mCheckBoxRememberMe);
                }

                Gson gson = new Gson();
                Utilisateur user = gson.fromJson(retour.getMsg(), Utilisateur.class);

                // Récupération de l'utilisateur comme étant l'utilisateur courant
                CurrentUser.getInstance();
                CurrentUser.setUser(user);

                Toast.makeText(mActivity, "Connecté avec le compte " + CurrentUser.getInstance().getEmailUTI() + " !", Toast.LENGTH_LONG).show();

                goodFinishActivity();
            } else {
                errorMsg.setText(retour.getMsg());
                Toast.makeText(mActivity, retour.getMsg(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            prgDialog.hide();
            SendDialogByFragmentManager(getFragmentManager(), getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, tag);
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
    }

    public void callLostPasswordActivity(View view) {
        Intent lostPasswordIntent = new Intent(getApplicationContext(), LostPasswordActivity.class);
        lostPasswordIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(lostPasswordIntent, 0);
    }

    public void callRegisterActivity(View view) {
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
        String encryptedPassword = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_PASSWORD);
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

    private void goodFinishActivity(){
        Utility.hideKeyboard(mActivity);
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private boolean checkLogin(String email, String decryptedPassword){
        boolean condition;
        mEmailView.setError(null);
        mPasswordView.setError(null);

        condition = !TextUtils.isEmpty(decryptedPassword) && !Utility.isPasswordValid(decryptedPassword);
        if (Utility.isTextViewOnError(condition, mPasswordView, getString(R.string.error_invalid_password), true)){
            return false;
        }

        condition = !Utility.isNotNull(email);
        if (Utility.isTextViewOnError(condition, mEmailView, getString(R.string.error_field_required), true)) {
            return false;
        }

        condition = !Utility.validateEmail(email);
        return !Utility.isTextViewOnError(condition, mEmailView, getString(R.string.error_invalid_email), true);
    }

    public void attemptLogin(View view) {
        String email = mEmailView.getText().toString().replace("'", "''");
        String decryptedPassword = mPasswordView.getText().toString().replace("'", "''");

        if (checkLogin(email, decryptedPassword)){
            prgDialog.show();
            String encryptedPassword = PasswordEncryptionService.desEncryptIt(decryptedPassword);
            retrofitService.login(email, encryptedPassword, loginCallback);
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
