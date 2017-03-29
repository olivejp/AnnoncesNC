package com.orlanth23.annoncesnc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.database.DictionaryDAO;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.utility.Utility;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.ReturnWS;
import com.orlanth23.annoncesnc.webservice.ServiceUtilisateur;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByFragmentManager;


public class LostFirebasePasswordActivity extends CustomCompatActivity {

    private static final String tag = LostFirebasePasswordActivity.class.getName();

    @BindView(R.id.email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.login_error)
    TextView errorMsg;

    private ServiceUtilisateur serviceUtilisateur = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(ServiceUtilisateur.class);

    // Création d'un RestAdapter pour le futur appel de mon RestService
    private Callback<ReturnWS> callbackLostPassword = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS rs = response.body();
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
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            prgDialog.hide();
            SendDialogByFragmentManager(getFragmentManager(), getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, tag);
        }
    };

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
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    prgDialog.hide();
                    if (task.isSuccessful()) {
                        // Display successfully registered message using Toast
                        Toast.makeText(getApplicationContext(), getString(R.string.dialog_password_send), Toast.LENGTH_LONG).show();

                        // Si l'authentification a fonctionné, je peux quitter l'activité
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.dialog_failed_webservice), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}

