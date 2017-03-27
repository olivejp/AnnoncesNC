package com.orlanth23.annoncesnc.webservice;

import java.util.HashMap;

public class InfoServer{
    private long nbUtilisateur;
    private long nbAnnonce;
    private HashMap<Integer, Integer> nbAnnonceByCategorie;

    public long getNbUtilisateur() {
        return nbUtilisateur;
    }

    public void setNbUtilisateur(long nbUtilisateur) {
        this.nbUtilisateur = nbUtilisateur;
    }

    public long getNbAnnonce() {
        return nbAnnonce;
    }

    public void setNbAnnonce(long nbAnnonce) {
        this.nbAnnonce = nbAnnonce;
    }

    public HashMap<Integer, Integer> getNbAnnonceByCategorie() {
        return nbAnnonceByCategorie;
    }

    public void setNbAnnonceByCategorie(HashMap<Integer, Integer> nbAnnonceByCategorie) {
        this.nbAnnonceByCategorie = nbAnnonceByCategorie;
    }
}
