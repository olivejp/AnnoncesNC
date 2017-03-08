package com.orlanth23.annoncesnc.webservice;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface RetrofitService {

    /**
     * Test d'envoi d'un header avec un JWT à l'intérieur
     * @param token
     * @return
     */
    @POST("/REST/services/jwt-test")
    Call<ReturnWS> testJwtHeader(@Header("Token") String token);

    /**
     * Test de loggin pour récupérer un JWT
     * @param email
     * @param password
     * @return
     */
    @POST("/REST/utilisateurs/login-jwt")
    Call<ReturnWS> loginJwt(@Query("email") String email,
                         @Query("password") String password);

    @POST("/REST/services/infoserver")
    Call<ReturnWS> infoServer();

    @POST("/REST/services/checkconnection")
    Call<ReturnWS> checkConnection();

    @GET("/REST/annonces/count")
    Call<ReturnWS> getNbAnnonce();

    @GET("/REST/utilisateurs/count")
    Call<ReturnWS> getNbUser();

    @GET("/REST/categories")
    Call<ReturnWS> getListCategory();

    @GET("/REST/categories/{idCategory}/annonces")
    Call<ReturnWS> getListAnnonceByCategoryWithPage(@Path("idCategory") Integer idCategory,
                                                    @Query("page") Integer page);

    @GET("/REST/annonces/dosearchmultiparam")
    Call<ReturnWS> searchAnnonceWithPage(@Query("keyword") String keyword,
                                         @Query("page") Integer page);


    @GET("/REST/utilisateurs/{idUser}/annonces")
    Call<ReturnWS> getListAnnonceByUser(@Path("idUser") Integer idUser,
                                        @Query("page") Integer page);


    @GET("/REST/annonces/dosearchmultiparam")
    Call<ReturnWS> searchAnnonceWithMultiparam(@Query("idCat") Integer idCat,
                                               @Query("minPrice") Integer minPrice,
                                               @Query("maxPrice") Integer maxPrice,
                                               @Query("keyword") String keyword,
                                               @Query("photo") boolean photo,
                                               @Query("page") Integer page);


    @POST("/REST/utilisateurs")
    Call<ReturnWS> register(@Query("email") String email,
                            @Query("password") String password,
                            @Query("telephone") Integer telephone);


    @POST("/REST/photos")
    Call<ReturnWS> postPhoto(@Query("idAnnonce") Integer idAnnonce,
                             @Query("idPhoto") Integer idPhoto,
                             @Query("nomPhoto") String nomPhoto);

    @GET("/REST/photos/{idPhoto}")
    Call<ReturnWS> getPhoto(@Path("idPhoto") Integer idPhoto);

    @DELETE("/REST/photos/{idPhoto}")
    Call<ReturnWS> postPhoto(@Path("idPhoto") Integer idPhoto);

    @POST("/REST/annonces")
    Call<ReturnWS> postAnnonce(@Query("idCat") Integer idCat,
                               @Query("idUser") Integer idUser,
                               @Query("titre") String titre,
                               @Query("description") String description,
                               @Query("prix") Integer prix,
                               @Query("idLocal") Integer idLocal);

    @PUT("/REST/annonces/{idAnnonce}")
    Call<ReturnWS> putAnnonce(@Path("idAnnonce") Integer idAnnonce,
                               @Query("idCat") Integer idCat,
                               @Query("titre") String titre,
                               @Query("description") String description,
                               @Query("prix") Integer prix,
                               @Query("idLocal") Integer idLocal);

    @POST("/REST/utilisateurs/login")
    Call<ReturnWS> login(@Query("email") String email,
                         @Query("password") String password);

    @DELETE("/REST/annonces/{idAnnonce}")
    Call<ReturnWS> deleteAnnonce(@Path("idAnnonce") Integer idAnnonce);

    @DELETE("/REST/annonces/{idAnnonce}/photos/{idPhoto}")
    Call<ReturnWS> deletePhoto(@Path("idAnnonce") Integer idAnnonce,
                               @Path("idPhoto") Integer idPhoto);


    @POST("/REST/utilisateurs/lost-password")
    Call<ReturnWS> doLostPassword(@Query("email") String email);

    @PUT("/REST/utilisateurs/{idUser}")
    Call<ReturnWS> updateUser(@Path("idUser") Integer idUser,
                              @Query("emailUser") String emailUser,
                              @Query("telephoneUser") Integer telephoneUser);

    @DELETE("/REST/utilisateurs/{idUser}")
    Call<ReturnWS> unregisterUser(@Path("idUser") Integer idUser);

    @POST("/REST/utilisateurs/{idUser}/change-password")
    Call<ReturnWS> changePassword(@Path("idUser") Integer idUser,
                                  @Query("oldPassword") String oldPassword,
                                  @Query("newPassword") String newPassword);

    @GET("/REST/utilisateurs/{idUser}/messages")
    Call<ReturnWS> getListMessage(@Path("idUser") Integer idUser);
}
