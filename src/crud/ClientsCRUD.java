package crud;

import Modele.Adresse;
import Modele.Client;
import Modele.Data;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Carmelle Adou
 */
public class ClientsCRUD {

    /**
     * Ajoute une adresse et retourne son ID.
     */
    private int ajouterAdresse(Adresse adresse) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO Adresse (rue, ville, code_postal) VALUES (?, ?, ?)";
        
        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, adresse.getRue());
            ps.setString(2, adresse.getVille());
            ps.setString(3, adresse.getCodePostal());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Ajoute un client avec son adresse.
     */
    public boolean ajouterClient(Client client) throws SQLException, ClassNotFoundException {
        int idAdresse = ajouterAdresse(client.getAdresse());
        if (idAdresse == -1) return false; // L'adresse doit être créée avant le client

        String sql = "INSERT INTO Client (nom, prenom, email, telephone, id_adresse, date_naissance) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, client.getNom());
            ps.setString(2, client.getPrenom());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getTelephone());
            ps.setInt(5, idAdresse);

            // Vérifier si la date est null avant d'insérer
            if (client.getDateNaissance() != null) {
                ps.setDate(6, new java.sql.Date(client.getDateNaissance().getTime()));
            } else {
                ps.setNull(6, Types.DATE); // Si la date est absente, insère NULL
            }

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Récupère tous les clients avec leur adresse.
     */
    public List<Client> getAllClients() throws SQLException, ClassNotFoundException {
        List<Client> listeClients = new ArrayList<>();
        String sql = "SELECT c.id_client, c.nom, c.prenom, c.email, c.telephone, a.id_adresse, a.rue, a.ville, a.code_postal, c.date_naissance "
                   + "FROM Client c JOIN Adresse a ON c.id_adresse = a.id_adresse "
                   + "WHERE c.email NOT LIKE 'deleted_%'";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Adresse adresse = new Adresse(
                    rs.getInt("id_adresse"),
                    rs.getString("rue"),
                    rs.getString("ville"),
                    rs.getString("code_postal")
                );

                // ✅ Vérifie si la date est NULL avant de la récupérer
                java.sql.Date sqlDate = rs.getDate("date_naissance");
                java.util.Date dateNaissance = (sqlDate != null) ? new java.util.Date(sqlDate.getTime()) : null;

                Client client = new Client(
                    rs.getInt("id_client"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    adresse, dateNaissance
                );
                
                client.setDateNaissance(dateNaissance); // Ajout de la date de naissance
                listeClients.add(client);
            }
        }
        return listeClients;
    }

    /**
     * Modifie un client et son adresse.
     */
    public boolean modifierClient(Client client) throws SQLException, ClassNotFoundException {
        // 🔹 Vérifier si l'adresse est valide, sinon la recréer
        if (client.getAdresse() == null || client.getAdresse().getIdAdresse() <= 0) {
            System.out.println("❌ Erreur : L'adresse du client est invalide, tentative d'ajout...");

            int idNouvelleAdresse = ajouterAdresse(client.getAdresse());
            if (idNouvelleAdresse <= 0) {
                System.out.println("❌ Échec de la création de l'adresse !");
                return false;
            }
            client.getAdresse().setIdAdresse(idNouvelleAdresse);
            System.out.println("🟢 Nouvelle adresse ajoutée avec l'ID : " + idNouvelleAdresse);
        }

        // 🔹 Mettre à jour l'adresse
        boolean adresseUpdated = modifierAdresse(client.getAdresse());

        String sql = "UPDATE Client SET nom=?, prenom=?, email=?, telephone=?, id_adresse=? WHERE id_client=?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, client.getNom());
            ps.setString(2, client.getPrenom());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getTelephone());
            ps.setInt(5, client.getAdresse().getIdAdresse());  // ✅ Utilisation de l'ID adresse mis à jour
            ps.setInt(6, client.getIdClient());

            return ps.executeUpdate() > 0 && adresseUpdated;
        }
    }

    /**
     * Met à jour une adresse existante.
     */
    private boolean modifierAdresse(Adresse adresse) throws SQLException, ClassNotFoundException {
        if (adresse == null || adresse.getIdAdresse() <= 0) {
            System.out.println("❌ Erreur : Adresse non valide !");
            return false;
        }

        String sql = "UPDATE Adresse SET rue=?, ville=?, code_postal=? WHERE id_adresse=?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, adresse.getRue());
            ps.setString(2, adresse.getVille());
            ps.setString(3, adresse.getCodePostal());
            ps.setInt(4, adresse.getIdAdresse());

            int rowsUpdated = ps.executeUpdate();
            System.out.println("✅ Adresse mise à jour ? " + (rowsUpdated > 0));
            System.out.println("ID Adresse : " + adresse.getIdAdresse());
            System.out.println("Rue : " + adresse.getRue());
            System.out.println("Code Postal : " + adresse.getCodePostal());
            System.out.println("Ville : " + adresse.getVille());

            return rowsUpdated > 0;
        }
    }

    /**
     * Archive un client au lieu de le supprimer (RGPD).
     */
    public boolean archiverClient(int idClient) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Client SET nom = '***', prenom = '***', email = CONCAT('deleted_', id_client, '@anonymized.com') WHERE id_client=?";
        
        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idClient);
            return ps.executeUpdate() > 0;
        }
    }
    
    public Client getClientById(int idClient) throws SQLException, ClassNotFoundException {
        String sql = "SELECT c.id_client, c.nom, c.prenom, c.email, c.telephone, c.date_naissance, " +
                     "a.id_adresse, a.rue, a.ville, a.code_postal " +
                     "FROM Client c " +
                     "LEFT JOIN Adresse a ON c.id_adresse = a.id_adresse " +
                     "WHERE c.id_client = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idClient);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idAdresse = rs.getInt("id_adresse");
                    System.out.println("🟢 Débogage - ID Adresse trouvé : " + idAdresse);

                    Adresse adresse = null;

                    // ✅ Vérifier si l'ID adresse est valide
                    if (idAdresse > 0) {
                        adresse = new Adresse(
                            idAdresse,
                            rs.getString("rue"),
                            rs.getString("ville"),
                            rs.getString("code_postal")
                        );
                    } else {
                        System.out.println("❌ Problème : L'ID Adresse récupéré est 0 ou NULL !");
                    }

                    // ✅ Vérifier si la date est NULL avant conversion
                    java.sql.Date sqlDate = rs.getDate("date_naissance");
                    java.util.Date dateNaissance = (sqlDate != null) ? new java.util.Date(sqlDate.getTime()) : null;

                    return new Client(
                        rs.getInt("id_client"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("telephone"),
                        adresse,  // ✅ Utiliser l'adresse correcte
                        dateNaissance
                    );
                }
            }
        }
        return null; // Retourne NULL si le client n'existe pas
    }

}
