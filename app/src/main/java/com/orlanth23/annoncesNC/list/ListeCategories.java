package com.orlanth23.annoncesnc.list;

import android.content.Context;
import android.content.res.Resources;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dto.Categorie;

import java.lang.reflect.Type;
import java.util.ArrayList;

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
                Categorie categorie = new Categorie(i, title, categorie_color[i-1], 0);
                myArrayList.add(categorie);
            }

            INSTANCE = new ListeCategories();
        }
        return INSTANCE;
    }

    public static boolean setNbAnnonceFromJson(String json) {
        if(json == null) {
            return false;
        }

        if(json.isEmpty()){
            return false;
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Categorie>>() {
        }.getType();
        ArrayList<Categorie> categories = gson.fromJson(json, listType);

        if (categories.isEmpty()){
            return false;
        }

        for (Categorie categorieOut : categories) {
            for (Categorie categorieIn : myArrayList) {
                if (categorieOut.getIdCAT().equals(categorieIn.getIdCAT())){
                    categorieIn.setNbAnnonceCAT(categorieOut.getNbAnnonceCAT());
                }
            }
        }

        return true;
    }

    public Categorie getCategorieByName(String nameCategorie) {
        int i = 0;
        int index = 0;
        for (Categorie cat : myArrayList) {
            if (cat.getNameCAT().equals(nameCategorie)) {
                return  cat;
            }
            i++;
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
