package com.orlanth23.annoncesNC.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by orlanth23 on 17/08/2015.
 */
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

    private Integer idANO;               // Identifiant
    private Categorie categorieANO;      // Une annonce appartient à une catégorie
    private Utilisateur ownerANO;        // Une annonce est rédigée par une personne
    private Integer priceANO;            // L'annonce a un prix
    private String descriptionANO;       // Description de l'annonce
    private String titreANO;             // Titre de l'annonce
    private boolean publishedANO;        // True si l'annonce est publiée sinon False
    private Long datePublished;          // La date de la parution
    private ArrayList<Photo> photos;     // Les photos de l'annonce

    // Nouvelle liste pour les photos
    // private ArrayList listPhoto;   // La liste des photos de l'annonce

    /* Constructeurs */
    public Annonce(Integer idANO, Categorie categorieANO, Utilisateur ownerANO, Integer priceANO, String descriptionANO, String titreANO, boolean publishedANO, Long datePublished, ArrayList<Photo> listPhoto) {
        this.idANO = idANO;
        this.categorieANO = categorieANO;
        this.ownerANO = ownerANO;
        this.priceANO = priceANO;
        this.descriptionANO = descriptionANO;
        this.titreANO = titreANO;
        this.publishedANO = publishedANO;
        this.datePublished = datePublished;
        this.photos = listPhoto;
    }

    /* Constructeur à partir d'un Parcel*/
    public Annonce(Parcel in) {
        idANO = in.readInt();
        categorieANO = in.readParcelable(Categorie.class.getClassLoader());
        ownerANO = in.readParcelable(Utilisateur.class.getClassLoader());
        priceANO = in.readInt();
        descriptionANO = in.readString();
        titreANO = in.readString();
        publishedANO = (boolean) in.readValue(Boolean.class.getClassLoader());
        datePublished = in.readLong();
        photos = new ArrayList<>();
        in.readTypedList(photos, Photo.CREATOR);
    }

    /* Constructeur vide */
    public Annonce(){
        super();
        this.idANO = 0;
        this.categorieANO = null;
        this.ownerANO = null;
        this.priceANO = 0;
        this.descriptionANO = "";
        this.titreANO = "";
        this.publishedANO = false;
        this.datePublished = (long) 0;
        this.photos = new ArrayList();
    }

    public Integer getIdANO() {
        return idANO;
    }

    public void setIdANO(Integer idANO) {
        this.idANO = idANO;
    }

    public Categorie getCategorieANO() {
        return categorieANO;
    }

    public void setCategorieANO(Categorie categorieANO) {
        this.categorieANO = categorieANO;
    }

    public Utilisateur getOwnerANO() {
        return ownerANO;
    }

    public void setOwnerANO(Utilisateur ownerANO) {
        this.ownerANO = ownerANO;
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
        dest.writeInt(this.idANO);
        dest.writeParcelable(this.categorieANO, 0);
        dest.writeParcelable(this.ownerANO, 0);
        dest.writeInt(this.priceANO);
        dest.writeString(this.descriptionANO);
        dest.writeString(this.titreANO);
        dest.writeValue(this.publishedANO);
        dest.writeLong(this.datePublished);
        dest.writeTypedList(this.photos);
    }
}
