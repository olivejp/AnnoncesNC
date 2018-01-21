package com.orlanth23.annoncesnc.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LostFirebasePasswordActivity extends CustomCompatActivity {

    private static final String tag = LostFirebasePasswordActivity.class.getName();

    @BindView(R.id.email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.login_error)
    TextView errorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_password);
        ButterKnife.bind(this);
        updateActionBar(R.string.action_lost_password, true);
    }

    @OnClick(R.id.lostPassword)
    public void lostPassword(View view) {

        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().replace("'", "''");

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (!Utility.isNotNull(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!Utility.validateEmail(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Si aucune erreur bloquante on continue et on appelle webservice pour savoir si l'utilisateur est connu
        if (cancel) {
            focusView.requestFocus();
        } else {
            prgDialog.show();
        }
    }
}

