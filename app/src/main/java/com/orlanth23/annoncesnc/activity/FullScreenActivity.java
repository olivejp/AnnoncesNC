package com.orlanth23.annoncesnc.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.orlanth23.annoncesnc.database.DictionaryDAO;
import com.orlanth23.annoncesnc.receiver.AnnoncesReceiver;

public class FullScreenActivity extends AppCompatActivity {

    private AnnoncesReceiver annoncesReceiver;

    @Override
    protected void onStart() {
        super.onStart();

        // Création d'un broadcast pour écouter si la connectivité à changer et appeler le syncAdapter
        annoncesReceiver = new AnnoncesReceiver();
        IntentFilter ifilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(annoncesReceiver, ifilter);

        Intent intent = new Intent();

        String firstTime = DictionaryDAO.getValueByKey(this, DictionaryDAO.Dictionary.DB_CLEF_FIRST_TIME);
        if (firstTime != null) {
            // Appel du MainActivity
            intent.setClass(this, MainActivity.class);
            startActivity(intent);
        } else {
            intent.setClass(this, SwipeActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(annoncesReceiver);
    }
}
