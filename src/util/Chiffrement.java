/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Carmelle Adou
 */

public class Chiffrement {

    private static final String KEY_FILE = "src/config/key.dat";
    private static SecretKey secretKey;

    // Chargement ou génération de la clé AES
    private static void generateKey() throws NoSuchAlgorithmException, IOException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        secretKey = keyGen.generateKey();
        saveKey(secretKey);
    }

    private static void saveKey(SecretKey key) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(KEY_FILE)) {
            byte[] encoded = Base64.getEncoder().encode(key.getEncoded());
            fos.write(encoded);
        }
    }

    private static SecretKey loadKey() throws IOException {
        File file = new File(KEY_FILE);
        if (!file.exists()) {
            try {
                generateKey();
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Chiffrement.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        byte[] fileContent = new FileInputStream(file).readAllBytes();
        byte[] decodedKey = Base64.getDecoder().decode(fileContent);
        return new SecretKeySpec(decodedKey, "AES");
    }

    public static String chiffrer(String texteClair) throws Exception {
        if (texteClair.startsWith("ENC(") && texteClair.endsWith(")")) {
            return texteClair; // Évite le double chiffrement
        }

        if (secretKey == null) secretKey = loadKey();

        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(ivBytes);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encrypted = cipher.doFinal(texteClair.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[ivBytes.length + encrypted.length];
        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
        System.arraycopy(encrypted, 0, combined, ivBytes.length, encrypted.length);

        return "ENC(" + Base64.getEncoder().encodeToString(combined) + ")";
    }


    public static String dechiffrer(String texteChiffre) throws Exception {
        if (secretKey == null) secretKey = loadKey();

        if (texteChiffre == null || texteChiffre.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Erreur : Le texte chiffré est vide !");
        }

        //System.out.println("🔍 Texte chiffré avant déchiffrement : " + texteChiffre);

        // Suppression de ENC(...) si présent
        if (texteChiffre.startsWith("ENC(") && texteChiffre.endsWith(")")) {
            texteChiffre = texteChiffre.substring(4, texteChiffre.length() - 1);
        }

        byte[] combined = Base64.getDecoder().decode(texteChiffre);

        if (combined.length < 32) {
            throw new IllegalArgumentException("❌ Erreur : Données chiffrées mal formées !");
        }

        return dechiffrementAES(combined);
    }

    private static String dechiffrementAES(byte[] combined) throws Exception {
        byte[] ivBytes = new byte[16];
        byte[] ciphertext = new byte[combined.length - 16];

        System.arraycopy(combined, 0, ivBytes, 0, 16);
        System.arraycopy(combined, 16, ciphertext, 0, ciphertext.length);

        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] decrypted = cipher.doFinal(ciphertext);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static void verifierCleAES() throws IOException {
        SecretKey key = loadKey();
        //System.out.println("🔑 Clé AES chargée : " + Base64.getEncoder().encodeToString(key.getEncoded()));
    }
}
