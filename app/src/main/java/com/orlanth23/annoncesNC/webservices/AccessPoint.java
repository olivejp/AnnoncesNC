package com.orlanth23.annoncesNC.webservices;

/**
 * Created by olivejp on 28/06/2016.
 */
public class AccessPoint {
    /**
     * Initialisation à null
     */
    private static AccessPoint INSTANCE = null;

    private static String pref_default_server_url = "http://annonces.noip.me";
    private static String pref_default_server_page_uploads = "http://annonces.noip.me/AnnoncesNC/fileUpload.php";
    private static String pref_default_server_dir_uploads = "http://annonces.noip.me/AnnoncesNC/uploads";
    private static String ENDPOINT = "http://annonces.noip.me:8080/accountuser/REST";
    private static boolean isBackUp = false;

    /**
     * Point d'accès pour l'instance unique du singleton
     */
    public static AccessPoint getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AccessPoint();
        }
        return INSTANCE;
    }

    /**
     * Permet de récuperer les données du serveur de secours.
     */
    public static void getBackUp() {
        pref_default_server_url = "http://www.oliweb.nc";
        pref_default_server_page_uploads = "http://www.oliweb.nc/Annonces/fileUpload.php";
        pref_default_server_dir_uploads = "http://www.oliweb.nc/Annonces/uploads";
        ENDPOINT = "http://www.oliweb.nc/accountuser/REST";
        isBackUp = true;
    }

    public static void getPrincipalServer() {
        pref_default_server_url = "http://annonces.noip.me";
        pref_default_server_page_uploads = "http://annonces.noip.me/AnnoncesNC/fileUpload.php";
        pref_default_server_dir_uploads = "http://annonces.noip.me/AnnoncesNC/uploads";
        ENDPOINT = "http://annonces.noip.me:8080/accountuser/REST";
        isBackUp = false;
    }

    public static String getPref_default_server_url() {
        return pref_default_server_url;
    }

    public static String getPref_default_server_page_uploads() {
        return pref_default_server_page_uploads;
    }

    public static String getPref_default_server_dir_uploads() {
        return pref_default_server_dir_uploads;
    }

    public static String getENDPOINT() {
        return ENDPOINT;
    }

    public static boolean isBackUp() {
        return isBackUp;
    }
}
