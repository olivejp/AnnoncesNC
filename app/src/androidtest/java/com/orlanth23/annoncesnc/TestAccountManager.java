package com.orlanth23.annoncesnc;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.orlanth23.annoncesnc.sync.AnnoncesAuthenticatorService;

import org.junit.Before;
import org.junit.Test;

public class TestAccountManager {

    private Context mContext;

    @Before
    public void precondition() {
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testGetAccount() {
        AccountManager am = AccountManager.get(mContext);
        Account[] accounts = am.getAccountsByType(AnnoncesAuthenticatorService.ACCOUNT_TYPE);
    }

}
