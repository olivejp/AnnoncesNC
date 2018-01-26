package com.orlanth23.annoncesnc.domain;

import org.jetbrains.annotations.Contract;

public enum StatutPhoto {
    valid("V"),
    ToSend("T"),
    ToUpdate("U"),
    ToDelete("D");

    private String valeur;

    StatutPhoto(String valeur) {
        this.valeur = valeur;
    }

    @Contract(pure = true)
    public String valeur() {
        return valeur;
    }
}
