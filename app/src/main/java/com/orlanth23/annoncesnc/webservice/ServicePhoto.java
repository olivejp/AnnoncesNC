package com.orlanth23.annoncesnc.webservice;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServicePhoto {
    @POST("/REST/photos")
    Call<ReturnWS> postPhoto(@Query("idAnnonce") Integer idAnnonce,
                             @Query("idPhoto") Integer idPhoto,
                             @Query("nomPhoto") String nomPhoto);

    @GET("/REST/photos/{idPhoto}")
    Call<ReturnWS> getPhoto(@Path("idPhoto") Integer idPhoto);

    @DELETE("/REST/photos/{idPhoto}")
    Call<ReturnWS> deletePhoto(@Path("idPhoto") Integer idPhoto);
}
