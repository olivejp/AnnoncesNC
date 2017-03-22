package com.orlanth23.annoncesnc.webservice;

import java.util.HashMap;

public class InfoServer{
    private int nbUtilisateur;
    private int nbAnnonce;
    private HashMap<Integer, Integer> nbAnnonceByCategorie;

    public int getNbUtilisateur() {
        return nbUtilisateur;
    }

    public void setNbUtilisateur(int nbUtilisateur) {
        this.nbUtilisateur = nbUtilisateur;
    }

    public int getNbAnnonce() {
        return nbAnnonce;
    }

    public void setNbAnnonce(int nbAnnonce) {
        this.nbAnnonce = nbAnnonce;
    }

    public HashMap<Integer, Integer> getNbAnnonceByCategorie() {
        return nbAnnonceByCategorie;
    }

    public void setNbAnnonceByCategorie(HashMap<Integer, Integer> nbAnnonceByCategorie) {
        this.nbAnnonceByCategorie = nbAnnonceByCategorie;
    }
}
