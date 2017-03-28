package com.orlanth23.annoncesnc.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dto.CurrentUser;

public class CustomCompatActivity extends AppCompatActivity {

    protected ProgressDialog prgDialog;
    protected FirebaseAuth mAuth;
    protected FirebaseAuth.AuthStateListener mAuthStateListener;
    protected FirebaseUser mFirebaseUser;
    protected FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage(getString(R.string.dialog_msg_patience));
        prgDialog.setCancelable(true);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                CurrentUser cu = CurrentUser.getInstance();
                if (fUser != null) {
                    cu.setConnected(true);
                    cu.setEmailUTI(fUser.getEmail());
                    cu.setIdUTI(fUser.getUid());
                } else {
                    cu.clear();
                }
            }
        };
    }

    public void changeActionBarTitle(int resIdTitle, boolean setHomeEnabled) {
        ActionBar tb = getSupportActionBar();
        if (tb != null) {
            tb.setTitle(resIdTitle);
            tb.setDisplayHomeAsUpEnabled(setHomeEnabled);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
        mFirebaseUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
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

    public ProgressDialog getPrgDialog() {
        return prgDialog;
    }

    public void setPrgDialog(ProgressDialog prgDialog) {
        this.prgDialog = prgDialog;
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public void setmAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public FirebaseAuth.AuthStateListener getmAuthStateListener() {
        return mAuthStateListener;
    }

    public void setmAuthStateListener(FirebaseAuth.AuthStateListener mAuthStateListener) {
        this.mAuthStateListener = mAuthStateListener;
    }

    public FirebaseUser getmFirebaseUser() {
        return mFirebaseUser;
    }

    public void setmFirebaseUser(FirebaseUser mFirebaseUser) {
        this.mFirebaseUser = mFirebaseUser;
    }
}
