package com.orlanth23.annoncesnc.dto;

import org.jetbrains.annotations.Contract;

public enum StatutAnnonce {
    UNREGISTRED("R"),
    VALID("V"),
    SOLD("S"),
    TOSEND("T");

    private String valeur;

    StatutAnnonce(String valeur) {
        this.valeur = valeur;
    }

    @Contract(pure = true)
    public String valeur() {
        return valeur;
    }
}
