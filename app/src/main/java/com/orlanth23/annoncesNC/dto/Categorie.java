package com.orlanth23.annoncesNC.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by orlanth23 on 17/08/2015.
 */
public class Categorie implements Parcelable{

    private Integer idCAT;
    private String nameCAT;
    private String imageCAT;
    private int nbAnnonceCAT;

    public static final Creator<Categorie> CREATOR = new Creator<Categorie>() {
        @Override
        public Categorie createFromParcel(Parcel in) {
            return new Categorie(in);
        }

        @Override
        public Categorie[] newArray(int size) {
            return new Categorie[size];
        }
    };

    public Categorie(Integer idCAT, String nameCAT, String imageCAT, int nbAnnonceCAT) {
        this.idCAT = idCAT;
        this.nameCAT = nameCAT;
        this.imageCAT = imageCAT;
        this.nbAnnonceCAT = nbAnnonceCAT;
    }

    protected Categorie(Parcel in) {
        idCAT = in.readInt();
        nameCAT = in.readString();
        imageCAT = in.readString();
        nbAnnonceCAT = in.readInt();
    }

    public Integer getIdCAT() {
        return idCAT;
    }

    public void setIdCAT(Integer idCAT) {
        this.idCAT = idCAT;
    }

    public String getNameCAT() {
        return nameCAT;
    }

    public void setNameCAT(String nameCAT) {
        this.nameCAT = nameCAT;
    }

    public String getImageCAT() {
        return imageCAT;
    }

    public void setImageCAT(String imageCAT) {
        this.imageCAT = imageCAT;
    }

    public int getNbAnnonceCAT() {
        return nbAnnonceCAT;
    }

    public void setNbAnnonceCAT(int nbAnnonceCAT) {
        this.nbAnnonceCAT = nbAnnonceCAT;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idCAT);
        dest.writeString(nameCAT);
        dest.writeString(imageCAT);
        dest.writeInt(nbAnnonceCAT);
    }
}
