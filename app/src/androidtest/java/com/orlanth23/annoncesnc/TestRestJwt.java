package com.orlanth23.annoncesnc;

import android.support.test.runner.AndroidJUnit4;

import com.orlanth23.annoncesnc.utility.PasswordEncryptionService;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.ReturnWS;
import com.orlanth23.annoncesnc.webservice.ServiceUtilisateur;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import io.jsonwebtoken.Jwts;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertTrue;

/**
 * Test de récupération d'un JWT puir test d'authentification avec un JWT
 */
@RunWith(AndroidJUnit4.class)
public class TestRestJwt {

    private static String LOGIN = "TEST";
    private static String PASSWORD = "1234";
    private static String WRONG_PASSWORD = "5486146";
    private static String token;
    private ServiceUtilisateur serviceUtilisateur = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(ServiceUtilisateur.class);

    @Test
    public void testOkRestJwt() {
        // CallBack pour Authentification avec JWT
        final Callback<ReturnWS> callbackTestJwt = new Callback<ReturnWS>() {
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

        // CallBack pour OBTENTION du JWT
        final Callback<ReturnWS> callbackOkLoginJwt = new Callback<ReturnWS>() {
            @Override
            public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
                if (response.isSuccessful()) {
                    ReturnWS rs = response.body();
                    if (rs.statusValid()) {
                        // On teste les données récupérées du JWT
                        token = rs.getMsg();
                        String key = PasswordEncryptionService.desEncryptIt(Proprietes.getProperty(Proprietes.CRYPTO_PASS));
                        assertTrue(Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject().equals(Proprietes.getProperty(Proprietes.JWT_SUBJECT_LOGIN)));
                        assertTrue(Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getAudience().equals(LOGIN));

                        Call<ReturnWS> callTestJwt = serviceUtilisateur.testJwtHeader(token);
                        callTestJwt.enqueue(callbackTestJwt);


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

        // Appel du service pour OBTENTION d'un JWT
        Call<ReturnWS> callOkLogin = serviceUtilisateur.loginJwt(LOGIN, PasswordEncryptionService.desEncryptIt(PASSWORD));
        try {
            Response<ReturnWS> response = callOkLogin.execute();
            if (response.isSuccessful()) {
                ReturnWS rs = response.body();
                if (rs.statusValid()) {
                    // On teste les données récupérées du JWT
                    token = rs.getMsg();

                    String key = PasswordEncryptionService.desEncryptIt(Proprietes.getProperty(Proprietes.CRYPTO_PASS));
                    String subject = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();
                    String audience = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getAudience();

                    assertTrue("Sujet bien récupéré", subject.equals(Proprietes.getProperty(Proprietes.JWT_SUBJECT_LOGIN)));
                    assertTrue("Login bien récupéré", audience.equals(LOGIN));

                    Call<ReturnWS> callTestJwt = serviceUtilisateur.testJwtHeader(token);
                    response = callTestJwt.execute();
                    if (response.isSuccessful()) {
                        rs = response.body();
                        if (rs.statusValid()) {
                            assertTrue(true);
                        } else {
                            assertTrue(false);
                        }
                    } else {
                        assertTrue("Code incorrect", false);
                    }

                } else {
                    assertTrue(false);
                }
            } else {
                assertTrue("Le code renvoyé n'est pas correct.",response.code() == 200);
            }
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Sortie avec exception !",false);
        }
    }

    @Test
    public void testKoRestJwt() {

        // CallBack pour obtention du JWT
        final Callback<ReturnWS> callbackKoLoginJwt = new Callback<ReturnWS>() {
            @Override
            public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
                if (response.isSuccessful()) {
                    ReturnWS rs = response.body();
                    if (rs.statusValid()) {
                        assertTrue("Tentative de connexion avec un mauvais MDP.",false);
                    }else{
                        assertTrue(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnWS> call, Throwable t) {
                assertTrue(true);
            }
        };

        // Appel du service pour tester que le WS n'arrive pas à s'authentifier
        Call<ReturnWS> callKoLogin = serviceUtilisateur.loginJwt(LOGIN, PasswordEncryptionService.desEncryptIt(WRONG_PASSWORD));
        callKoLogin.enqueue(callbackKoLoginJwt);
    }
}
