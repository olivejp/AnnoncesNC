package com.orlanth23.annoncesnc.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.orlanth23.annoncesnc.database.provider.ProviderContract;
import com.orlanth23.annoncesnc.sync.AnnoncesAuthenticatorService;
import com.orlanth23.annoncesnc.utility.Utility;

public class AnnoncesReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Utility.checkWifiAndMobileData(context)) {
            context.getContentResolver();
            ContentResolver.requestSync(AnnoncesAuthenticatorService.getAccount(), ProviderContract.CONTENT_AUTHORITY, Bundle.EMPTY);
        }
    }
}
