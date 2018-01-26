package com.orlanth23.annoncesnc;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.orlanth23.annoncesnc.domain.list.ListeCategories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TestListeCategorie {

    private Context mContext;
    private HashMap<Integer, Integer> hashMap;

    @Before
    public void precondition() {
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testListeCategorie() {
        ListeCategories listeCategories = ListeCategories.getInstance(mContext);
        hashMap = new HashMap<>();
        hashMap.put(listeCategories.getCategorieByName("Immobilier").getIdCAT(), 9);
        hashMap.put(listeCategories.getCategorieByName("Automobile").getIdCAT(), 2);
        assertTrue(listeCategories.getListCategorie().size() > 0);

        ListeCategories.setNbAnnonceFromHashMap(mContext, hashMap);
        assertTrue(listeCategories.getCategorieByName("Immobilier").getNbAnnonceCAT() == 9);
        assertTrue(listeCategories.getCategorieByName("Automobile").getNbAnnonceCAT() == 2);
    }
}
