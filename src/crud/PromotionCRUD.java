package crud;

import Modele.Data;
import Modele.Promotion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.sql.Statement;


public class PromotionCRUD {

    /**
     * Ajoute une promotion pour un article spécifique
     */
    public void ajouterPromotion(int idStock, String description, double tauxReduction, LocalDate dateDebut, LocalDate dateFin) throws SQLException, ClassNotFoundException {
        Connection conn = Data.getConnection();
        try {
            // Étape 1️⃣ : Insérer la promotion dans Promotions
            String sql1 = "INSERT INTO Promotions (description_promotion, taux_reduction, date_debut, date_fin) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt1 = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
            pstmt1.setString(1, description);
            pstmt1.setDouble(2, tauxReduction);
            pstmt1.setDate(3, java.sql.Date.valueOf(dateDebut));
            pstmt1.setDate(4, java.sql.Date.valueOf(dateFin));
            pstmt1.executeUpdate();

            // Récupérer l'ID de la promotion insérée
            ResultSet rs = pstmt1.getGeneratedKeys();
            int promoId = -1;
            if (rs.next()) {
                promoId = rs.getInt(1);
            }

            // Étape 2️⃣ : Associer la promotion à l'article dans Applique
            if (promoId != -1) {
                String sql2 = "INSERT INTO Applique (id_stock, id_promotion) VALUES (?, ?)";
                PreparedStatement pstmt2 = conn.prepareStatement(sql2);
                pstmt2.setInt(1, idStock);
                pstmt2.setInt(2, promoId);
                pstmt2.executeUpdate();
            } else {
                System.out.println("❌ Erreur : ID de promotion non récupéré !");
            }

            System.out.println("✅ Nouvelle promotion enregistrée !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout de la promotion : " + e.getMessage());
        } finally {
            conn.close();
        }
    }


    /**
     * Récupère la promotion active pour un article donné
     */
    public Promotion getPromotionActive(int idStock) throws SQLException, ClassNotFoundException {
        String sql = "SELECT p.id_promotion, p.taux_reduction " +
                     "FROM promotions p " + // ⚠ Vérifie bien le nom exact de ta table en BD
                     "JOIN applique a ON p.id_promotion = a.id_promotion " +
                     "WHERE a.id_stock = ? AND p.date_fin >= CURDATE() " +
                     "ORDER BY p.date_fin DESC LIMIT 1"; // 🔹 Récupère uniquement la promo la plus récente

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idStock);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double tauxReduction = rs.getDouble("taux_reduction");
                    if (tauxReduction > 0) { // 🔹 Vérifie que la réduction est valide
                        return new Promotion(rs.getInt("id_promotion"), tauxReduction);
                    }
                }
            }
        }
        return null; // 🔹 Retourne null si aucune promo n'est trouvée
    }

    /**
     * Supprime une promotion expirée
     */
    public void supprimerPromotionsExpirees() throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM promotions WHERE date_fin < CURDATE()";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("✅ Suppression des promotions expirées (" + rowsDeleted + " supprimées)");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("❌ Erreur lors de la suppression des promotions expirées : " + ex.getMessage());
        }
    }
    
    public boolean promotionDejaExistante(int idStock) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM applique WHERE id_stock = ?";
        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idStock);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true; // ✅ Promotion déjà existante
                }
            }
        }
        return false; // ❌ Pas de promotion existante
    }

    
    public double getPrixReduit(int idStock) {
        double prixInitial = -1;
        double prixReduit = -1;
        double tauxReduction = 0;

        String sql = "SELECT s.prix_initial, p.taux_reduction " +
                     "FROM stock s " +
                     "LEFT JOIN applique a ON s.id_stock = a.id_stock " +
                     "LEFT JOIN promotions p ON a.id_promotion = p.id_promotion " +
                     "WHERE s.id_stock = ? AND (p.date_fin IS NULL OR p.date_fin >= CURRENT_DATE())";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idStock);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    prixInitial = rs.getDouble("prix_initial"); // ✅ Récupère le vrai prix stocké en BD
                    tauxReduction = rs.getDouble("taux_reduction");

                    if (tauxReduction > 0) {
                        prixReduit = prixInitial - ((tauxReduction / 100) * prixInitial); // ✅ Appliquer la réduction correctement
                    } else {
                        prixReduit = prixInitial; // ✅ Aucun changement si pas de réduction
                    }

                    System.out.println("🔍 DEBUG: Prix initial récupéré de la BD = " + prixInitial + 
                                       ", Réduction appliquée = " + tauxReduction + "%, Prix final calculé = " + prixReduit);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return prixReduit; // ✅ Retourne toujours le bon prix, qu'il y ait promo ou non
    }

}
