package com.orlanth23.annoncesnc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.utility.PasswordEncryptionService;
import com.orlanth23.annoncesnc.utility.Utility;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByFragmentManager;

public class ChangePasswordActivity extends CustomRetrofitCompatActivity {

    public static final String TAG = ChangePasswordActivity.class.getName();

    @BindView(R.id.oldPassword)
    EditText oldPassword;
    @BindView(R.id.newPassword)
    EditText newPassword;
    @BindView(R.id.newPasswordConfirm)
    EditText newPasswordConfirm;

    private CustomRetrofitCompatActivity mActivity = this;

    private retrofit.Callback<ReturnWS> changePasswordCallback = new retrofit.Callback<ReturnWS>() {
        @Override
        public void success(ReturnWS rc, Response response) {
            prgDialog.hide();
            if (rc.statusValid()) {
                Toast.makeText(mActivity, getString(R.string.dialog_register_ok), Toast.LENGTH_LONG).show();
                Utility.hideKeyboard(mActivity);
                setResult(RESULT_OK, new Intent());
                finish();
            } else {
                Toast.makeText(mActivity, rc.getMsg(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            prgDialog.hide();
            SendDialogByFragmentManager(getFragmentManager(), getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, TAG);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);
        changeActionBarTitle(R.string.change_password, true);
    }

    @OnClick(R.id.btnChangePassword)
    public void changePassword() {
        if (checkChangePassword()) {
            String oldPass = oldPassword.getText().toString().replace("'", "''");
            String newPass = newPassword.getText().toString().replace("'", "''");
            String oldPasswordEncrypted = PasswordEncryptionService.desEncryptIt(oldPass);
            String newPasswordEncrypted = PasswordEncryptionService.desEncryptIt(newPass);

            prgDialog.show();
            retrofitService.changePassword(CurrentUser.getInstance().getIdUTI(), oldPasswordEncrypted, newPasswordEncrypted, changePasswordCallback);
        }
    }

    private boolean checkChangePassword() {
        String oldPass = oldPassword.getText().toString().replace("'", "''");
        String newPass = newPassword.getText().toString().replace("'", "''");
        String newPassConfirm = newPasswordConfirm.getText().toString().replace("'", "''");

        CurrentUser.getInstance();

        if (!CurrentUser.isConnected()) {
            oldPassword.setError(getString(R.string.error_need_user_connection));
            return false;
        }

        if (!Utility.isNotNull(oldPass)) {
            oldPassword.setError(getString(R.string.error_field_required));
            oldPassword.requestFocus();
            return false;
        }

        if (!Utility.isNotNull(newPass)) {
            newPassword.setError(getString(R.string.error_field_required));
            newPassword.requestFocus();
            return false;
        }

        if (!Utility.isNotNull(newPassConfirm)) {
            newPasswordConfirm.setError(getString(R.string.error_field_required));
            newPasswordConfirm.requestFocus();
            return false;
        }

        if (!newPass.equals(newPassConfirm)) {
            newPasswordConfirm.setError(getString(R.string.error_confirmationPassword_incorrect));
            newPasswordConfirm.requestFocus();
            return false;
        }

        if (!oldPass.isEmpty() && !newPass.isEmpty() && oldPass.equals(newPass)) {
            newPassword.setError(getString(R.string.error_same_oldPassword_newPassword));
            newPassword.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getFragmentManager().popBackStackImmediate();
    }
}
