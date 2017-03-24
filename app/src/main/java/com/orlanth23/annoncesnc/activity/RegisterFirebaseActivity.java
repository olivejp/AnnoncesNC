package com.orlanth23.annoncesnc.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dto.Utilisateur;
import com.orlanth23.annoncesnc.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterFirebaseActivity extends CustomRetrofitCompatActivity {

    public static final int CODE_REGISTER_ACTIVITY = 100;
    private static final String TAG = RegisterFirebaseActivity.class.getName();
    @BindView(R.id.register_error)
    TextView vErrorMsg;
    @BindView(R.id.registerTelephone)
    EditText vTelephone;
    @BindView(R.id.registerEmail)
    EditText vEmail;
    @BindView(R.id.registerPassword)
    EditText vPassword;
    @BindView(R.id.registerPasswordConfirm)
    EditText vPasswordConfirm;
    @BindView(R.id.checkBox_register_remember_me)
    CheckBox vCheckBoxRegisterRememberMe;
    private ProgressDialog prgDialog;
    private AppCompatActivity mActivity = this;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private String mIdUser;
    private String mEmail;
    private String mTelephone;
    private String mPassword;

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

        // Get instance from FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
    }

    public void setDefaultValues() {
        vEmail.setText("");
        vPassword.setText("");
        vPasswordConfirm.setText("");
        vTelephone.setText("");
    }

    private boolean checkRegister(EditText emailET, EditText passwordET, EditText passwordConfirmET, EditText telephoneET) {

        // RAZ des données
        mTelephone = "";
        mEmail = "";
        mPassword = "";
        View focusView = null;
        boolean cancel = false;

        // Récupération des variables présentes sur le layout
        // Téléphone, email et password
        String tel = telephoneET.getText().toString();
        if (!tel.equals("")) {
            mTelephone = tel;
        }

        mEmail = emailET.getText().toString().replace("'", "''");
        mPassword = passwordET.getText().toString().replace("'", "''");
        String passwordConfirmString = passwordConfirmET.getText().toString().replace("'", "''");

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

        if (!Utility.isNotNull(mPassword)) {
            passwordET.setError(getString(R.string.error_field_required));
            focusView = passwordET;
            cancel = true;
        }

        if (!Utility.isNotNull(passwordConfirmString)) {
            passwordConfirmET.setError(getString(R.string.error_field_required));
            focusView = passwordConfirmET;
            cancel = true;
        }

        // When Email entered is invalid
        if (!Utility.validateEmail(mEmail)) {
            emailET.setError(getString(R.string.error_invalid_email));
            focusView = emailET;
            cancel = true;
        }


        if (!mPassword.equals(passwordConfirmString)) {
            passwordConfirmET.setError(getString(R.string.error_incorrect_password));
            focusView = passwordConfirmET;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        }

        return !cancel;
    }

    public void register(View view) {
        if (checkRegister(vEmail, vPassword, vPasswordConfirm, vTelephone)) {
            // On cache le clavier
            Utility.hideKeyboard(mActivity);

            // On affiche la barre de progression
            prgDialog.show();

            // Création de l'utilisateur sur Firebase Auth
            mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            // Display failed message using Toast
                            Toast.makeText(mActivity, getString(R.string.dialog_failed_webservice), Toast.LENGTH_LONG).show();
                            prgDialog.hide();
                        }

                        if (task.isSuccessful()) {
                            // Récupération du numéro Id de l'utilisateur renvoyé par le WS
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Récupération de l'id de l'utilisateur
                                mIdUser = firebaseUser.getUid();

                                // On remet les zones à blank
                                setDefaultValues();

                                // Création d'un utilisateur dans notre Firebase Database
                                createFirebaseDatabaseUser();
                            }else{
                                prgDialog.hide();
                            }
                        }else{
                            prgDialog.hide();
                        }
                    }
                });
        }
    }

    private void createFirebaseDatabaseUser(){

        // Récupération de l'utilisateur
        Utilisateur user = new Utilisateur();
        user.setIdUTI(mIdUser);
        user.setEmailUTI(mEmail);
        user.setTelephoneUTI(Integer.valueOf(mTelephone));

        // Enregistrement de cet utilisateur dans la RealTimeDatabase de Firebase
        DatabaseReference userRef = mDatabase.getReference("users/" + mIdUser);

        userRef.setValue(user).addOnCompleteListener(mActivity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                // On enlève la barre de progression
                prgDialog.hide();

                if (task.isSuccessful()) {
                    // Si on a coché la case pour se souvenir de l'utilisateur
                    if (vCheckBoxRegisterRememberMe.isChecked()) {
                        Utility.saveAutoComplete(mActivity, mIdUser, mEmail, mTelephone, mPassword);
                    }

                    Toast.makeText(mActivity, getString(R.string.dialog_register_ok), Toast.LENGTH_LONG).show();

                    // On retourne un résultat dans un intent
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);                             // On retourne un résultat RESULT_OK
                    finish();
                } else {
                    // On a pas réussi à insérer dans RealTimeDatabase
                    Toast.makeText(mActivity, getString(R.string.dialog_failed_webservice), Toast.LENGTH_LONG).show();

                    // On retourne un résultat dans un intent
                    Intent returnIntent = new Intent();
                    setResult(RESULT_CANCELED, returnIntent);                       // On retourne un résultat RESULT_CANCELED
                    finish();
                }
            }
        });
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
}
