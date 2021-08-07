package com.felipeyan.minima;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    String AES = "AES";

    protected String encrypt(String data, String password) throws Exception {
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE, generateKey(password));
        return Base64.encodeToString(c.doFinal(data.getBytes()), Base64.DEFAULT);
    }

    protected String decrypt(String encrypted, String password) throws Exception {
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.DECRYPT_MODE, generateKey(password));
        return new String(c.doFinal(Base64.decode(encrypted, Base64.DEFAULT)));
    }

    protected SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes, 0, bytes.length);
        return new SecretKeySpec(digest.digest(), "AES");
    }

    protected String decryptNote(Context context, String note) {
        try { // Decrypts the note using the stored password
            note = decrypt(note, new Preferences(context).getPassword());
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }

        return note; // Returns the decrypted note as a String
    }
}