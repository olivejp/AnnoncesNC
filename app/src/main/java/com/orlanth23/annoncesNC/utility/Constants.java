package com.orlanth23.annoncesNC.utility;

public class Constants {
    static final String IMAGE_DIRECTORY_NAME = "OliwebNcImageUpload";
    static final String CURRENCY = "xpf";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static final int MAX_IMAGE_SIZE = 500;
    public static final int ID_ALL_CAT = 999;

    public static final String PARAM_VIS = "VIS";
    public static final String PARAM_MAJ = "MAJ";
    public static final String PARAM_CRE = "CRE";

    static final String cryptoPass = "@nn0nc3s84";

    private static final String LOCAL_ENDPOINT = "http://annoncesnc.ddns.net";
    private static final String LOCAL_PORT_ENDPOINT = "8080";
    private static final String PACKAGE_NAME = "newAccountuser";
    private static final String DISTANT_ENDPOINT = "http://www.oliweb.nc";

    public static final String SERVER_PRIMARY_PAGE_UPLOAD = LOCAL_ENDPOINT + "/AnnoncesNC/fileUpload.php";
    public static final String SERVER_PRIMARY_DIRECTORY_UPLOAD = LOCAL_ENDPOINT + "/AnnoncesNC/uploads";
    public static final String SERVER_PRIMARY_ENDPOINT = LOCAL_ENDPOINT+":"+LOCAL_PORT_ENDPOINT+"/"+ PACKAGE_NAME +"/REST";

    public static final String SERVER_SECONDARY_PAGE_UPLOAD = DISTANT_ENDPOINT+ "/Annonces/fileUpload.php";
    public static final String SERVER_SECONDARY_DIRECTORY_UPLOAD = DISTANT_ENDPOINT + "/Annonces/uploads";
    public static final String SERVER_SECONDARY_ENDPOINT = DISTANT_ENDPOINT + "/"+ PACKAGE_NAME +"/REST";
}