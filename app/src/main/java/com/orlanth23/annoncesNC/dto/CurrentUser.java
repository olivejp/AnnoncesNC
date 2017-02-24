package com.orlanth23.annoncesnc.dto;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.database.DictionaryDAO;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.RetrofitService;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByActivity;

public class CurrentUser extends Utilisateur implements Parcelable {

    public static final Parcelable.Creator<CurrentUser> CREATOR = new Parcelable.Creator<CurrentUser>() {

        @Override
        public CurrentUser createFromParcel(Parcel source) {
            return new CurrentUser(source);
        }

        @Override
        public CurrentUser[] newArray(int size) {
            return new CurrentUser[size];
        }
    };
    private static CurrentUser INSTANCE = null;
    private static boolean connected = false;
    private static Activity activity;
    private static Runnable runnable;
    private static Callback<ReturnWS> callbackLogin = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS rs = response.body();
                if (rs.statusValid()) {
                    Gson gson = new Gson();

                    Utilisateur user = gson.fromJson(rs.getMsg(), Utilisateur.class);
                    CurrentUser.getInstance().setIdUTI(user.getIdUTI());
                    CurrentUser.getInstance().setEmailUTI(user.getEmailUTI());
                    CurrentUser.getInstance().setTelephoneUTI(user.getTelephoneUTI());
                    CurrentUser.setConnected(true);

                    Thread myThread = new Thread(runnable);
                    myThread.start();

                    // Display successfully registered message using Toast
                    Toast.makeText(activity, activity.getString(R.string.connected_with) + CurrentUser.getInstance().getEmailUTI() + " !", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, rs.getMsg(), Toast.LENGTH_LONG).show();
                }
            }
        }
        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            SendDialogByActivity(activity, activity.getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, null);
        }
    };

    private CurrentUser(Parcel in) {
        INSTANCE = in.readParcelable(CurrentUser.class.getClassLoader());
        connected = (boolean) in.readValue(Boolean.class.getClassLoader());
    }

    private CurrentUser() {
        super();
        connected = false;
    }

    public static synchronized CurrentUser getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CurrentUser();
        }
        return INSTANCE;
    }

    public static boolean isConnected() {
        return INSTANCE != null && connected;
    }

    public static void setConnected(boolean connected) {
        if (INSTANCE != null){
            CurrentUser.connected = connected;
        }
    }

    public static void setUser(Utilisateur user){
        INSTANCE.setIdUTI(user.getIdUTI());
        INSTANCE.setEmailUTI(user.getEmailUTI());
        INSTANCE.setTelephoneUTI(user.getTelephoneUTI());
        setConnected(true);
    }

    public static void retrieveConnection(final Activity p_activity, final Runnable p_runnable) {

        runnable = p_runnable;
        activity = p_activity;

        // Récupération de l'utilisateur par défaut
        // Création d'un RestAdapter pour le futur appel de mon RestService
        String connexion_auto = DictionaryDAO.getValueByKey(activity, DictionaryDAO.Dictionary.DB_CLEF_AUTO_CONNECT);
        if (connexion_auto != null && connexion_auto.equals("O")) {
            if (!CurrentUser.isConnected()) {
                String email = DictionaryDAO.getValueByKey(activity, DictionaryDAO.Dictionary.DB_CLEF_LOGIN);
                String password = DictionaryDAO.getValueByKey(activity, DictionaryDAO.Dictionary.DB_CLEF_MOT_PASSE);

                // Si les données d'identification ont été saisies
                if (email != null && password != null) {
                    RetrofitService retrofitService = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitService.class);
                    Call<ReturnWS> callLogin = retrofitService.login(email, password);
                    callLogin.enqueue(callbackLogin);
                }
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(INSTANCE, 0);
        dest.writeValue(connected);
    }
}
