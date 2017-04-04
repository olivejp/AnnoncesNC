package com.orlanth23.annoncesnc.utility;

import android.util.Base64;
import android.util.Log;

import com.orlanth23.annoncesnc.webservice.Proprietes;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class PasswordEncryptionService {

    private static SecretKey getSecretKey() throws InvalidKeySpecException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String crypto = Proprietes.getProperty(Proprietes.CRYPTO_PASS);
        DESKeySpec keySpec = new DESKeySpec(crypto.getBytes("UTF8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        return keyFactory.generateSecret(keySpec);
    }

    public static String desEncryptIt(String value) {
        try {
            byte[] clearText = value.getBytes("UTF8");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            return Base64.encodeToString(cipher.doFinal(clearText), Base64.DEFAULT);
        } catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            Log.e("desEncryptIt", e.getMessage(), e);
        }
        return value;
    }

    public static String desDecryptIt(@NotNull String value) {
        try {
            byte[] encrypedPwdBytes = Base64.decode(value, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decrypedValueBytes = (cipher.doFinal(encrypedPwdBytes));
            return new String(decrypedValueBytes);
        } catch (InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            Log.e("desDecryptIt", e.getMessage(), e);
        }
        return value;
    }
}