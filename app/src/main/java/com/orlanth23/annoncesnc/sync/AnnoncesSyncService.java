package com.orlanth23.annoncesnc.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AnnoncesSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static AnnoncesSyncAdapter sAnnonceSyncAdapter = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("AnnoncesSyncService", "onCreate - AnnoncesSyncService");
        synchronized (sSyncAdapterLock){
            if (sAnnonceSyncAdapter == null){
                sAnnonceSyncAdapter = new AnnoncesSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sAnnonceSyncAdapter.getSyncAdapterBinder();
    }
}
