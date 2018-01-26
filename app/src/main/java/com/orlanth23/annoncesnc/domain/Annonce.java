package com.orlanth23.annoncesnc.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Annonce implements Parcelable {

    public static final Parcelable.Creator<Annonce> CREATOR = new Parcelable.Creator<Annonce>() {

        @Override
        public Annonce createFromParcel(Parcel source) {
            return new Annonce(source);
        }

        @Override
        public Annonce[] newArray(int size) {
            return new Annonce[size];
        }
    };

    private String UUIDANO;                // Identifiant
    private Integer idCategorieANO;      // Une annonce appartient à une catégorie
    private Utilisateur utilisateurANO;  // Une annonce est rédigée par une personne
    private Integer priceANO;            // L'annonce a un prix
    private String descriptionANO;       // Description de l'annonce
    private String titreANO;             // Titre de l'annonce
    private boolean publishedANO;        // True si l'annonce est publiée sinon False
    private Long datePublished;          // La date de la parution
    private String statutANO;            // Statut de l'annonce
    private ArrayList<Photo> photos;     // Les photos de l'annonce


    /* Constructeur à partir d'un Parcel*/
    private Annonce(Parcel in) {
        UUIDANO = in.readString();
        idCategorieANO = in.readInt();
        utilisateurANO = in.readParcelable(Utilisateur.class.getClassLoader());
        priceANO = in.readInt();
        descriptionANO = in.readString();
        titreANO = in.readString();
        publishedANO = (boolean) in.readValue(Boolean.class.getClassLoader());
        datePublished = in.readLong();
        statutANO = in.readString();
        photos = new ArrayList<>();

        in.readTypedList(photos, Photo.CREATOR);
    }

    /* Constructeur vide */
    public Annonce(){
        super();
        this.UUIDANO = "";
        this.idCategorieANO = null;
        this.utilisateurANO = null;
        this.priceANO = 0;
        this.descriptionANO = "";
        this.titreANO = "";
        this.publishedANO = false;
        this.datePublished = (long) 0;
        this.statutANO = "";
        this.photos = new ArrayList<>();
    }

    public String getUUIDANO() {
        return UUIDANO;
    }

    public void setUUIDANO(String UUIDANO) {
        this.UUIDANO = UUIDANO;
    }

    public Integer getIdCategorieANO() {
        return idCategorieANO;
    }

    public void setIdCategorieANO(Integer idCategorieANO) {
        this.idCategorieANO = idCategorieANO;
    }

    public Utilisateur getUtilisateurANO() {
        return utilisateurANO;
    }

    public void setUtilisateurANO(Utilisateur utilisateurANO) {
        this.utilisateurANO = utilisateurANO;
    }

    public Integer getPriceANO() {
        return priceANO;
    }

    public void setPriceANO(Integer priceANO) {
        this.priceANO = priceANO;
    }

    public String getDescriptionANO() {
        return descriptionANO;
    }

    public void setDescriptionANO(String descriptionANO) {
        this.descriptionANO = descriptionANO;
    }

    public String getTitreANO() {
        return titreANO;
    }

    public void setTitreANO(String titreANO) {
        this.titreANO = titreANO;
    }

    public boolean isPublishedANO() {
        return publishedANO;
    }

    public void setPublishedANO(boolean publishedANO) {
        this.publishedANO = publishedANO;
    }

    public Long getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(Long datePublished) {
        this.datePublished = datePublished;
    }

    public String getStatutANO() {
        return statutANO;
    }

    public void setStatutANO(String statutANO) {
        this.statutANO = statutANO;
    }

    public ArrayList<Photo> getPhotos() {
        return this.photos;
    }

    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.UUIDANO);
        dest.writeInt(this.idCategorieANO);
        dest.writeParcelable(this.utilisateurANO, 0);
        dest.writeInt(this.priceANO);
        dest.writeString(this.descriptionANO);
        dest.writeString(this.titreANO);
        dest.writeValue(this.publishedANO);
        dest.writeLong(this.datePublished);
        dest.writeString(this.statutANO);
        dest.writeTypedList(this.photos);
    }
}
