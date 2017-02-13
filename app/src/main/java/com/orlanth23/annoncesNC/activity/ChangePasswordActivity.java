package com.orlanth23.annoncesNC.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesNC.dto.CurrentUser;
import com.orlanth23.annoncesNC.utility.PasswordEncryptionService;
import com.orlanth23.annoncesNC.utility.Utility;
import com.orlanth23.annoncesNC.webservice.ReturnWS;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.orlanth23.annoncesNC.utility.Utility.SendDialogByFragmentManager;

public class ChangePasswordActivity extends CustomRetrofitCompatActivity {

    public static final String tag = ChangePasswordActivity.class.getName();

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
            SendDialogByFragmentManager(getFragmentManager(), getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, tag);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);
        changeActionBarTitle(R.string.change_password, true);
    }

    public void changePassword(View view) {
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
        if (Utility.isTextViewOnError(!CurrentUser.isConnected(), oldPassword, getString(R.string.error_need_user_connection), false)){
            return false;
        }
        if (Utility.isTextViewOnError(!Utility.isNotNull(oldPass), oldPassword, getString(R.string.error_field_required), true)){
            return false;
        }
        if (Utility.isTextViewOnError(!Utility.isNotNull(newPass), newPassword, getString(R.string.error_field_required), true)){
            return false;
        }
        if (Utility.isTextViewOnError(!Utility.isNotNull(newPassConfirm), newPasswordConfirm, getString(R.string.error_field_required), true)){
            return false;
        }
        if (Utility.isTextViewOnError(!newPass.equals(newPassConfirm), newPasswordConfirm, getString(R.string.error_confirmationPassword_incorrect), true)){
            return false;
        }
        return !Utility.isTextViewOnError(!oldPass.isEmpty() && !newPass.isEmpty() && oldPass.equals(newPass), newPassword, getString(R.string.error_same_oldPassword_newPassword), true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getFragmentManager().popBackStackImmediate();
    }
}
