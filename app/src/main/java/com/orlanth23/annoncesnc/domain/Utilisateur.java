package com.orlanth23.annoncesnc.domain;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

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

    private String idUTI;
    private String emailUTI;
    private String telephoneUTI;
    private String displayNameUTI;
    private Uri photoUrlUTI;

    public Utilisateur() {
        this.idUTI = "";
        this.emailUTI = "";
        this.telephoneUTI = "";
        this.displayNameUTI = "";
        this.photoUrlUTI = Uri.parse("");
    }

    public Utilisateur(Parcel in) {
        idUTI = in.readString();
        emailUTI = in.readString();
        telephoneUTI = in.readString();
        displayNameUTI = in.readString();
        photoUrlUTI = Uri.parse(in.readString());
    }

    public String getIdUTI() {
        return idUTI;
    }

    public void setIdUTI(String idUTI) {
        this.idUTI = idUTI;
    }

    public String getEmailUTI() {
        return emailUTI;
    }

    public void setEmailUTI(String emailUTI) {
        this.emailUTI = emailUTI;
    }

    public String getTelephoneUTI() {
        return telephoneUTI;
    }

    public void setTelephoneUTI(String telephoneUTI) {
        this.telephoneUTI = telephoneUTI;
    }

    public String getDisplayNameUTI() {
        return displayNameUTI;
    }

    public void setDisplayNameUTI(String displayNameUTI) {
        this.displayNameUTI = displayNameUTI;
    }

    public Uri getPhotoUrlUTI() {
        return photoUrlUTI;
    }

    public void setPhotoUrlUTI(Uri photoUrlUTI) {
        this.photoUrlUTI = photoUrlUTI;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idUTI);
        dest.writeString(emailUTI);
        dest.writeString(telephoneUTI);
        dest.writeString(displayNameUTI);
        dest.writeString(String.valueOf(photoUrlUTI));
    }

}
