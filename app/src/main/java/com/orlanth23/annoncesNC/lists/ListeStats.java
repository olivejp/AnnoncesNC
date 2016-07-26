package com.orlanth23.annoncesNC.lists;

/**
 * Created by olivejp on 26/05/2016.
 */
public class ListeStats {

    private static ListeStats INSTANCE = null;
    private static Integer nbAnnonces;
    private static Integer nbUsers;

    public static ListeStats getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ListeStats();
        }
        return INSTANCE;
    }

    public static Integer getNbAnnonces() {
        return nbAnnonces;
    }

    public static void setNbAnnonces(Integer nbAnnonces) {
        ListeStats.nbAnnonces = nbAnnonces;
    }

    public static Integer getNbUsers() {
        return nbUsers;
    }

    public static void setNbUsers(Integer nbUsers) {
        ListeStats.nbUsers = nbUsers;
    }
}
