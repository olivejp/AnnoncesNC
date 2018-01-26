package com.orlanth23.annoncesnc.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

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
    private String UUIDPhoto;
    private String pathPhoto;
    private String idAnnoncePhoto;
    private String statutPhoto;

    public Photo() {
        super();
    }

    public Photo(@Nullable String UUIDPhoto, String p_namePhoto, String idAnnoncePhoto, String statutPhoto) {
        this.UUIDPhoto = UUIDPhoto;
        this.pathPhoto = p_namePhoto;
        this.idAnnoncePhoto = idAnnoncePhoto;
        this.statutPhoto = statutPhoto;
    }

    public Photo(Parcel in) {
        UUIDPhoto = in.readString();
        pathPhoto = in.readString();
        idAnnoncePhoto = in.readString();
        statutPhoto = in.readString();
    }

    public String getUUIDPhoto() {
        return UUIDPhoto;
    }

    public void setUUIDPhoto(String UUIDPhoto) {
        this.UUIDPhoto = UUIDPhoto;
    }

    public String getPathPhoto() {
        return pathPhoto;
    }

    public void setPathPhoto(String pathPhoto) {
        this.pathPhoto = pathPhoto;
    }

    public String getIdAnnoncePhoto() {
        return idAnnoncePhoto;
    }

    public void setIdAnnoncePhoto(String idAnnoncePhoto) {
        this.idAnnoncePhoto = idAnnoncePhoto;
    }

    public String getStatutPhoto() {
        return statutPhoto;
    }

    public void setStatutPhoto(String statutPhoto) {
        this.statutPhoto = statutPhoto;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.UUIDPhoto);
        dest.writeString(this.pathPhoto);
        dest.writeString(this.idAnnoncePhoto);
        dest.writeString(this.statutPhoto);
    }
}
