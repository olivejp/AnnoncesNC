package com.orlanth23.annoncesNC.dto;

/**
 * Created by orlanth23 on 11/07/2016.
 */

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {
    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {

        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    protected Integer idMessage;
    protected Utilisateur sender;
    protected Utilisateur receiver;
    protected String message;
    protected Long dateMessage;

    /* Constructeur à partir d'un Parcel*/
    public Message(Parcel in) {
        idMessage = in.readInt();
        sender = in.readParcelable(Utilisateur.class.getClassLoader());
        receiver = in.readParcelable(Utilisateur.class.getClassLoader());
        message = in.readString();
        dateMessage = in.readLong();
    }

    public Message(Integer idMessage, Utilisateur sender, Utilisateur receiver, String message, Long dateMessage) {
        super();
        this.idMessage = idMessage;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.dateMessage = dateMessage;
    }

    public Integer getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(Integer idMessage) {
        this.idMessage = idMessage;
    }

    public Utilisateur getSender() {
        return sender;
    }

    public void setSender(Utilisateur sender) {
        this.sender = sender;
    }

    public Utilisateur getReceiver() {
        return receiver;
    }

    public void setReceiver(Integer idreceiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getDateMessage() {
        return dateMessage;
    }

    public void setDateMessage(Long dateMessage) {
        this.dateMessage = dateMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.idMessage);
        dest.writeParcelable(this.sender, 0);
        dest.writeParcelable(this.receiver, 0);
        dest.writeString(this.message);
        dest.writeLong(this.dateMessage);
    }

}

