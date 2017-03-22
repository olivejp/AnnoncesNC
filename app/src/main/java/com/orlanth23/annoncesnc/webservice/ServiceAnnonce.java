package com.orlanth23.annoncesnc.webservice;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServiceAnnonce {

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

    @DELETE("/REST/annonces/{idAnnonce}")
    Call<ReturnWS> deleteAnnonce(@Path("idAnnonce") Integer idAnnonce);

    @DELETE("/REST/annonces/{idAnnonce}/photos/{idPhoto}")
    Call<ReturnWS> deletePhoto(@Path("idAnnonce") Integer idAnnonce,
                               @Path("idPhoto") Integer idPhoto);

    @GET("/REST/annonces/dosearchmultiparam")
    Call<ReturnWS> searchAnnonceWithPage(@Query("keyword") String keyword,
                                         @Query("page") Integer page);

    @GET("/REST/annonces/dosearchmultiparam")
    Call<ReturnWS> searchAnnonceWithMultiparam(@Query("idCat") Integer idCat,
                                               @Query("minPrice") Integer minPrice,
                                               @Query("maxPrice") Integer maxPrice,
                                               @Query("keyword") String keyword,
                                               @Query("photo") boolean photo,
                                               @Query("page") Integer page);
}
