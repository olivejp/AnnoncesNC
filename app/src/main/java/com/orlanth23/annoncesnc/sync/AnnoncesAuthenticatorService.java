package com.orlanth23.annoncesnc.sync;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

// Vu sur la vidéo : https://www.youtube.com/watch?v=DaSgkc_bDkE
public class AnnoncesAuthenticatorService extends Service {

    private static final String ACCOUNT_NAME = "sync";
    private static final String ACCOUNT_TYPE = "annoncesnc.orlanth23.com";
    private AnnoncesAuthenticator mAuthenticator;

    public static Account GetAccount() {
        // Note: Normally the account name is set to the user's identity (username or email
        // address). However, since we aren't actually using any user accounts, it makes more sense
        // to use a generic string in this case.
        //
        // This string should *not* be localized. If the user switches locale, we would not be
        // able to locate the old account, and may erroneously register multiple accounts.
        final String accountName = ACCOUNT_NAME;

        return new Account(accountName, ACCOUNT_TYPE);
    }

    @Override
    public void onCreate() {
        mAuthenticator = new AnnoncesAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
