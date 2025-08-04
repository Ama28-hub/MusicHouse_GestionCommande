package configUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.Properties;
import util.Chiffrement;

/**
 * 
 * @author Carmelle Adou
 */

public class ConfigManager {
    
    private static final String CONFIG_FILE = "src/config/config.properties";
    private static final Properties properties = new Properties();

    // 🔹 Chargement automatique du fichier de configuration au démarrage
    static {
        loadConfig();
    }

    /**
     * Charge la configuration depuis le fichier
     */
    private static void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("❌ Erreur lors du chargement du fichier de configuration : " + e.getMessage());
            }
        }
    }

    /**
     * Sauvegarde la configuration dans le fichier
     */
    private static void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Configuration de la base de données (chiffrée)");
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la sauvegarde du fichier de configuration : " + e.getMessage());
        }
    }

    /**
     * Récupère une propriété et la déchiffre si nécessaire
     */
    public static String getProperty(String key) {
        String valeur = properties.getProperty(key);
        try {
            if (valeur != null && valeur.startsWith("ENC(") && valeur.endsWith(")")) {
                return Chiffrement.dechiffrer(valeur.substring(4, valeur.length() - 1));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du déchiffrement de la clé '" + key + "' : " + e.getMessage());
        }
        return valeur; // Retourne la valeur en clair si non chiffrée
    }

    /**
     * Définit une propriété en la chiffrant si nécessaire
     */
    public static void setProperty(String key, String value) {
        try {
            // 🔒 On chiffre toutes les propriétés sensibles (password, url, user, etc.)
            if (key.startsWith("db.")) {
                value = "ENC(" + Chiffrement.chiffrer(value) + ")";
            }
            
            properties.setProperty(key, value);
            saveConfig();

            System.out.println("🔐 Propriété '" + key + "' enregistrée avec succès !");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chiffrement/enregistrement de la clé '" + key + "' : " + e.getMessage());
        }
    }
}
