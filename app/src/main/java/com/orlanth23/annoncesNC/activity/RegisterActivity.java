package com.orlanth23.annoncesnc.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.utility.PasswordEncryptionService;
import com.orlanth23.annoncesnc.utility.Utility;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByFragmentManager;

public class RegisterActivity extends CustomRetrofitCompatActivity {

    public static final int CODE_REGISTER_ACTIVITY = 100;
    private static final String tag = RegisterActivity.class.getName();
    @BindView(R.id.register_error)
    TextView errorMsg;
    @BindView(R.id.registerTelephone)
    EditText telephoneET;
    @BindView(R.id.registerEmail)
    EditText emailET;
    @BindView(R.id.registerPassword)
    EditText pwdET;
    @BindView(R.id.registerPasswordConfirm)
    TextView pwdConfirmET;
    @BindView(R.id.checkBox_register_remember_me)
    CheckBox checkBox_register_remember_me;
    private ProgressDialog prgDialog;
    private AppCompatActivity mActivity = this;

    private String mEmail;
    private Integer mTelephone;
    // Retour du webservice Register
    private Callback<ReturnWS> myCallback = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            String errorString="";
            if (response.isSuccessful()) {
                ReturnWS rs = response.body();
                prgDialog.hide();
                if (rs.statusValid()) {

                    // Récupération du numéro Id de l'utilisateur renvoyé par le WS
                    Integer idUser = rs.getIdServer();

                    // On remet les zones à blank
                    setDefaultValues();

                    // Display successfully registered message using Toast
                    Toast.makeText(mActivity, getString(R.string.dialog_register_ok), Toast.LENGTH_LONG).show();

                    // Récupération de l'utilisateur comme étant l'utilisateur courant
                    CurrentUser.getInstance().setIdUTI(idUser);
                    CurrentUser.getInstance().setEmailUTI(mEmail);
                    CurrentUser.getInstance().setTelephoneUTI(mTelephone);
                    CurrentUser.getInstance().setConnected(true);

                    // Si on a coché la case pour se souvenir de l'utilisateur
                    if (checkBox_register_remember_me.isChecked()) {
                        Utility.saveAutoComplete(mActivity, emailET, pwdET, checkBox_register_remember_me, idUser);
                    }

                    Utility.hideKeyboard(mActivity);

                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);                             // On retourne un résultat RESULT_OK
                    finish();                                                       // On finit l'activité
                } else {
                    switch (rs.getMsg()) {
                        case "ERROR_MAIL_ALREADY_EXIST":
                            errorString = getString(R.string.error_mail_already_exist);
                            emailET.setError(errorString);
                            emailET.requestFocus();
                            break;
                    }
                    errorMsg.setText(errorString);
                }
            } else {
                onFailureCallback();
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            onFailureCallback();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        // Rajout d'une toolbar et changement du titre
        ActionBar tb = getSupportActionBar();
        if (tb != null) {
            tb.setTitle(R.string.action_sign_up);
            tb.setDisplayHomeAsUpEnabled(true);
        }

        // Création d'une progress bar
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage(getString(R.string.dialog_msg_patience));
        prgDialog.setCancelable(false);
    }

    private void onFailureCallback() {
        prgDialog.hide();
        SendDialogByFragmentManager(getFragmentManager(), getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, tag);
    }

    public void setDefaultValues() {
        emailET.setText("");
        pwdET.setText("");
        pwdConfirmET.setText("");
        telephoneET.setText("");
    }

    private boolean checkRegister() {
        mTelephone = 0;
        View focusView = null;
        boolean cancel = false;

        // Récupération des variables présentes sur le layout
        // Téléphone, email et password
        String monTelephone = telephoneET.getText().toString();
        if (!monTelephone.equals("")) {
            mTelephone = Integer.parseInt(monTelephone);
        }
        mEmail = emailET.getText().toString().replace("'", "''");
        String password = pwdET.getText().toString().replace("'", "''");
        String passwordConfirm = pwdConfirmET.getText().toString().replace("'", "''");

        // When Email Edit View and Password Edit View have values other than Null
        if (!Utility.isNotNull(mEmail)) {
            emailET.setError(getString(R.string.error_field_required));
            focusView = emailET;
            cancel = true;
        }

        if (!Utility.isNotNull(String.valueOf(mTelephone))) {
            telephoneET.setError(getString(R.string.error_field_required));
            focusView = telephoneET;
            cancel = true;
        }

        if (!Utility.isNotNull(password)) {
            pwdET.setError(getString(R.string.error_field_required));
            focusView = pwdET;
            cancel = true;
        }

        if (!Utility.isNotNull(passwordConfirm)) {
            pwdConfirmET.setError(getString(R.string.error_field_required));
            focusView = pwdConfirmET;
            cancel = true;
        }

        // When Email entered is invalid
        if (!Utility.validateEmail(mEmail)) {
            emailET.setError(getString(R.string.error_invalid_email));
            focusView = emailET;
            cancel = true;
        }


        if (!password.equals(passwordConfirm)) {
            pwdConfirmET.setError(getString(R.string.error_incorrect_password));
            focusView = pwdConfirmET;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        }

        return !cancel;
    }

    public void register(View view) {

        int telephone = 0;

        if (checkRegister()) {

            // Récupération des variables présentes sur le layout
            // Nom, Prenom, Téléphone, email et password
            String monTelephone = telephoneET.getText().toString();
            if (!monTelephone.equals("")) {
                telephone = Integer.parseInt(monTelephone);
            }
            String email = emailET.getText().toString().replace("'", "''");
            String password = pwdET.getText().toString().replace("'", "''");
            final String motDePasseEncrypted = PasswordEncryptionService.desEncryptIt(password);

            prgDialog.show();

            Call<ReturnWS> call = retrofitService.register(email, motDePasseEncrypted, telephone);
            call.enqueue(myCallback);
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
}
