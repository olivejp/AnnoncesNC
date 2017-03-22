package com.orlanth23.annoncesnc.webservice;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface ServiceRest {
    @POST("/REST/services/infoserver")
    Call<ReturnWS> infoServer();

    @GET("/REST/categories/{idCategory}/annonces")
    Call<ReturnWS> getListAnnonceByCategoryWithPage(@Path("idCategory") Integer idCategory,
                                                    @Query("page") Integer page);
}
