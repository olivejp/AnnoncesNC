package com.orlanth23.annoncesNC.webservices;

import com.orlanth23.annoncesNC.dto.Annonce;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Query;

public interface RetrofitService {

    @GET("/register/doregister")
    void register(@Query("email") String email, @Query("password") String password, @Query("telephone") Integer telephone, Callback<ReturnWS> cb);

    @GET("/list/listcategory")
    void getListCategory(Callback<ReturnWS> cb);

    @GET("/list/listannoncebyuserwithpage")
    void getListAnnonceByUser(@Query("idUser") Integer idUser, @Query("page") Integer page, Callback<ArrayList<Annonce>> cb);

    @GET("/list/listannoncebycategorywithpage")
    void getListAnnonceByCategoryWithPage(@Query("idCategory") Integer idCategory, @Query("page") Integer page, Callback<ArrayList<Annonce>> cb);

    @GET("/search/dosearchwithpage")
    void searchAnnonceWithPage(@Query("keyword") String keyword, @Query("page") Integer page, Callback<ArrayList<Annonce>> cb);

    @GET("/post/dopostphoto")
    void postPhoto(@Query("idAnnonce") Integer idAnnonce, @Query("idPhoto") Integer idPhoto, @Query("nomPhoto") String nomPhoto, Callback<ReturnWS> cb);

    @GET("/post/dopostannonce")
    void postAnnonce(@Query("idCat") Integer idCat, @Query("idUser") Integer idUser, @Query("idAnnonce") Integer idAnnonce, @Query("titre") String titre, @Query("description") String description, @Query("prix") Integer prix, Callback<ReturnWS> cb);

    @GET("/login/dologin")
    void login(@Query("email") String email, @Query("password") String password, Callback<ReturnWS> cb);

    @DELETE("/post/deleteannonce")
    void deleteAnnonce(@Query("idAnnonce") Integer idAnnonce, Callback<ReturnWS> cb);

    @GET("/post/lostpassword")
    void doLostPassword(@Query("email") String email, Callback<ReturnWS> cb);

    @GET("/search/dosearchmultiparam")
    void searchAnnonceWithMultiparam(@Query("idCat") Integer idCat, @Query("minPrice") Integer minPrice,
                                     @Query("maxPrice") Integer maxPrice, @Query("keyword") String keyword,
                                     @Query("photo") boolean photo, @Query("page") Integer page, Callback<ArrayList<Annonce>> cb);

    @GET("/get/checkconnection")
    void checkConnection(Callback<ReturnWS> cb);

    @GET("/get/getnbuser")
    void getNbUser(Callback<ReturnWS> cb);

    @GET("/get/getnbannonce")
    void getNbAnnonce(Callback<ReturnWS> cb);

    @GET("/post/updateuser")
    void updateUser(@Query("idUser") Integer idUser, @Query("emailUser") String emailUser, @Query("telephoneUser") Integer telephoneUser, Callback<ReturnWS> cb);

    @GET("/post/unregister")
    void unregisterUser(@Query("idUser") Integer idUser, Callback<ReturnWS> cb);

    @GET("/post/changepassword")
    void changePassword(@Query("idUser") Integer idUser, @Query("oldPassword") String oldPassword, @Query("newPassword") String newPassword, Callback<ReturnWS> cb);

    @GET("/post/dodeletephoto")
    void deletePhoto(@Query("idAnnonce") Integer idAnnonce, @Query("idPhoto") Integer idPhoto, Callback<ReturnWS> cb);

    @GET("/list/listmessage")
    void getListMessage(@Query("idUser") Integer idUser, Callback<ReturnWS> cb);
}
