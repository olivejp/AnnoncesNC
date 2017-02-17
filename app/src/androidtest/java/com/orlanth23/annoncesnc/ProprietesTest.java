package com.orlanth23.annoncesnc;

import android.support.test.runner.AndroidJUnit4;

import com.orlanth23.annoncesnc.webservice.Proprietes;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class ProprietesTest {

    @Test
    public void testAccessPoint(){
        String localEndpoint = Proprietes.getProperty(Proprietes.LOCAL_ENDPOINT);
        assertEquals(localEndpoint, "http://annoncesnc.ddns.net");
    }
}
