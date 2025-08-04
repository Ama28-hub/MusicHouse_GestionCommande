package crud;

import Modele.Commande;
import Modele.Data;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeCRUD {
    // Ajouter une commande
    public boolean ajouterCommande(Commande commande) throws ClassNotFoundException {
        String sql = "INSERT INTO Commande (id_client, id_emp, date_commande, type_paiement, etat, total) VALUES (?, ?, NOW(), ?, ?, ?)";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, commande.getIdClient());
            ps.setInt(2, commande.getIdEmploye()); // Peut être modifié si l'employé n'est pas nécessaire
            ps.setString(3, commande.getTypePaiement());
            ps.setString(4, commande.getEtat());
            ps.setDouble(5, commande.getTotal());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la commande : " + e.getMessage());
            return false;
        }
    }

    // Récupérer toutes les commandes
    public List<Commande> getAllCommandes() throws ClassNotFoundException {
        List<Commande> listeCommandes = new ArrayList<>();
        String sql = "SELECT * FROM Commande";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Commande commande = new Commande(
                    rs.getInt("id_commande"),
                    rs.getInt("id_client"),
                    rs.getInt("id_emp"),
                    rs.getDate("date_commande"),
                    rs.getString("type_paiement"),
                    rs.getString("etat"),
                    rs.getDouble("total")
                );
                listeCommandes.add(commande);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes : " + e.getMessage());
        }
        return listeCommandes;
    }

    // Récupérer une commande par son ID
    public Commande getCommandeById(int idCommande) throws ClassNotFoundException {
        String sql = "SELECT * FROM Commande WHERE id_commande = ?";
        Commande commande = null;

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idCommande);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    commande = new Commande(
                        rs.getInt("id_commande"),
                        rs.getInt("id_client"),
                        rs.getInt("id_emp"),
                        rs.getDate("date_commande"),
                        rs.getString("type_paiement"),
                        rs.getString("etat"),
                        rs.getDouble("total")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la commande : " + e.getMessage());
        }
        return commande;
    }

    // Modifier une commande
    public boolean modifierCommande(Commande commande) throws ClassNotFoundException {
        String sql = "UPDATE Commande SET id_client=?, id_emp=?, type_paiement=?, etat=?, total=? WHERE id_commande=?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, commande.getIdClient());
            ps.setInt(2, commande.getIdEmploye());
            ps.setString(3, commande.getTypePaiement());
            ps.setString(4, commande.getEtat());
            ps.setDouble(5, commande.getTotal());
            ps.setInt(6, commande.getIdCommande());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de la commande : " + e.getMessage());
            return false;
        }
    }

    // Supprimer une commande
    public boolean supprimerCommande(int idCommande) throws ClassNotFoundException {
        String sql = "DELETE FROM Commande WHERE id_commande = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idCommande);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la commande : " + e.getMessage());
            return false;
        }
    }

    // Finaliser une commande (changer son état en "Confirmée")
    public boolean finaliserCommande(int idCommande) throws ClassNotFoundException {
        String sql = "UPDATE Commande SET etat = 'Confirmée' WHERE id_commande = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idCommande);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la finalisation de la commande : " + e.getMessage());
            return false;
        }
    }
    
    public int getLastInsertedCommandeId() throws ClassNotFoundException {
        String sql = "SELECT MAX(id_commande) FROM Commande";
        try (Connection conn = Data.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du dernier ID de commande : " + e.getMessage());
        }
        return -1;
    }

    public double calculerMontantTotalCommande(int idCommande) throws SQLException, ClassNotFoundException {
        String sql = "SELECT SUM(c.Qte * (s.prix_initial * (1 - COALESCE(p.taux_reduction, 0) / 100))) AS total " +
                     "FROM Contient c " +
                     "JOIN Stock s ON c.id_stock = s.id_stock " +
                     "LEFT JOIN applique a ON s.id_stock = a.id_stock " +
                     "LEFT JOIN Promotions p ON a.id_promotion = p.id_promotion " +
                     "AND CURDATE() BETWEEN p.date_debut AND p.date_fin " +
                     "WHERE c.id_commande = ?";

        double total = 0;
        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCommande);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total");
                }
            }
        }
        return total;
    }


}
