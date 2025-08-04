package Modele;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import configUtil.ConfigManager;


public class Data {

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        System.out.println("⚡ Connexion à la base de données en cours...");

        // Récupération des propriétés
        String driver = ConfigManager.getProperty("db.driver");
        String url = ConfigManager.getProperty("db.url");
        String user = ConfigManager.getProperty("db.user");
        String password = ConfigManager.getProperty("db.password");

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("⚠️ Erreur : Le mot de passe de la base de données est vide ou mal déchiffré !");
        }

        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connexion réussie à la base de données !");
            return connection;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("❌ Erreur lors de la connexion : " + e.getMessage());
            throw e;
        }
    }

}
