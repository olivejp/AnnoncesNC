package com.orlanth23.annoncesnc.ui.interfaces;

import com.orlanth23.annoncesnc.domain.Utilisateur;

public interface CustomUserSignCallback {
    void onCompleteUserSign(Utilisateur user);

    void onCancelledUserSign();

    void onFailureUserSign(Exception e);
}
