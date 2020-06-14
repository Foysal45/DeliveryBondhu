package com.ajkerdeal.app.essential.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class AESEncryptionClass {
    private static String INIT_VECTOR_PARAM = "LBFB3BAKuKtnAN";
    private static String PASSWORD = "3byamAfK";
    private static String SALT_KEY = "Gne8A5C5";

    private static SecretKeySpec generateAESKey() throws NoSuchAlgorithmException, InvalidKeySpecException {

        // Prepare password and salt key.
        char[] password = new String(Base64.decode(PASSWORD, Base64.DEFAULT)).toCharArray();
        byte[] salt = new String(Base64.decode(SALT_KEY, Base64.DEFAULT)).getBytes(StandardCharsets.UTF_8);

        // Create object of  [Password Based Encryption Key Specification] with required iteration count and key length.
        KeySpec spec = new PBEKeySpec(password, salt, 64, 256);

        // Now create AES Key using required hashing algorithm.
        SecretKey key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec);

        // Get encoded bytes of secret key.
        byte[] bytesSecretKey = key.getEncoded();

        // Create specification for AES Key.
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytesSecretKey, "AES");
        return secretKeySpec;
    }

    /**
     * Call this method to encrypt the readable plain text and get Base64 of encrypted bytes.
     */
    public static String encryptMessage(String message) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {

        byte[] initVectorParamBytes = new String(Base64.decode(INIT_VECTOR_PARAM, Base64.DEFAULT)).getBytes(StandardCharsets.UTF_8);

        Cipher encryptionCipherBlock = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encryptionCipherBlock.init(Cipher.ENCRYPT_MODE, generateAESKey(), new IvParameterSpec(initVectorParamBytes));

        byte[] messageBytes = message.getBytes();
        byte[] cipherTextBytes = encryptionCipherBlock.doFinal(messageBytes);
        String encryptedText = Base64.encodeToString(cipherTextBytes, Base64.DEFAULT);
        return encryptedText;
    }

    /**
     * Call this method to decrypt the Base64 of encrypted message and get readable plain text.
     */
    public static String decryptMessage(String base64Cipher) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {

        byte[] initVectorParamBytes = new String(Base64.decode(INIT_VECTOR_PARAM, Base64.DEFAULT)).getBytes(StandardCharsets.UTF_8);

        Cipher decryptionCipherBlock = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptionCipherBlock.init(Cipher.DECRYPT_MODE, generateAESKey(), new IvParameterSpec(initVectorParamBytes));

        byte[] cipherBytes = Base64.decode(base64Cipher, Base64.DEFAULT);
        byte[] messageBytes = decryptionCipherBlock.doFinal(cipherBytes);
        String plainText = new String(messageBytes);
        return plainText;
    }

}
