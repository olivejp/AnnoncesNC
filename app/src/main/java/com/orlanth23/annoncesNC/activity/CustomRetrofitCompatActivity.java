package com.orlanth23.annoncesnc.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.orlanth23.annoncesnc.R;

public class CustomRetrofitCompatActivity extends AppCompatActivity {

    protected ProgressDialog prgDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage(getString(R.string.dialog_msg_patience));
        prgDialog.setCancelable(true);
    }

    public void changeActionBarTitle(int resIdTitle, boolean setHomeEnabled) {
        ActionBar tb = getSupportActionBar();
        if (tb != null) {
            tb.setTitle(resIdTitle);
            tb.setDisplayHomeAsUpEnabled(setHomeEnabled);
        }
    }
}
