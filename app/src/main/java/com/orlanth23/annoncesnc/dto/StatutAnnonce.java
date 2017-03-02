package com.orlanth23.annoncesnc.dto;

import org.jetbrains.annotations.Contract;

public enum StatutAnnonce {
    Valid("V"),
    Sold("S"),
    ToPost("T"),
    ToUpdate("U"),
    ToDelete("D");

    private String valeur;

    StatutAnnonce(String valeur) {
        this.valeur = valeur;
    }

    @Contract(pure = true)
    public String valeur() {
        return valeur;
    }
}
