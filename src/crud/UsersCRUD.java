package crud;

import Modele.Users;
import Modele.Data;
import Modele.Privileges;
import util.Chiffrement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UsersCRUD {

    /**
     * Récupère un utilisateur par email, y compris ses privilèges
     */
    public Users getUserByEmail(String email) throws SQLException, ClassNotFoundException, Exception {
        Users user = null;
        String sql = "SELECT id_user, nom, prenom, email, mot_de_passe, role FROM Users WHERE email=?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("mot_de_passe");
                    String decryptedPassword = storedPassword;
                    
                    // 🔓 Déchiffrer le mot de passe avec AES uniquement
                    if (storedPassword.startsWith("ENC(") && storedPassword.endsWith(")")) {
                        decryptedPassword = Chiffrement.dechiffrer(storedPassword.substring(4, storedPassword.length() - 1));
                        //System.out.println("🔓 Mot de passe déchiffré avec AES : " + decryptedPassword);
                    }

                    user = new Users(
                        rs.getInt("id_user"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        decryptedPassword
                    );

                    String role = rs.getString("role");
                    if (role == null || role.trim().isEmpty()) {
                        role = "user"; // Valeur par défaut
                    }
                    user.setRole(role);
                   // System.out.println("🔍 Rôle récupéré depuis la base : " + user.getRole());
                }
            }
        }
        return user;
    }

    /**
     * Insère un utilisateur et un employé avec chiffrement AES
     */
    public boolean insertUserAndEmploye(Users user, String titre, String nomEntreprise, java.util.Date dateEmbauche, List<Integer> privilegeIds) 
        throws SQLException, ClassNotFoundException, Exception {

        String sqlUser = "INSERT INTO Users (nom, prenom, email, mot_de_passe, role) VALUES (?, ?, ?, ?, ?)";
        String sqlEmploye = "INSERT INTO Employe (nom, prenom, titre, date_embauche, nom_entreprise, id_user) VALUES (?, ?, ?, ?, ?, ?)";

        boolean success = false;
        Connection conn = null;

        try {
            conn = Data.getConnection();
            conn.setAutoCommit(false);

            int userId = -1;

            try (PreparedStatement psUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, user.getNom());
                psUser.setString(2, user.getPrenom());
                psUser.setString(3, user.getEmail());

                // ✅ Ne stocke que AES, pas de bcrypt
                String motDePasse = user.getMotDePasse();
                if (!motDePasse.startsWith("ENC(")) { 
                    motDePasse = Chiffrement.chiffrer(motDePasse);
                }
                psUser.setString(4, motDePasse);

                String role = user.getRole();
                if (role == null || role.trim().isEmpty()) {
                    role = "user"; 
                }
                psUser.setString(5, role);

                int rowsAffected = psUser.executeUpdate();
                if (rowsAffected > 0) {
                    try (ResultSet rs = psUser.getGeneratedKeys()) {
                        if (rs.next()) {
                            userId = rs.getInt(1);
                        }
                    }
                }
            }

            if (userId > 0) {
                try (PreparedStatement psEmploye = conn.prepareStatement(sqlEmploye)) {
                    psEmploye.setString(1, user.getNom());
                    psEmploye.setString(2, user.getPrenom());
                    psEmploye.setString(3, titre);
                    psEmploye.setDate(4, new java.sql.Date(dateEmbauche.getTime()));
                    psEmploye.setString(5, nomEntreprise);
                    psEmploye.setInt(6, userId);
                    psEmploye.executeUpdate();
                }
                assignUserPrivileges(conn, userId, privilegeIds);
                success = true;
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return success;
    }

    /**
     * Met à jour le mot de passe d'un utilisateur (chiffrement systématique)
     */
    public boolean updateUserPassword(String email, String nouveauMotDePasse) throws SQLException, ClassNotFoundException, Exception {
        String sqlUpdate = "UPDATE Users SET mot_de_passe = ? WHERE email = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {

            // On chiffre systématiquement le nouveau mot de passe
            String motDePasseChiffre = Chiffrement.chiffrer(nouveauMotDePasse);

            ps.setString(1, motDePasseChiffre);
            ps.setString(2, email);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Mise à jour d'un utilisateur + employé existant (mot de passe chiffré)
     */
    public boolean updateUserAndEmploye(Users user, String titre, String nomEntreprise,
                                        java.util.Date dateEmbauche, List<Integer> privilegeIds)
        throws SQLException, ClassNotFoundException, Exception {

        String sqlUser = "UPDATE Users SET nom=?, prenom=?, email=?, mot_de_passe=?, role=? WHERE id_user=?";
        String sqlEmploye = "UPDATE Employe SET titre=?, date_embauche=?, nom_entreprise=? WHERE id_user=?";

        boolean success = false;
        Connection conn = null;

        try {
            conn = Data.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                psUser.setString(1, user.getNom());
                psUser.setString(2, user.getPrenom());
                psUser.setString(3, user.getEmail());

                // Toujours vérifier et chiffrer
                String motDePasse = user.getMotDePasse();
                if (!motDePasse.startsWith("ENC(")) {
                    motDePasse = Chiffrement.chiffrer(motDePasse);
                }
                psUser.setString(4, motDePasse);

                psUser.setString(5, user.getRole());
                psUser.setInt(6, user.getIdUser());
                psUser.executeUpdate();
            }

            try (PreparedStatement psEmploye = conn.prepareStatement(sqlEmploye)) {
                psEmploye.setString(1, titre);
                psEmploye.setDate(2, new java.sql.Date(dateEmbauche.getTime()));
                psEmploye.setString(3, nomEntreprise);
                psEmploye.setInt(4, user.getIdUser());
                psEmploye.executeUpdate();
            }

            removeUserPrivileges(conn, user.getIdUser());
            assignUserPrivileges(conn, user.getIdUser(), privilegeIds);

            conn.commit();
            success = true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
        return success;
    }

    /**
     * "Anonymise" un utilisateur (supprime ses données identifiables)
     */
    public boolean anonymiserUtilisateur(int idUser) throws SQLException, ClassNotFoundException {
        String sqlAnonymizeUser = "UPDATE Users SET nom = '***', prenom = '***', email = CONCAT('deleted_', id_user, '@anonymized.com'), role = 'Inactif' WHERE id_user = ?";
        String sqlAnonymizeEmploye = "UPDATE Employe SET nom = '***', prenom = '***', titre = 'Supprimé', nom_entreprise = 'Supprimé' WHERE id_user = ?";

        boolean success = false;
        Connection conn = null;

        try {
            conn = Data.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psUser = conn.prepareStatement(sqlAnonymizeUser)) {
                psUser.setInt(1, idUser);
                psUser.executeUpdate();
            }

            try (PreparedStatement psEmploye = conn.prepareStatement(sqlAnonymizeEmploye)) {
                psEmploye.setInt(1, idUser);
                psEmploye.executeUpdate();
            }

            removeUserPrivileges(conn, idUser);

            conn.commit();
            success = true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
        return success;
    }

    /**
     * Supprime tous les privilèges d'un utilisateur (table Beneficie).
     */
    private void removeUserPrivileges(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM Beneficie WHERE id_user=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Charge la liste des privilèges (table Privileges) d'un user depuis la table de liaison Beneficie
     */
    private List<Privileges> loadPrivilegesForUser(int userId) throws SQLException, ClassNotFoundException {
        List<Privileges> privileges = new ArrayList<>();
        String sql = "SELECT p.id_privilege, p.description, p.niveau_acces "
                   + "FROM Privileges p JOIN Beneficie b ON p.id_privilege = b.id_privilege "
                   + "WHERE b.id_user = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    privileges.add(new Privileges(
                        rs.getInt("id_privilege"),
                        rs.getString("description"),
                        rs.getString("niveau_acces")
                    ));
                }
            }
        }
        return privileges;
    }

    /**
     * Associe des privilèges à l'utilisateur (insère dans Beneficie).
     */
    private void assignUserPrivileges(Connection conn, int userId, List<Integer> privilegeIds) throws SQLException {
        String sql = "INSERT INTO Beneficie (id_user, id_privilege) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer privilegeId : privilegeIds) {
                ps.setInt(1, userId);
                ps.setInt(2, privilegeId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Users> getAllUsersWithEmployes() {
        List<Users> listeUsers = new ArrayList<>();
        try {
            Connection conn = Data.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users"); // Vérifie si cette requête fonctionne bien !
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                Users user = new Users(
                    rs.getInt("id_user"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("role")
                );
                listeUsers.add(user);
                //System.out.println("📌 Utilisateur chargé : " + user.getNom() + " - " + user.getEmail());
            }

            //System.out.println("📌 Nombre total d'utilisateurs récupérés : " + count);

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("❌ Erreur récupération utilisateurs : " + e.getMessage());
            e.printStackTrace();
        }
        return listeUsers;
    }

    /**
     * Récupère un utilisateur par son ID (sans déchiffrer le mot de passe)
     */
    public Users getUserById(int idUser) throws SQLException, ClassNotFoundException {
        Users user = null;
        String sql = "SELECT id_user, nom, prenom, email, role FROM Users WHERE id_user = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new Users(
                        rs.getInt("id_user"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        "" // ici on ne récupère pas le mot de passe
                        );
                }
            }
        }
        return user;
    }
    
    public List<Users> getAllUsers() throws SQLException, ClassNotFoundException {
        List<Users> liste = new ArrayList<>();
        String sql = "SELECT id_user, nom, prenom, email, mot_de_passe, role FROM Users";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                liste.add(new Users(
                    rs.getInt("id_user"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("mot_de_passe")  // 📌 Le mot de passe est récupéré en base
                    ));
            }
        }
        return liste;
    }

    
    public void updateUnencryptedPasswords() throws Exception {
        Connection conn = Data.getConnection();
        PreparedStatement ps = conn.prepareStatement("UPDATE Users SET mot_de_passe = ? WHERE id_user = ?");

        UsersCRUD usersCRUD = new UsersCRUD();  // ✅ Création d'une instance de UsersCRUD
        List<Users> users = usersCRUD.getAllUsers(); // ✅ Récupérer la liste des utilisateurs

        for (Users user : users) {
            String motDePasse = user.getMotDePasse();
            if (motDePasse != null && !motDePasse.startsWith("ENC(")) { // Vérifier si ce n'est pas déjà chiffré
                String motDePasseChiffre = Chiffrement.chiffrer(motDePasse);
                ps.setString(1, motDePasseChiffre);
                ps.setInt(2, user.getIdUser());
                ps.executeUpdate();
                //System.out.println("✅ Mot de passe mis à jour pour : " + user.getEmail());
            }
        }
        ps.close();
        conn.close();
    }
    
    public void corrigerMotsDePasseAES() throws Exception {
        Connection conn = Data.getConnection();
        PreparedStatement psUpdate = conn.prepareStatement("UPDATE Users SET mot_de_passe = ? WHERE id_user = ?");

        String sql = "SELECT id_user, mot_de_passe FROM Users";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idUser = rs.getInt("id_user");
                String motDePasseChiffre = rs.getString("mot_de_passe");

                if (motDePasseChiffre.startsWith("ENC(") && motDePasseChiffre.endsWith(")")) {
                    String motDePasseDechiffre = Chiffrement.dechiffrer(motDePasseChiffre.substring(4, motDePasseChiffre.length() - 1));

                    // 🚨 Si le mot de passe déchiffré est encore en bcrypt, on ne peut pas le récupérer
                    if (motDePasseDechiffre.startsWith("$2a$10$")) {
                        //System.err.println("❌ Mot de passe bcrypt détecté pour l'utilisateur " + idUser + ". Veuillez réinitialiser manuellement.");
                        continue; // Impossible de récupérer un bcrypt
                    }

                    // ✅ Rechiffrement propre avec AES uniquement
                    String motDePasseRechiffre = Chiffrement.chiffrer(motDePasseDechiffre);
                    psUpdate.setString(1, motDePasseRechiffre);
                    psUpdate.setInt(2, idUser);
                    psUpdate.executeUpdate();
                    //System.out.println("✅ Mot de passe mis à jour pour utilisateur " + idUser);
                }
            }
        }
        psUpdate.close();
        conn.close();
    }


}
