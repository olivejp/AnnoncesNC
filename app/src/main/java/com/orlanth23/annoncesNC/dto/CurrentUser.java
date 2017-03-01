package com.orlanth23.annoncesnc.dto;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;

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
    private Activity mActivity;
    private Runnable runnable;
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

    public boolean isConnected() {
        return INSTANCE != null && connected;
    }

    public void setConnected(boolean connected) {
        if (INSTANCE != null){
            CurrentUser.connected = connected;
        }
    }

    public void setUser(Utilisateur user){
        INSTANCE.setIdUTI(user.getIdUTI());
        INSTANCE.setEmailUTI(user.getEmailUTI());
        INSTANCE.setTelephoneUTI(user.getTelephoneUTI());
        setConnected(true);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(INSTANCE, 0);
        dest.writeValue(connected);
    }
}
