package com.orlanth23.annoncesnc.dto;

import org.jetbrains.annotations.Contract;

public enum StatutAnnonce {
    Valid("V"),
    Sold("S"),
    ToPost("T"),
    InPosting("P"),
    ToUpdate("U"),
    InUpdating("A"),
    ToDelete("D"),
    InDeleting("E");

    private String valeur;

    StatutAnnonce(String valeur) {
        this.valeur = valeur;
    }

    @Contract(pure = true)
    public String valeur() {
        return valeur;
    }
}
