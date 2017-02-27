package com.orlanth23.annoncesnc;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.orlanth23.annoncesnc.list.ListeCategories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TestListeCategorie {

    private Context mContext;

    private String jsonSample = "[{'idCAT': 1," +
        "'nameCAT': 'Immobilier'," +
        "'couleurCAT': '#1BA900'," +
        "'nbAnnonceCAT': 9}, {" +
        "'idCAT': 2," +
        "'nameCAT': 'Automobile'," +
        "'couleurCAT': '#88A900'," +
        "'nbAnnonceCAT': 2}]";

    @Before
    public void precondition() {
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testListeCategorie() {
        ListeCategories listeCategories = ListeCategories.getInstance(mContext);
        assertTrue(listeCategories.getListCategorie().size() > 0);

        ListeCategories.setNbAnnonceFromJson(jsonSample);
        assertTrue(listeCategories.getCategorieByName("Immobilier").getNbAnnonceCAT() == 9);
        assertTrue(listeCategories.getCategorieByName("Automobile").getNbAnnonceCAT() == 2);
    }
}
