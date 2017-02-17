package com.orlanth23.annoncesnc.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.database.DictionaryDAO;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.utility.Utility;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.RetrofitService;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByFragmentManager;


public class LostPasswordActivity extends CustomRetrofitCompatActivity implements NoticeDialogFragment.NoticeDialogListener {

    private static final String tag = LostPasswordActivity.class.getName();

    @BindView(R.id.email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.login_error)
    TextView errorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_password);
        ButterKnife.bind(this);
        changeActionBarTitle(R.string.action_lost_password, true);
        populateAutoComplete();
    }

    private void populateAutoComplete() {
        mEmailView.setText(DictionaryDAO.getValueByKey(getApplicationContext(), DictionaryDAO.Dictionary.DB_CLEF_LOGIN));
    }


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

            // Création d'un RestAdapter pour le futur appel de mon RestService
            RetrofitService retrofitService = new RestAdapter.Builder().setEndpoint(Proprietes.getServerEndpoint()).build().create(RetrofitService.class);
            Callback<ReturnWS> myCallback = new Callback<ReturnWS>() {
                @Override
                public void success(ReturnWS rs, Response response) {
                    prgDialog.hide();
                    if (rs.statusValid()) {

                        // Display successfully registered message using Toast
                        Toast.makeText(getApplicationContext(), getString(R.string.dialog_password_send), Toast.LENGTH_LONG).show();

                        // Si l'authentification a fonctionné, je peux quitter l'activité
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else {
                        errorMsg.setText(rs.getMsg());
                        Toast.makeText(getApplicationContext(), rs.getMsg(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    prgDialog.hide();
                    SendDialogByFragmentManager(getFragmentManager(), getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, tag);
                }
            };
            prgDialog.show();
            retrofitService.doLostPassword(email, myCallback);
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

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}

