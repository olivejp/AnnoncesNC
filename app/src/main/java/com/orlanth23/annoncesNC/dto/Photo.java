package com.orlanth23.annoncesnc.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class Photo implements Parcelable {
    public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {

        @Override
        public Photo createFromParcel(Parcel source) {
            return new Photo(source);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
    private Integer idPhoto;
    private String namePhoto;
    private Integer idAnnoncePhoto;

    public Photo() {
        super();
    }

    public Photo(Integer p_idPhoto, String p_namePhoto, Integer idAnnonce) {
        this.idPhoto = p_idPhoto;
        this.namePhoto = p_namePhoto;
        this.idAnnoncePhoto = idAnnonce;
    }

    public Photo(Parcel in) {
        idPhoto = in.readInt();
        namePhoto = in.readString();
        idAnnoncePhoto = in.readInt();
    }

    public Integer getIdPhoto() {
        return idPhoto;
    }

    public void setIdPhoto(Integer idPhoto) {
        this.idPhoto = idPhoto;
    }

    public String getNamePhoto() {
        return namePhoto;
    }

    public void setNamePhoto(String namePhoto) {
        this.namePhoto = namePhoto;
    }

    public Integer getIdAnnoncePhoto() {
        return idAnnoncePhoto;
    }

    public void setIdAnnoncePhoto(Integer idAnnoncePhoto) {
        this.idAnnoncePhoto = idAnnoncePhoto;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.idPhoto);
        dest.writeString(this.namePhoto);
        dest.writeInt(this.idAnnoncePhoto);
    }
}