package com.orlanth23.annoncesNC.lists;


import com.orlanth23.annoncesNC.dto.Categorie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by orlanth23 on 28/08/2015.
 */
public class ListeCategories {

    /**
     * Initialisation à null
     */
    private static ListeCategories INSTANCE = null;

    private static HashMap<Integer, Categorie> myMap;
    private static ArrayList<Categorie> myArrayList;
    private static boolean maj = false;

    /**
     *
     * @param arrayList
     */
    private static void fillOtherList(ArrayList<Categorie> arrayList){
        // Alimentation du hashmap à partir du arraylist
        myMap = new HashMap<>();
        for (Categorie cat : arrayList) {
            myMap.put(cat.getIdCAT(), cat);
        }
    }

    /**
     * Point d'accès pour l'instance unique du singleton
     */
    public static ListeCategories getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ListeCategories();
        }
        return INSTANCE;
    }

    public static boolean isMaj() {
        return maj;
    }

    public static void setMyArrayList(ArrayList<Categorie> arrayList) {
        myArrayList = arrayList;

        // On va alimenter le hasmap à partir du arraylist
        fillOtherList(arrayList);

        maj = true;
    }

    /**
     * @return
     */
    public List<String> getListNameCategory() {
        List<String> categorieNames = new ArrayList<>();

        for (Categorie categorie : getListCategorie()) {
            categorieNames.add(categorie.getNameCAT());
        }
        return categorieNames;
    }

    public Categorie getById(Integer id) {
        return myMap.get(id);
    }

    public Categorie getByName(String nameCategorie) {
        for (Categorie cat : myArrayList) {
            if (cat.getNameCAT() == nameCategorie) {
                return cat;
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
