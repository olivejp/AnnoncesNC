package com.orlanth23.annoncesnc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.database.DictionaryDAO;
import com.orlanth23.annoncesnc.domain.CurrentUser;
import com.orlanth23.annoncesnc.service.UserService;
import com.orlanth23.annoncesnc.ui.interfaces.CustomChangePasswordCallback;
import com.orlanth23.annoncesnc.utility.PasswordEncryptionService;
import com.orlanth23.annoncesnc.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangePasswordFirebaseActivity extends CustomCompatActivity implements CustomChangePasswordCallback {

    public static final String TAG = ChangePasswordFirebaseActivity.class.getName();

    @BindView(R.id.newPassword)
    EditText newPassword;
    @BindView(R.id.newPasswordConfirm)
    EditText newPasswordConfirm;

    private String mPassword;

    private CustomCompatActivity mActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);
        updateActionBar(R.string.change_password, true);
    }

    @OnClick(R.id.btnChangePassword)
    public void changePassword() {
        if (checkChangePassword()) {
            prgDialog.setMessage("Mise à jour du mot de passe.");
            prgDialog.show();
            mPassword = newPassword.getText().toString().replace("'", "''");
            UserService.updatePassword(mAuth, this, mPassword, this);
        }
    }

    private boolean checkChangePassword() {
        mPassword = newPassword.getText().toString().replace("'", "''");
        String newPassConfirm = newPasswordConfirm.getText().toString().replace("'", "''");

        CurrentUser cu = CurrentUser.getInstance();

        if (!cu.isConnected() || (mAuth.getCurrentUser() == null)) {
            newPassword.setError(getString(R.string.error_need_user_connection));
            return false;
        }

        if (!Utility.isNotNull(mPassword)) {
            newPassword.setError(getString(R.string.error_field_required));
            newPassword.requestFocus();
            return false;
        }

        if (!Utility.isNotNull(newPassConfirm)) {
            newPasswordConfirm.setError(getString(R.string.error_field_required));
            newPasswordConfirm.requestFocus();
            return false;
        }

        if (!mPassword.equals(newPassConfirm)) {
            newPasswordConfirm.setError(getString(R.string.error_confirmationPassword_incorrect));
            newPasswordConfirm.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onCompleteChangePassword() {
        prgDialog.dismiss();

        // Si l'utilisateur dont on a modifié le mot de passe est celui qui est enregistré dans notre BD,
        // On met à jour le mot de passe dans la BD également.
        if (mAuth.getCurrentUser().getUid() == DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_ID_USER)) {
            DictionaryDAO.update(this, DictionaryDAO.Dictionary.DB_CLEF_MOT_PASSE, PasswordEncryptionService.desEncryptIt(mPassword));
        }

        Utility.hideKeyboard(this);
        setResult(RESULT_OK, new Intent());
        finish();
    }

    @Override
    public void onFailureChangePassword() {
        prgDialog.dismiss();
    }
}
