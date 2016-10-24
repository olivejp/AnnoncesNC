package com.orlanth23.annoncesNC.dto;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.database.DictionaryDAO;
import com.orlanth23.annoncesNC.dialogs.NoticeDialogFragment;
import com.orlanth23.annoncesNC.webservices.AccessPoint;
import com.orlanth23.annoncesNC.webservices.RetrofitService;
import com.orlanth23.annoncesNC.webservices.ReturnWS;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.orlanth23.annoncesNC.utility.Utility.SendDialogByActivity;

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

    public static void retrieveConnection(final Activity activity, final Runnable runnable) {
        // Récupération de l'utilisateur par défaut
        // Création d'un RestAdapter pour le futur appel de mon RestService
        String connexion_auto = DictionaryDAO.getValueByKey(activity, DictionaryDAO.Dictionary.DB_CLEF_AUTO_CONNECT);
        if (connexion_auto != null && connexion_auto.equals("O")) {
            if (!CurrentUser.isConnected()) {
                String email = DictionaryDAO.getValueByKey(activity, DictionaryDAO.Dictionary.DB_CLEF_LOGIN);
                String password = DictionaryDAO.getValueByKey(activity, DictionaryDAO.Dictionary.DB_CLEF_PASSWORD);

                // Si les données d'identification ont été saisies
                if (email != null && password != null) {
                    RetrofitService retrofitService = new RestAdapter.Builder().setEndpoint(AccessPoint.getInstance().getServerEndpoint()).build().create(RetrofitService.class);
                    retrofitService.login(email, password, new Callback<ReturnWS>() {
                                @Override
                                public void success(ReturnWS retour, Response response) {
                                    if (retour.statusValid()) {
                                        Gson gson = new Gson();

                                        Utilisateur user = gson.fromJson(retour.getMsg(), Utilisateur.class);
                                        CurrentUser.getInstance().setIdUTI(user.getIdUTI());
                                        CurrentUser.getInstance().setEmailUTI(user.getEmailUTI());
                                        CurrentUser.getInstance().setTelephoneUTI(user.getTelephoneUTI());
                                        CurrentUser.setConnected(true);

                                        runnable.run();

                                        // Display successfully registered message using Toast
                                        Toast.makeText(activity, activity.getString(R.string.connected_with) + CurrentUser.getInstance().getEmailUTI() + " !", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(activity, retour.getMsg(), Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    SendDialogByActivity(activity, activity.getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, null);
                                }
                            }
                    );
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
