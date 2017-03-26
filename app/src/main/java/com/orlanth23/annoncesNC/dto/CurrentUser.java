package com.orlanth23.annoncesnc.dto;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.orlanth23.annoncesnc.database.DictionaryDAO;

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
    private static String token = null;

    private CurrentUser(Parcel in) {
        INSTANCE = in.readParcelable(CurrentUser.class.getClassLoader());
        connected = (boolean) in.readValue(Boolean.class.getClassLoader());
        token = in.readString();
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

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        CurrentUser.token = token;
    }

    public boolean isConnected() {
        return INSTANCE != null && connected;
    }

    public void setConnected(boolean connected) {
        if (INSTANCE != null) {
            CurrentUser.connected = connected;
        }
    }

    public void setUser(Utilisateur user) {
        INSTANCE.setIdUTI(user.getIdUTI());
        INSTANCE.setEmailUTI(user.getEmailUTI());
        INSTANCE.setTelephoneUTI(user.getTelephoneUTI());
        setConnected(true);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(INSTANCE, 0);
        dest.writeValue(connected);
        dest.writeString(token);
    }

    public CurrentUser getUserFromDictionary(Context context) {
        String idUser = DictionaryDAO.getValueByKey(context, DictionaryDAO.Dictionary.DB_CLEF_ID_USER);
        String email = DictionaryDAO.getValueByKey(context, DictionaryDAO.Dictionary.DB_CLEF_LOGIN);
        String telephone = DictionaryDAO.getValueByKey(context, DictionaryDAO.Dictionary.DB_CLEF_TELEPHONE);

        // Si les données d'identification ont été saisies
        INSTANCE.setConnected(true);
        INSTANCE.setIdUTI(idUser);
        INSTANCE.setEmailUTI(email);
        INSTANCE.setTelephoneUTI(telephone);
        return INSTANCE;

    }

    public void clear() {
        INSTANCE.setConnected(false);
        INSTANCE.setIdUTI("");
        INSTANCE.setTelephoneUTI("");
        INSTANCE.setEmailUTI("");
    }
}
