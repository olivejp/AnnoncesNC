package com.orlanth23.annoncesNC.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.dialogs.NoticeDialogFragment;
import com.orlanth23.annoncesNC.dto.CurrentUser;
import com.orlanth23.annoncesNC.utility.PasswordEncryptionService;
import com.orlanth23.annoncesNC.utility.Utility;
import com.orlanth23.annoncesNC.webservices.AccessPoint;
import com.orlanth23.annoncesNC.webservices.RetrofitService;
import com.orlanth23.annoncesNC.webservices.ReturnClass;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.orlanth23.annoncesNC.utility.Utility.SendDialogByFragmentManager;

public class ChangePasswordActivity extends AppCompatActivity {

    public static final String tag = ChangePasswordActivity.class.getName();
    @Bind(R.id.oldPassword)
    EditText oldPassword;
    @Bind(R.id.newPassword)
    EditText newPassword;
    @Bind(R.id.newPasswordConfirm)
    EditText newPasswordConfirm;
    private ProgressDialog prgDialog;
    private AppCompatActivity mActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);

        // Rajout d'une toolbar et changement du titre
        ActionBar tb = getSupportActionBar();
        if (tb != null) {
            tb.setTitle(R.string.change_password);
            tb.setDisplayHomeAsUpEnabled(true);
        }

        // Création d'une progress bar
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage(getString(R.string.dialog_msg_patience));
        prgDialog.setCancelable(false);
    }

    public void changePassword(View view) {
        if (checkChangePassword()) {
            // Lancement du webservice pour modification de mot de passe
            // Appel du RETROFIT Webservice
            String oldPass = oldPassword.getText().toString().replace("'", "''");
            String newPass = newPassword.getText().toString().replace("'", "''");
            String oldPasswordEncrypted = PasswordEncryptionService.desEncryptIt(oldPass);
            String newPasswordEncrypted = PasswordEncryptionService.desEncryptIt(newPass);

            RetrofitService retrofitService = new RestAdapter.Builder().setEndpoint(AccessPoint.getENDPOINT()).build().create(RetrofitService.class);
            retrofit.Callback<ReturnClass> myCallback = new retrofit.Callback<ReturnClass>() {
                @Override
                public void success(ReturnClass rs, Response response) {
                    prgDialog.hide();
                    if (rs.isStatus()) {

                        // Display successfully registered message using Toast
                        Toast.makeText(mActivity, getString(R.string.dialog_register_ok), Toast.LENGTH_LONG).show();

                        // On cache le clavier
                        Utility.hideKeyboard(mActivity);

                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);                             // On retourne un résultat RESULT_OK
                        finish();                                                       // On finit l'activité
                    } else {
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

            // Lancement du webservice
            retrofitService.postChangePassword(CurrentUser.getInstance().getIdUTI(), oldPasswordEncrypted, newPasswordEncrypted, myCallback);
        }
    }

    private boolean checkChangePassword() {
        View focusView = null;
        boolean cancel = false;

        // Récupération des variables présentes sur le layout
        // oldPassword, newPassword, confirm new password
        String oldPass = oldPassword.getText().toString().replace("'", "''");
        String newPass = newPassword.getText().toString().replace("'", "''");
        String newPassConfirm = newPasswordConfirm.getText().toString().replace("'", "''");

        CurrentUser.getInstance();
        if (!CurrentUser.isConnected()) {
            oldPassword.setError(getString(R.string.error_need_user_connection));
            cancel = true;
        }

        if (!Utility.isNotNull(oldPass)) {
            oldPassword.setError(getString(R.string.error_field_required));
            focusView = oldPassword;
            cancel = true;
        }

        if (!Utility.isNotNull(newPass)) {
            newPassword.setError(getString(R.string.error_field_required));
            focusView = newPassword;
            cancel = true;
        }

        if (!Utility.isNotNull(newPassConfirm)) {
            newPasswordConfirm.setError(getString(R.string.error_field_required));
            focusView = newPasswordConfirm;
            cancel = true;
        }

        if (!newPass.equals(newPassConfirm)) {
            newPasswordConfirm.setError("Le mot de passe de confirmation est incorrect");
            focusView = newPasswordConfirm;
            cancel = true;
        }

        if (!oldPass.isEmpty() && !newPass.isEmpty() && oldPass.equals(newPass)) {
            newPassword.setError("Le nouveau mot de passe est identique à l'ancien");
            focusView = newPassword;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
        }

        return !cancel;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getFragmentManager().popBackStackImmediate();
    }
}
