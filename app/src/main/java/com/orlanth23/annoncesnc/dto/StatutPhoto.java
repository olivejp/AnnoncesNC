package com.orlanth23.annoncesnc.dto;

import org.jetbrains.annotations.Contract;

public enum StatutPhoto {
    VALID("V"),
    TOSEND("T");

    private String valeur;

    StatutPhoto(String valeur) {
        this.valeur = valeur;
    }

    @Contract(pure = true)
    public String valeur() {
        return valeur;
    }
}
