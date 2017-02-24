package com.orlanth23.annoncesnc.activity;

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
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.database.DictionaryDAO;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.dto.Utilisateur;
import com.orlanth23.annoncesnc.utility.PasswordEncryptionService;
import com.orlanth23.annoncesnc.utility.Utility;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByFragmentManager;


public class LoginActivityRetrofit extends CustomRetrofitCompatActivity implements NoticeDialogFragment.NoticeDialogListener {

    private static final String tag = LoginActivityRetrofit.class.getName();

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

    private CustomRetrofitCompatActivity mActivity = this;

    private Callback<ReturnWS> loginCallback = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            prgDialog.hide();
            if (response.isSuccessful()) {
                ReturnWS rs = response.body();
                if (rs.statusValid()) {
                    if (mCheckBoxRememberMe.isChecked()) {
                        Utility.saveAutoComplete(mActivity, mEmailView, mPasswordView, mCheckBoxRememberMe);
                    }
                    Gson gson = new Gson();
                    Utilisateur user = gson.fromJson(rs.getMsg(), Utilisateur.class);

                    // Récupération de l'utilisateur comme étant l'utilisateur courant
                    CurrentUser.getInstance();
                    CurrentUser.setUser(user);

                    Toast.makeText(mActivity, "Connecté avec le compte " + CurrentUser.getInstance().getEmailUTI() + " !", Toast.LENGTH_LONG).show();
                    goodFinishActivity();
                } else {
                    errorMsg.setText(rs.getMsg());
                    Toast.makeText(mActivity, rs.getMsg(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
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
        String decryptedPassword = mPasswordView.getText().toString().replace("'", "''");

        if (checkLogin(email, decryptedPassword)) {
            prgDialog.show();
            String encryptedPassword = PasswordEncryptionService.desEncryptIt(decryptedPassword);
            Call<ReturnWS> callLogin = retrofitService.login(email, encryptedPassword);
            callLogin.enqueue(loginCallback);
        }
    }

    @OnClick(R.id.lostPassword)
    public void callLostPasswordActivity() {
        Intent lostPasswordIntent = new Intent(getApplicationContext(), LostPasswordActivity.class);
        lostPasswordIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(lostPasswordIntent, 0);
    }

    @OnClick(R.id.login_btnRegister)
    public void callRegisterActivity() {
        Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(registerIntent, RegisterActivity.CODE_REGISTER_ACTIVITY);
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

