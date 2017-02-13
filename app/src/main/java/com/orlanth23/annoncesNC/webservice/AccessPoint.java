package com.orlanth23.annoncesNC.webservice;

import com.orlanth23.annoncesNC.utility.Constants;

public class AccessPoint {

    private static AccessPoint INSTANCE = null;
    private static String serverPageUpload = Constants.SERVER_PRIMARY_PAGE_UPLOAD;
    private static String serverDirectoryUploads = Constants.SERVER_PRIMARY_DIRECTORY_UPLOAD;
    private static String serverEndpoint = Constants.SERVER_PRIMARY_ENDPOINT;
    private static boolean isBackUp = false;

    public static AccessPoint getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AccessPoint();
        }
        return INSTANCE;
    }

    public void changeToBackUpServer() {
        serverPageUpload = Constants.SERVER_SECONDARY_PAGE_UPLOAD;
        serverDirectoryUploads = Constants.SERVER_SECONDARY_DIRECTORY_UPLOAD;
        serverEndpoint = Constants.SERVER_SECONDARY_ENDPOINT;
        isBackUp = true;
    }

    public void changeToPrincipalServer() {
        serverPageUpload = Constants.SERVER_PRIMARY_PAGE_UPLOAD;
        serverDirectoryUploads = Constants.SERVER_PRIMARY_DIRECTORY_UPLOAD;
        serverEndpoint = Constants.SERVER_PRIMARY_ENDPOINT;
        isBackUp = false;
    }
    public String getServerPageUpload() {
        return serverPageUpload;
    }

    public String getServerDirectoryUploads() {
        return serverDirectoryUploads;
    }
    public String getServerEndpoint() {
        return serverEndpoint;
    }

    public boolean isBackUp() {
        return isBackUp;
    }
}
