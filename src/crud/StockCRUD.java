/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crud;


import Modele.Data;
import Modele.Stocks;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author Carmelle Adou
 */
public class StockCRUD {

    public boolean ajouterStock(Stocks stock) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO Stock (nom_article, description_article, quantite_en_stock, seuil_min, prix_initial, etat) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, stock.getNomArticle());
            ps.setString(2, stock.getDescriptionArticle());
            ps.setInt(3, stock.getQuantiteEnStock());
            ps.setInt(4, stock.getSeuilMin());
            ps.setDouble(5, stock.getPrixInitial());
            ps.setString(6, "Actif");

            return ps.executeUpdate() > 0;
        }
    }

    public boolean modifierStock(Stocks stock) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Stock SET nom_article=?, description_article=?, quantite_en_stock=?, seuil_min=?, prix_initial=? WHERE id_stock=?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, stock.getNomArticle());
            ps.setString(2, stock.getDescriptionArticle());
            ps.setInt(3, stock.getQuantiteEnStock());
            ps.setInt(4, stock.getSeuilMin());
            ps.setDouble(5, stock.getPrixInitial());
            ps.setInt(6, stock.getIdStock());

            return ps.executeUpdate() > 0;
        }
    }
    
    public boolean desactiverStock(int idStock) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Stock SET etat = 'Désactivé' WHERE id_stock = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idStock);
            return ps.executeUpdate() > 0;
        }
    }
    
    public boolean reactiverStock(int idStock) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Stock SET etat = 'Actif' WHERE id_stock = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idStock);
            return ps.executeUpdate() > 0;
        }
    }
    
    public Stocks getStockById(int idStock) throws SQLException, ClassNotFoundException {
        Stocks stock = null;
        String sql = "SELECT id_stock, nom_article, description_article, quantite_en_stock, seuil_min, prix_initial, etat FROM Stock WHERE id_stock = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idStock);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stock = new Stocks(
                        rs.getInt("id_stock"),
                        rs.getString("nom_article"),
                        rs.getString("description_article"),
                        rs.getInt("quantite_en_stock"),
                        rs.getInt("seuil_min"),
                        rs.getDouble("prix_initial"),  // 🔹 Utilisation correcte du prix initial
                        rs.getString("etat")
                    );

                }
            }
        }
        return stock;
    }


    public List<Stocks> getAllStocks() throws SQLException, ClassNotFoundException {
        List<Stocks> listeStocks = new ArrayList<>();
        String sql = "SELECT * FROM Stock";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Stocks stock = new Stocks(
                    rs.getInt("id_stock"),
                rs.getString("nom_article"),
                rs.getString("description_article"),
                rs.getInt("quantite_en_stock"), 
                rs.getInt("seuil_min"),
                //rs.getDouble("prix_initial"),
                rs.getDouble("prix_initial"),
                rs.getString("etat"));
                listeStocks.add(stock);
            }
        }
        return listeStocks;
    }
    
    public List<Stocks> getDisabledStocks() throws SQLException, ClassNotFoundException {
        List<Stocks> listeStocks = new ArrayList<>();
        String sql = "SELECT * FROM Stock WHERE etat = 'Désactivé'";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Stocks stock = new Stocks(
                    rs.getInt("id_stock"),
                    rs.getString("nom_article"), rs.getString("description_article"),
                    rs.getInt("quantite_en_stock"), rs.getInt("seuil_min"),
                    rs.getDouble("prix_initial"), rs.getString("etat"));
                listeStocks.add(stock);
            }
        }
        return listeStocks;
    }
    
    

    public List<Stocks> getArticlesByCommande(int idCommande) throws SQLException, ClassNotFoundException {
        List<Stocks> articles = new ArrayList<>();
        String sql = "SELECT s.id_stock, s.nom_article, c.Qte, c.prix_initial " +
                     "FROM contient c " +
                     "JOIN Stock s ON c.id_stock = s.id_stock " +
                     "WHERE c.id_commande = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idCommande);
            System.out.println("🔍 Exécution de la requête : " + sql);
            System.out.println("🔹 Paramètre : id_commande = " + idCommande);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Stocks article = new Stocks(
                        rs.getInt("id_stock"),
                        rs.getString("nom_article"),
                        "", 
                        rs.getInt("Qte"),  
                        0,   
                        0,  
                        "Actif"
                    );
                    articles.add(article);
                    System.out.println("✅ Article récupéré : " + article.getNomArticle() + " - Quantité : " + article.getQuantiteEnStock());
                }
            }

            if (articles.isEmpty()) {
                System.out.println("⚠️ Aucun article trouvé pour la commande " + idCommande);
            }

        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL lors de la récupération des articles : " + e.getMessage());
        }

        System.out.println("🔍 Nombre total d'articles récupérés : " + articles.size());
        return articles;
    }
    
    public boolean stockExiste(int idStock) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM Stock WHERE id_stock = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idStock);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;  // Retourne vrai si l'article existe
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL lors de la vérification du stock : " + e.getMessage());
        }
        return false;  // Retourne faux si erreur ou stock inexistant
    }

    public double getPrixArticle(int idStock) throws ClassNotFoundException {
        String sql = "SELECT s.prix_initial, " +
                     "COALESCE(p.taux_reduction, 0) AS reduction " +
                     "FROM Stock s " +
                     "LEFT JOIN applique a ON s.id_stock = a.id_stock " +
                     "LEFT JOIN Promotions p ON a.id_promotion = p.id_promotion " +
                     "AND CURDATE() BETWEEN p.date_debut AND p.date_fin " +
                     "WHERE s.id_stock = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idStock);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double prixInitial = rs.getDouble("prix_initial");
                    double reduction = rs.getDouble("reduction");

                    double prixFinal = prixInitial * (1 - reduction / 100);
                    return Math.round(prixFinal * 100.0) / 100.0; // Arrondir à 2 décimales
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1; // Retourne -1 en cas d'erreur
    }

    
    
    public void verifierEtSupprimerPromotionsExpirees() throws ClassNotFoundException {
        String sql = "SELECT a.id_promotion, a.id_stock " +
                     "FROM applique a " +
                     "JOIN promotions p ON a.id_promotion = p.id_promotion " +
                     "WHERE p.date_fin < CURDATE()";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<Integer> promotionsASupprimer = new ArrayList<>();
            Map<Integer, Integer> stocksAReinitialiser = new HashMap<>();

            while (rs.next()) {
                int idPromotion = rs.getInt("id_promotion");
                int idStock = rs.getInt("id_stock");
                promotionsASupprimer.add(idPromotion);
                stocksAReinitialiser.put(idStock, idPromotion);
            }

            if (promotionsASupprimer.isEmpty()) {
                System.out.println("✅ Aucune promotion expirée à supprimer.");
                return;
            }

            // Début de la transaction
            conn.setAutoCommit(false);

            try {
                // Réinitialiser les prix des articles
                for (int idStock : stocksAReinitialiser.keySet()) {
                    remettrePrixInitial(idStock);
                    System.out.println("🔄 Prix réinitialisé pour l'article ID: " + idStock);
                }

                // Supprimer les promotions expirées
                for (int idPromotion : promotionsASupprimer) {
                    supprimerPromotion(idPromotion);
                    System.out.println("❌ Promotion ID " + idPromotion + " supprimée.");
                }

                // Valider la transaction
                conn.commit();
                System.out.println("✅ Suppression des promotions expirées effectuée avec succès.");

            } catch (SQLException e) {
                // Annuler la transaction en cas d'erreur
                conn.rollback();
                System.err.println("❌ Erreur lors de la suppression des promotions. Annulation de la transaction.");
                e.printStackTrace();
            } finally {
                // Rétablir le mode AutoCommit
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la vérification des promotions expirées : " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void remettrePrixInitial(int idStock) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Contient SET prix_initial = " +
                     "(SELECT prix_initial FROM Stock WHERE id_stock = ?) " +
                     "WHERE id_stock = ?";
        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idStock);
            pstmt.setInt(2, idStock);
            pstmt.executeUpdate();
            System.out.println("🔄 Prix réinitialisé pour l'article " + idStock);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void supprimerPromotion(int idPromotion) throws SQLException, ClassNotFoundException {
        String sqlDeleteApplique = "DELETE FROM applique WHERE id_promotion = ?";
        String sqlDeletePromotion = "DELETE FROM promotions WHERE id_promotion = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt1 = conn.prepareStatement(sqlDeleteApplique);
             PreparedStatement pstmt2 = conn.prepareStatement(sqlDeletePromotion)) {

            // Supprimer d'abord les références dans applique
            pstmt1.setInt(1, idPromotion);
            int rowsDeleted = pstmt1.executeUpdate();
            //System.out.println("✔ " + rowsDeleted + " référence(s) supprimée(s) dans `applique` pour id_promotion=" + idPromotion);

            // Puis supprimer la promotion elle-même
            pstmt2.setInt(1, idPromotion);
            int promoDeleted = pstmt2.executeUpdate();
            if (promoDeleted > 0) {
                System.out.println("✔ Promotion supprimée (ID " + idPromotion + ")");
            } else {
                System.out.println("⚠ Aucune promotion trouvée avec ID " + idPromotion);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean articleDejaDansCommande(int idCommande, int idStock) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM contient WHERE id_commande = ? AND id_stock = ?";
        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCommande);
            pstmt.setInt(2, idStock);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true; // ✅ L'article est déjà ajouté
                }
            }
        }
        return false; // ❌ L'article n'est pas encore ajouté
    }

    public boolean mettreAJourArticleDansCommande(int idCommande, int idStock, int quantite, double prix) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE contient SET Qte = Qte + ?, prix_initial = ? WHERE id_commande = ? AND id_stock = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantite);
            pstmt.setDouble(2, prix);
            pstmt.setInt(3, idCommande);
            pstmt.setInt(4, idStock);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Retourne true si mise à jour effectuée
        }
    }


    public double getPrixActuel(int idStock) throws SQLException, ClassNotFoundException {
        String sql = "SELECT prix_initial FROM Stock WHERE id_stock = ?"; 
        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idStock);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("prix_initial");
            }
        }
        return -1;
    }
    
    public double getPrixInitial(int idStock) throws SQLException, ClassNotFoundException {
        String sql = "SELECT prix_initial FROM Stock WHERE id_stock = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idStock);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("prix_initial");
            }
        }
        return -1; // Retourne -1 si aucun prix n'est trouvé (erreur)
    }

    public void mettreAJourPrixReduit(int idStock, double prixReduit) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Stock SET prix_reduit = ? WHERE id_stock = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, prixReduit);
            pstmt.setInt(2, idStock);
            pstmt.executeUpdate();
        }
    }

}
