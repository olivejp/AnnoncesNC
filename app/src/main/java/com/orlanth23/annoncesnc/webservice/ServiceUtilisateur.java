package com.orlanth23.annoncesnc.webservice;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServiceUtilisateur {
    /**
     * Test d'envoi d'un header avec un JWT à l'intérieur
     * @param token
     * @return
     */
    @POST("/REST/services/jwt-test")
    Call<ReturnWS> testJwtHeader(@Header("Authorization") String token);

    /**
     * Test de loggin pour récupérer un JWT
     * @param email
     * @param password
     * @return
     */
    @POST("/REST/utilisateurs/login-jwt")
    Call<ReturnWS> loginJwt(@Query("email") String email,
                            @Query("password") String password);

    @GET("/REST/utilisateurs/{idUser}/annonces")
    Call<ReturnWS> getListAnnonceByUser(@Path("idUser") Integer idUser,
                                        @Query("page") Integer page);

    @POST("/REST/utilisateurs")
    Call<ReturnWS> register(@Query("email") String email,
                            @Query("password") String password,
                            @Query("telephone") Integer telephone);

    @POST("/REST/utilisateurs/login")
    Call<ReturnWS> login(@Query("email") String email,
                         @Query("password") String password);

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
