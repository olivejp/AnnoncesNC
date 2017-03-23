package com.orlanth23.annoncesnc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangePasswordFirebaseActivity extends CustomRetrofitCompatActivity {

    public static final String TAG = ChangePasswordFirebaseActivity.class.getName();

    @BindView(R.id.newPassword)
    EditText newPassword;
    @BindView(R.id.newPasswordConfirm)
    EditText newPasswordConfirm;

    private FirebaseUser mUserFirebase;
    private FirebaseAuth mAuth;

    private CustomRetrofitCompatActivity mActivity = this;

    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                CurrentUser.getInstance().setConnected(true);
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out");
                CurrentUser.getInstance().setConnected(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);
        changeActionBarTitle(R.string.change_password, true);
        mAuth = FirebaseAuth.getInstance();
        mUserFirebase = FirebaseAuth.getInstance().getCurrentUser();
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

    @OnClick(R.id.btnChangePassword)
    public void changePassword() {
        if (checkChangePassword()) {

            prgDialog.show();

            String password = newPassword.getText().toString().replace("'", "''");

            mUserFirebase.updatePassword(password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        prgDialog.hide();
                        if (task.isSuccessful()) {
                            Toast.makeText(mActivity, getString(R.string.dialog_register_ok), Toast.LENGTH_LONG).show();
                            Utility.hideKeyboard(mActivity);
                            setResult(RESULT_OK, new Intent());
                            finish();
                        }else{
                            Toast.makeText(mActivity, "Echec de la mise à jour du mot de passe.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        }
    }

    private boolean checkChangePassword() {
        String newPass = newPassword.getText().toString().replace("'", "''");
        String newPassConfirm = newPasswordConfirm.getText().toString().replace("'", "''");

        CurrentUser cu = CurrentUser.getInstance();

        if (!cu.isConnected()) {
            newPassword.setError(getString(R.string.error_need_user_connection));
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

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getFragmentManager().popBackStackImmediate();
    }
}
