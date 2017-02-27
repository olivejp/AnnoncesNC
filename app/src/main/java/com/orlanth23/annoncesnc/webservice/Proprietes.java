package com.orlanth23.annoncesnc.webservice;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Proprietes {

    public static final String FICHIER_CONF = "config.properties";
    public static final String FICHIER_CONF_LOCAL = "config-local.properties";
    public static String LOCAL_ENDPOINT = "LOCAL_ENDPOINT";
    public static String LOCAL_PORT_ENDPOINT = "LOCAL_PORT_ENDPOINT";
    public static String SERVER_PRIMARY_PAGE_UPLOAD = "SERVER_PRIMARY_PAGE_UPLOAD";
    public static String SERVER_PRIMARY_DIRECTORY_UPLOAD = "SERVER_PRIMARY_DIRECTORY_UPLOAD";
    public static String SERVER_PRIMARY_ENDPOINT = "SERVER_PRIMARY_ENDPOINT";
    public static String CRYPTO_PASS = "CRYPTO_PASS";
    private static Properties properties;

    public static String getProperty(String key) {
        if (properties != null) {
            return properties.getProperty(key);
        }
        synchronized (Proprietes.class) {
            try {
                if (properties == null) {
                    Class.forName(Proprietes.class.getName());
                    properties = new java.util.Properties();
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    InputStream propertiesStream = classLoader.getResourceAsStream(FICHIER_CONF_LOCAL);
                    if (propertiesStream != null) {
                        properties.load(propertiesStream);
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                Log.e("getProperty", e.getMessage(), e);
            }
        }
        return properties.getProperty(key);
    }

    @NonNull
    public static String getServerPageUpload() {
        return getProperty(LOCAL_ENDPOINT).concat(getProperty(SERVER_PRIMARY_PAGE_UPLOAD));
    }

    @NonNull
    public static String getServerDirectoryUploads() {
        return getProperty(LOCAL_ENDPOINT).concat(getProperty(SERVER_PRIMARY_DIRECTORY_UPLOAD));
    }

    @NonNull
    public static String getServerEndpoint() {
        String retour = getProperty(LOCAL_ENDPOINT).concat(":").concat(getProperty(LOCAL_PORT_ENDPOINT)).concat(getProperty(SERVER_PRIMARY_ENDPOINT));
        return retour;
    }
}
