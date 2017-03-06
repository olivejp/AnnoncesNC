package com.orlanth23.annoncesnc.list;

import android.content.Context;
import android.content.res.Resources;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dto.Categorie;

import java.util.ArrayList;
import java.util.HashMap;

public class ListeCategories {

    private static ListeCategories INSTANCE = null;
    private static ArrayList<Categorie> myArrayList = new ArrayList<>();

    public static ListeCategories getInstance(Context context) {
        if (INSTANCE == null) {
            Resources res = context.getResources();
            String[] categorie_title = res.getStringArray(R.array.categorie_title_array);
            String[] categorie_color = res.getStringArray(R.array.categorie_color_array);

            int i = 0;
            for (String title : categorie_title) {
                i++;
                String color = categorie_color[i-1];
                Categorie categorie = new Categorie(i, title, color, 0);
                myArrayList.add(categorie);
            }

            INSTANCE = new ListeCategories();
        }
        return INSTANCE;
    }

    public static boolean setNbAnnonceFromHashMap(Context context, HashMap<Integer, Integer> pHashMap) {
        if(pHashMap == null) {
            return false;
        }

        if(pHashMap.isEmpty()){
            return false;
        }

        ListeCategories listeCategories = ListeCategories.getInstance(context);
        for (Categorie categorie : listeCategories.getListCategorie()) {
            Integer nbAnnonce = pHashMap.get(categorie.getIdCAT());
            if (nbAnnonce != null) {
                categorie.setNbAnnonceCAT(nbAnnonce);
            }
        }

        return true;
    }

    public Categorie getCategorieById(Integer idCategorie) {
        for (Categorie cat : myArrayList) {
            if (cat.getIdCAT().equals(idCategorie)) {
                return  cat;
            }
        }
        return null;
    }

    public Categorie getCategorieByName(String nameCategorie) {
        for (Categorie cat : myArrayList) {
            if (cat.getNameCAT().equals(nameCategorie)) {
                return  cat;
            }
        }
        return null;
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
