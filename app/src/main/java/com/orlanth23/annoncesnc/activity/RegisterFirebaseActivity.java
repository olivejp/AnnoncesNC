package com.orlanth23.annoncesnc.activity;

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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.dto.Utilisateur;
import com.orlanth23.annoncesnc.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterFirebaseActivity extends CustomCompatActivity {

    public static final int CODE_REGISTER_ACTIVITY = 100;
    public static final String EMAIL = "EMAIL";

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
    @BindView(R.id.registerDisplayName)
    EditText vDisplayName;
    @BindView(R.id.checkBox_register_remember_me)
    CheckBox vCheckBoxRegisterRememberMe;
    private AppCompatActivity mActivity = this;

    private String mIdUser;
    private String mEmail;
    private String mDisplayName;
    private String mTelephone;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        mEmail = bundle.getString(EMAIL);
        vEmail.setText(mEmail);

        // Récupération de l'actionBar et changement du titre
        ActionBar tb = getSupportActionBar();
        if (tb != null) {
            tb.setTitle(R.string.action_sign_up);
            tb.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setDefaultValues() {
        vEmail.setText("");
        vDisplayName.setText("");
        vPassword.setText("");
        vPasswordConfirm.setText("");
        vTelephone.setText("");
    }

    private boolean checkRegister(EditText emailET, EditText displayNameET, EditText passwordET, EditText passwordConfirmET, EditText telephoneET) {

        // RAZ des données
        mDisplayName = "";
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
        mDisplayName = displayNameET.getText().toString().replace("'", "''");
        mPassword = passwordET.getText().toString().replace("'", "''");
        String passwordConfirmString = passwordConfirmET.getText().toString().replace("'", "''");

        // When Email Edit View and Password Edit View have values other than Null
        if (!Utility.isNotNull(mEmail)) {
            emailET.setError(getString(R.string.error_field_required));
            focusView = emailET;
            cancel = true;
        }

        if (!Utility.isNotNull(mDisplayName)) {
            displayNameET.setError(getString(R.string.error_field_required));
            focusView = displayNameET;
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
        if (checkRegister(vEmail, vDisplayName, vPassword, vPasswordConfirm, vTelephone)) {
            Utility.hideKeyboard(mActivity);

            prgDialog.setMessage("Création du profil : " + mEmail);
            prgDialog.show();

            // Création de l'utilisateur sur Firebase Auth
            mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Mise à jour du nom d'affichage
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(mDisplayName)
                                .build();

                            mFirebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User profile updated.");
                                    }else{
                                        Log.d(TAG, "Erreur lors de la mise à jour du nom d'affichage.");
                                    }
                                }
                            });

                            createFirebaseDatabaseUser();
                        }

                        if (!task.isSuccessful()) {
                            // Display failed message using Toast
                            prgDialog.hide();
                            vErrorMsg.setText(task.getException().getMessage());
                        }
                    }
                });
        }
    }

    private void createFirebaseDatabaseUser() {
        prgDialog.setMessage("Enregistrement dans la base de données.");

        // Récupération du numéro Id de l'utilisateur renvoyé par le WS
        mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            return;
        }
        // Récupération de l'id de l'utilisateur
        mIdUser = mFirebaseUser.getUid();

        // Récupération de l'utilisateur
        Utilisateur user = new Utilisateur();
        user.setIdUTI(mIdUser);
        user.setEmailUTI(mEmail);
        user.setDisplayNameUTI(mDisplayName);
        user.setTelephoneUTI(mTelephone);

        // Enregistrement de cet utilisateur dans la RealTimeDatabase de Firebase
        DatabaseReference userRef = mDatabase.getReference("users/" + mIdUser);

        userRef.setValue(user).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                // On enlève la barre de progression
                prgDialog.hide();

                if (task.isSuccessful()) {
                    // Si on a coché la case pour se souvenir de l'utilisateur
                    if (vCheckBoxRegisterRememberMe.isChecked()) {
                        Utility.saveAutoComplete(mActivity, mIdUser, mEmail, mDisplayName, mTelephone, mPassword);
                    }

                    // Récupération des infos dans notre CurrentUser
                    CurrentUser cu = CurrentUser.getInstance();
                    cu.setConnected(true);
                    cu.setIdUTI(mIdUser);
                    cu.setEmailUTI(mEmail);
                    cu.setDisplayNameUTI(mDisplayName);
                    cu.setTelephoneUTI(mTelephone);

                    Toast.makeText(mActivity, getString(R.string.dialog_register_ok), Toast.LENGTH_LONG).show();

                    // On retourne un résultat dans un intent
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);
                    finish();
                } else {
                    // Suppression de l'utilisateur dans FirebaseAuth
                    mFirebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User account : " + mEmail + " deleted.");
                            } else {
                                Log.d(TAG, "ERROR - User account : " + mEmail + " has not been deleted.");
                            }
                        }
                    });

                    // Echec de l'insertion dans RealTimeDatabase
                    vErrorMsg.setText(task.getException().getMessage());

                    // RAZ du CurrentUser
                    CurrentUser.getInstance().clear();
                }
            }
        });
    }
}
