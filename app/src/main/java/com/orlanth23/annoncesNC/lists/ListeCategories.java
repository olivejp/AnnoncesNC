package com.orlanth23.annoncesNC.lists;


import com.orlanth23.annoncesNC.dto.Categorie;

import java.util.ArrayList;


/**
 * Created by orlanth23 on 28/08/2015.
 */
public class ListeCategories {

    private static ListeCategories INSTANCE = null;
    private static ArrayList<Categorie> myArrayList;

    public static ListeCategories getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ListeCategories();
        }
        return INSTANCE;
    }

    public static void setMyArrayList(ArrayList<Categorie> arrayList) {
        myArrayList = arrayList;
    }

    public int getIndexByName(String nameCategorie) {
        int i = 0;
        int index = 0;
        for (Categorie cat : myArrayList) {
            if (cat.getNameCAT().equals(nameCategorie)) {
                index = i;
            }
            i++;
        }
        return index;
    }

    public ArrayList<Categorie> getListCategorie() {
        return myArrayList;
    }
}
