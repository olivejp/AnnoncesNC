package com.orlanth23.annoncesNC.lists;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orlanth23.annoncesNC.dto.Categorie;

import java.lang.reflect.Type;
import java.util.ArrayList;

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

    public static void setMyArrayListFromJson(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Categorie>>() {
        }.getType();
        ArrayList<Categorie> categories = gson.fromJson(json, listType);
        setMyArrayList(categories);
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
