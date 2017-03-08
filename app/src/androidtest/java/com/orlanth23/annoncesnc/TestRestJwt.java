package com.orlanth23.annoncesnc;

import android.support.test.runner.AndroidJUnit4;

import com.orlanth23.annoncesnc.utility.PasswordEncryptionService;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.RetrofitService;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import org.junit.Test;
import org.junit.runner.RunWith;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TestRestJwt {

    @Test
    public void testRestJwt() {

        Callback<ReturnWS> callbackLoginJwt = new Callback<ReturnWS>() {
            @Override
            public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
                if (response.isSuccessful()) {
                    ReturnWS rs = response.body();
                    if (rs.statusValid()) {
                        assertTrue(true);
                    } else {
                        assertTrue(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnWS> call, Throwable t) {
                assertTrue(false);
            }
        };

        RetrofitService retrofitService = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitService.class);
        Call<ReturnWS> call = retrofitService.loginJwt("TEST", PasswordEncryptionService.desEncryptIt("1234"));
        call.enqueue(callbackLoginJwt);
    }
}
