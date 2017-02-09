package com.orlanth23.annoncesNC.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class Categorie implements Parcelable{

    private Integer idCAT;
    private String nameCAT;
    private String couleurCAT;
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

    public Categorie(Integer idCAT, String nameCAT, String couleurCAT, int nbAnnonceCAT) {
        this.idCAT = idCAT;
        this.nameCAT = nameCAT;
        this.couleurCAT = couleurCAT;
        this.nbAnnonceCAT = nbAnnonceCAT;
    }

    protected Categorie(Parcel in) {
        idCAT = in.readInt();
        nameCAT = in.readString();
        couleurCAT = in.readString();
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

    public String getCouleurCAT() {
        return couleurCAT;
    }

    public void setCouleurCAT(String couleurCAT) {
        this.couleurCAT = couleurCAT;
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
        dest.writeString(couleurCAT);
        dest.writeInt(nbAnnonceCAT);
    }
}
