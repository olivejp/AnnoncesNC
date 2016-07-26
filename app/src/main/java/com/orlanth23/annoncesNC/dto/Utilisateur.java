package com.orlanth23.annoncesNC.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by orlanth23 on 17/08/2015.
 */
public class Utilisateur implements Parcelable {

    public static final Parcelable.Creator<Utilisateur> CREATOR = new Parcelable.Creator<Utilisateur>() {

        @Override
        public Utilisateur createFromParcel(Parcel source) {
            return new Utilisateur(source);
        }

        @Override
        public Utilisateur[] newArray(int size) {
            return new Utilisateur[size];
        }
    };

    private Integer idUTI;
    private String emailUTI;
    private Integer telephoneUTI;

    public Utilisateur(Integer idUTI, String emailUTI, Integer telephoneUTI) {
        this.idUTI = idUTI;
        this.emailUTI = emailUTI;
        this.telephoneUTI = telephoneUTI;
    }

    public Utilisateur() {
        this.idUTI = 0;
        this.emailUTI = "";
        this.telephoneUTI = 0;
    }

    public Utilisateur(Parcel in) {
        idUTI = in.readInt();
        emailUTI = in.readString();
        telephoneUTI = in.readInt();
    }

    public Integer getIdUTI() {
        return idUTI;
    }

    public void setIdUTI(Integer idUTI) {
        this.idUTI = idUTI;
    }

    public String getEmailUTI() {
        return emailUTI;
    }

    public void setEmailUTI(String emailUTI) {
        this.emailUTI = emailUTI;
    }

    public Integer getTelephoneUTI() {
        return telephoneUTI;
    }

    public void setTelephoneUTI(Integer telephoneUTI) {
        this.telephoneUTI = telephoneUTI;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idUTI);
        dest.writeString(emailUTI);
        dest.writeInt(telephoneUTI);
    }

}
