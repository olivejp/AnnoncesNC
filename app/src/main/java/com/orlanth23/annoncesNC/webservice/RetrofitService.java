package com.orlanth23.annoncesnc.webservice;

import com.orlanth23.annoncesnc.dto.Annonce;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface RetrofitService {

    @POST("/services/checkconnection")
    Call<ReturnWS> checkConnection();

    @GET("/annonces/count")
    Call<ReturnWS> getNbAnnonce();

    @GET("/utilisateurs/count")
    Call<ReturnWS> getNbUser();

    @GET("/categories")
    Call<ReturnWS> getListCategory();

    @GET("/categories/{idCagtegory}/annonces")
    Call<ArrayList<Annonce>> getListAnnonceByCategoryWithPage(@Path("idCategory") Integer idCategory,
                                                              @Query("page") Integer page);

    @GET("/annonces/dosearchmultiparam")
    Call<ArrayList<Annonce>> searchAnnonceWithPage(@Query("keyword") String keyword,
                                                   @Query("page") Integer page);


    @GET("/utilisateurs/{idUser}/annonces")
    Call<ArrayList<Annonce>> getListAnnonceByUser(@Path("idUser") Integer idUser,
                                                  @Query("page") Integer page);


    @GET("/annonces/dosearchmultiparam")
    Call<ArrayList<Annonce>> searchAnnonceWithMultiparam(@Query("idCat") Integer idCat,
                                                         @Query("minPrice") Integer minPrice,
                                                         @Query("maxPrice") Integer maxPrice,
                                                         @Query("keyword") String keyword,
                                                         @Query("photo") boolean photo,
                                                         @Query("page") Integer page);


    @POST("/utilisateurs")
    Call<ReturnWS> register(@Query("email") String email,
                                @Query("password") String password,
                                @Query("telephone") Integer telephone);


    @POST("/photos")
    Call<ReturnWS> postPhoto(@Query("idAnnonce") Integer idAnnonce,
                                 @Query("idPhoto") Integer idPhoto,
                                 @Query("nomPhoto") String nomPhoto);

    @POST("/annonces")
    Call<ReturnWS> postAnnonce(@Query("idCat") Integer idCat,
                                   @Query("idUser") Integer idUser,
                                   @Query("idAnnonce") Integer idAnnonce,
                                   @Query("titre") String titre,
                                   @Query("description") String description,
                                   @Query("prix") Integer prix);

    @POST("/utilisateurs/login")
    Call<ReturnWS> login(@Query("email") String email,
                             @Query("password") String password);

    @DELETE("/annonces/{idAnnonce}")
    Call<ReturnWS> deleteAnnonce(@Path("idAnnonce") Integer idAnnonce);

    @DELETE("/annonces/{idAnnonce}/photos")
    Call<ReturnWS> deletePhoto(@Path("idAnnonce") Integer idAnnonce,
                                   @Query("idPhoto") Integer idPhoto);


    @POST("/utilisateurs/lostpassword")
    Call<ReturnWS> doLostPassword(@Query("email") String email);

    @PUT("/utilisateurs/{idUser}")
    Call<ReturnWS> updateUser(@Path("idUser") Integer idUser,
                                  @Query("emailUser") String emailUser,
                                  @Query("telephoneUser") Integer telephoneUser);

    @DELETE("/utilisateurs/{idUser}")
    Call<ReturnWS> unregisterUser(@Path("idUser") Integer idUser);

    @POST("/utilisateurs/{idUser}/change-password")
    Call<ReturnWS> changePassword(@Path("idUser") Integer idUser,
                                  @Query("oldPassword") String oldPassword,
                                  @Query("newPassword") String newPassword);

    @GET("/utilisateurs/{idUser}/messages")
    Call<ReturnWS> getListMessage(@Path("idUser") Integer idUser);
}
