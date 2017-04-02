package com.orlanth23.annoncesnc.dto;

import java.util.Map;

public class AnnonceFirebase {
    private String UUIDFirebaseAnnonce;
    private Integer idCategory;
    private String idUtilisateur;
    private String titreAnnonce;
    private String descriptionAnnonce;
    private Integer prixAnnonce;
    private Integer idLocal;
    private Map<String, String> creationDate;
    private Map<String, String> lastModificationDate;

    public void clear() {
        UUIDFirebaseAnnonce = "";
        idCategory = 0;
        idUtilisateur = "";
        titreAnnonce = "";
        descriptionAnnonce = "";
        prixAnnonce = 0;
        idLocal = 0;
        creationDate = null;
        lastModificationDate = null;
    }

    public String getUUIDFirebaseAnnonce() {
        return UUIDFirebaseAnnonce;
    }

    public void setUUIDFirebaseAnnonce(String UUIDAnnonce) {
        this.UUIDFirebaseAnnonce = UUIDAnnonce;
    }

    public Integer getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Integer idCategory) {
        this.idCategory = idCategory;
    }

    public String getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(String idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getTitreAnnonce() {
        return titreAnnonce;
    }

    public void setTitreAnnonce(String titreAnnonce) {
        this.titreAnnonce = titreAnnonce;
    }

    public String getDescriptionAnnonce() {
        return descriptionAnnonce;
    }

    public void setDescriptionAnnonce(String descriptionAnnonce) {
        this.descriptionAnnonce = descriptionAnnonce;
    }

    public Integer getPrixAnnonce() {
        return prixAnnonce;
    }

    public void setPrixAnnonce(Integer prixAnnonce) {
        this.prixAnnonce = prixAnnonce;
    }

    public Integer getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(Integer idLocal) {
        this.idLocal = idLocal;
    }

    public Map<String, String> getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Map<String, String> creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, String> getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Map<String, String> lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }
}
