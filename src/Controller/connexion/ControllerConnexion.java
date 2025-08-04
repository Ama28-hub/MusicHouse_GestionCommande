package Controller.connexion;

import Modele.Data;
import Modele.Privileges;
import Modele.Users;
import util.Chiffrement; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public abstract class ControllerConnexion {
    
    public static Users controle(String email, String motDePasseSaisi) throws SecurityException {
        String query = "SELECT id_user, nom, prenom, email, mot_de_passe, role FROM Users WHERE email = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {

            preparedStatement.setString(1, email);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    String motDePasseChiffre = rs.getString("mot_de_passe"); 

                    if (motDePasseChiffre == null || motDePasseChiffre.trim().isEmpty()) {
                        throw new SecurityException("⚠️ Mot de passe vide ou null en base !");
                    }

                    // Vérification et déchiffrement si nécessaire
                    String motDePasseEnClair = motDePasseChiffre;
                    if (motDePasseChiffre.startsWith("ENC(") && motDePasseChiffre.endsWith(")")) {
                        try {
                            motDePasseEnClair = Chiffrement.dechiffrer(motDePasseChiffre.substring(4, motDePasseChiffre.length() - 1));
                        } catch (Exception e) {
                            throw new SecurityException("❌ Erreur lors du déchiffrement du mot de passe !");
                        }
                    }

                    // Vérification du mot de passe saisi
                    if (!motDePasseSaisi.equals(motDePasseEnClair)) {
                        throw new SecurityException("Mot de passe incorrect !");
                    }

                    Users user = new Users(
                        rs.getInt("id_user"), 
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        motDePasseEnClair   // Stocker le mot de passe déchiffré
                        );

                    List<Privileges> privileges = loadPrivilegesForUser(conn, user.getIdUser());
                    user.setPrivileges(privileges);

                    System.out.println("✅ Connecté en tant que : " + user.getRole());
                    return user;
                } else {
                    throw new SecurityException("❌ Aucun utilisateur trouvé pour cet email !");
                }
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Erreur lors de la vérification : " + e.getMessage());
            return null; 
        }
    }


    private static List<Privileges> loadPrivilegesForUser(Connection conn, int userId) {
        List<Privileges> privileges = new ArrayList<>();
        String sql = "SELECT p.id_privilege, p.description, p.niveau_acces "
                   + "FROM Privileges p "
                   + "JOIN Beneficie b ON p.id_privilege = b.id_privilege "
                   + "WHERE b.id_user = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Privileges p = new Privileges(
                        rs.getInt("id_privilege"),
                        rs.getString("description"),
                        rs.getString("niveau_acces")
                    );
                    privileges.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du chargement des privilèges : " + e.getMessage());
        }
        return privileges;
    }

    public static void fermetureSession() throws ClassNotFoundException {
        try (Connection conn = Data.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connexion fermée avec succès.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                "Problème rencontré à la fermeture de la connexion : " + ex.getMessage(),
                "ERREUR",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
