package com.orlanth23.annoncesnc.interfaces;

import com.orlanth23.annoncesnc.dto.Utilisateur;

public interface CustomUserSignCallback {
    void onCompleteUserSign(Utilisateur user);

    void onCancelledUserSign();

    void onFailureUserSign();
}
