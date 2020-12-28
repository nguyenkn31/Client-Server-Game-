package nonGUI_755;

import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class AES {
    private static final String encryptionKey = "ABCDEFGHIJKLMNOP";
    public static String encrypt(String plaintext) {
        String encryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            byte[] key = encryptionKey.getBytes("UTF-8");
            
            /* The SecretKeySpec provides the mechanism of converting byte data into a 
             * secret key suitable to be passed to init() method of the Cipher class. */
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            
            
            /* Now we generate an initialization vector (IV) */
            /* To produce different ciphertext with each run of the encryption, we use a random initialization vector. */
            byte[] IV = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(IV);
            
            IvParameterSpec ivspec = new IvParameterSpec(key);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF8"));
            Base64.Encoder encoder = Base64.getEncoder();
            encryptedText = encoder.encodeToString(cipherText);
        } catch(Exception e) {}
        return encryptedText;
    }

    public static String decrypt(String ciphertext) {
        String decryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            byte[] key = encryptionKey.getBytes("UTF-8");
            
            /* The SecretKeySpec provides the mechanism of converting byte data into a 
             * secret key suitable to be passed to init() method of the Cipher class. */
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            
            /* Now we generate an initialization vector (IV) */
            /* To produce different ciphertext with each run of the encryption, we use a random initialization vector. */
            byte[] IV = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(IV);
            
            IvParameterSpec ivspec = new IvParameterSpec(key);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] cipherText = decoder.decode(ciphertext.getBytes("UTF8"));
            decryptedText = new String(cipher.doFinal(cipherText), "UTF-8");
        } catch(Exception e) {}
        return decryptedText;
    }        
}