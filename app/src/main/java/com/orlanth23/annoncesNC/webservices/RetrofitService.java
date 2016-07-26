package com.orlanth23.annoncesNC.webservices;

import com.orlanth23.annoncesNC.dto.Annonce;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by olivejp on 06/04/2016.
 */
public interface RetrofitService {

    @GET("/register/retrofitdoregister")
    void register(@Query("email") String email, @Query("password") String password, @Query("telephone") Integer telephone, Callback<ReturnClass> cb);

    @GET("/list/retrofitlistcategory")
    void listcategorie(Callback<ReturnClass> cb);

    @GET("/list/retrofitlistannoncebyuserwithpage")
    void listannoncebyuser(@Query("idUser") Integer idUser, @Query("page") Integer page, Callback<ArrayList<Annonce>> cb);

    @GET("/list/retrofitlistannoncebycategorywithpage")
    void listannoncebycategorywithpage(@Query("idCategory") Integer idCategory, @Query("page") Integer page, Callback<ArrayList<Annonce>> cb);

    @GET("/search/retrofitdosearchwithpage")
    void dosearchwithpage(@Query("keyword") String keyword, @Query("page") Integer page, Callback<ArrayList<Annonce>> cb);

    @GET("/post/retrofitdopostphoto")
    void doPostPhoto(@Query("idAnnonce") Integer idAnnonce, @Query("idPhoto") Integer idPhoto, @Query("nomPhoto") String nomPhoto, Callback<ReturnClass> cb);

    @GET("/post/retrofitdopostannonce")
    void doPostAnnonce(@Query("idCat") Integer idCat, @Query("idUser") Integer idUser, @Query("idAnnonce") Integer idAnnonce, @Query("titre") String titre, @Query("description") String description, @Query("prix") Integer prix, Callback<ReturnClass> cb);

    @GET("/login/retrofitdologin")
    void doLogin(@Query("email") String email, @Query("password") String password, Callback<ReturnClass> cb);

    @DELETE("/post/retrofitdeleteannonce")
    void deleteAnnonce(@Query("idAnnonce") Integer idAnnonce, Callback<ReturnClass> cb);

    @GET("/post/retrofitlostpassword")
    void doLostPassword(@Query("email") String email, Callback<ReturnClass> cb);

    @GET("/search/retrofitdosearchmultiparam")
    void doSearchMultiparam(@Query("idCat") Integer idCat, @Query("minPrice") Integer minPrice,
                            @Query("maxPrice") Integer maxPrice, @Query("keyword") String keyword,
                            @Query("photo") boolean photo, @Query("page") Integer page, Callback<ArrayList<Annonce>> cb);

    @GET("/get/retrofitcheckconnection")
    void checkConnection(Callback<ReturnClass> cb);

    @GET("/get/retrofitgetnbuser")
    void getNbUser(Callback<ReturnClass> cb);

    @GET("/get/retrofitgetnbannonce")
    void getNbAnnonce(Callback<ReturnClass> cb);

    @GET("/post/retrofitupdateuser")
    void doUpdateUser(@Query("idUser") Integer idUser, @Query("emailUser") String emailUser, @Query("telephoneUser") Integer telephoneUser, Callback<ReturnClass> cb);

    @GET("/post/retrofitunregister")
    void doUnregisterUser(@Query("idUser") Integer idUser, Callback<ReturnClass> cb);

    @GET("/post/retrofitchangepassword")
    void postChangePassword(@Query("idUser") Integer idUser, @Query("oldPassword") String oldPassword, @Query("newPassword") String newPassword, Callback<ReturnClass> cb);

    @GET("/post/retrofitdodeletephoto")
    void doDeletePhoto(@Query("idAnnonce") Integer idAnnonce, @Query("idPhoto") Integer idPhoto, Callback<ReturnClass> cb);

    @GET("/list/retrofitlistmessage")
    void getListMessage(@Query("idUser") Integer idUser, Callback<ReturnClass> cb);
}
