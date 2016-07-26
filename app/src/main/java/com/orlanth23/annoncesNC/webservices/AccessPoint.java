package com.orlanth23.annoncesNC.webservices;

import com.orlanth23.annoncesNC.utility.Constants;

/**
 * Created by olivejp on 28/06/2016.
 */
public class AccessPoint {

    private static AccessPoint INSTANCE = null;
    private static String defaultServerPageUpload;
    private static String defaultServerDirectoryUploads;
    private static String defaultServerEndpoint;
    private static boolean isBackUp = false;

    public static AccessPoint getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AccessPoint();
        }
        return INSTANCE;
    }

    public static void getBackUpServer() {
        defaultServerPageUpload = Constants.SERVER_SECONDARY_PAGE_UPLOAD;
        defaultServerDirectoryUploads = Constants.SERVER_SECONDARY_DIRECTORY_UPLOAD;
        defaultServerEndpoint = Constants.SERVER_SECONDARY_ENDPOINT;
        isBackUp = true;
    }

    public static void getPrincipalServer() {
        defaultServerPageUpload = Constants.SERVER_PRIMARY_PAGE_UPLOAD;
        defaultServerDirectoryUploads = Constants.SERVER_PRIMARY_DIRECTORY_UPLOAD;
        defaultServerEndpoint = Constants.SERVER_PRIMARY_ENDPOINT;
        isBackUp = false;
    }
    public static String getDefaultServerPageUpload() {
        return defaultServerPageUpload;
    }

    public static String getDefaultServerDirectoryUploads() {
        return defaultServerDirectoryUploads;
    }
    public static String getDefaultServerEndpoint() {
        return defaultServerEndpoint;
    }

    public static boolean isBackUp() {
        return isBackUp;
    }
}
