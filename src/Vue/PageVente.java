/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vue;

import Modele.Adresse;
import Modele.Client;
import crud.CommandeCRUD;
import crud.StockCRUD;
import Modele.Commande;
import Modele.Data;
import Modele.Promotion;
import Modele.Stocks;
import Modele.Users;
import com.itextpdf.text.DocumentException;
import crud.ClientsCRUD;
import java.io.IOException;
import utils.Facture;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Statement;
import crud.PromotionCRUD;
/**
 *
 * @author Carmelle Adou
 */
public class PageVente extends javax.swing.JFrame {
    private CommandeCRUD commandeCRUD = new CommandeCRUD();
    private StockCRUD stockCRUD = new StockCRUD();
    private int idClientSelectionne = -1; 
    private int idCommandeEnCours = -1;
    private PromotionCRUD promotionCRUD = new PromotionCRUD();
    private double prixInitial;

    


    /**
     * Creates new form PageVente
     */
    public PageVente() throws ClassNotFoundException {
        initComponents();
        // ⛔ Supprime les lignes vides si elles existent
        DefaultTableModel model = (DefaultTableModel) jTable_Panier.getModel();
        model.setRowCount(0);
        try {
            stockCRUD = new StockCRUD();
            chargerArticlesStock(); // Charge d'abord les articles
            stockCRUD.verifierEtSupprimerPromotionsExpirees(); // Puis supprime les promotions expirées
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des promotions.");
        }
    }
    
   public void setClientSelectionne(int idClient, String nom, String prenom, String rue, String ville, String codePostal) {
        this.idClientSelectionne = idClient;
        jTextF_Nom.setText(nom);
        jTextF_Prenom.setText(prenom);
        jTextF_Rue.setText(rue);
        jTextF_Ville.setText(ville);
        jTextF_CodePostal.setText(codePostal);
    }
    
    private void chargerArticlesStock() {
        jComboBox_Stocks.removeAllItems(); // Vider la comboBox avant de recharger
        try {
            StockCRUD stockCrud = new StockCRUD();
            List<Stocks> stocks = stockCrud.getAllStocks();

            for (Stocks stock : stocks) {
                // Ajout dans la comboBox sous la forme "ID - Nom Article"
                jComboBox_Stocks.addItem(stock.getIdStock() + " - " + stock.getNomArticle());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement des stocks: " + e.getMessage());
        }
    }
    
   public void chargerCommandeExistante(int idCommande, int idClient) {
        this.idCommandeEnCours = idCommande;
        this.idClientSelectionne = idClient;

        // ✅ Créer une instance de ClientsCRUD
        ClientsCRUD clientsCRUD = new ClientsCRUD();

        // ✅ Charger les infos du client
        try {
            Client c = clientsCRUD.getClientById(idClient); // Utilisation correcte
            if (c != null) {
                afficherInfosClient(c); // Afficher Nom, Prénom, Rue, Ville, Code Postal
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des informations du client.");
        }

        // ✅ Charger les articles associés à la commande
        chargerArticlesDansPanier(idCommande);
    }

    
   public void afficherInfosClient(Client c) {
        try {
            // ✅ Affichage des informations de base du client
            jTextF_Nom.setText(c.getNom());
            jTextF_Prenom.setText(c.getPrenom());

            // ✅ Vérification de l'adresse du client
            if (c.getAdresse() != null) {
                Adresse adr = c.getAdresse();
                jTextF_Rue.setText(adr.getRue());
                jTextF_CodePostal.setText(adr.getCodePostal());
                jTextF_Ville.setText(adr.getVille());
            } else {
                // ✅ Si aucune adresse n'est enregistrée, afficher "Non renseigné"
                jTextF_Rue.setText("Non renseigné");
                jTextF_CodePostal.setText("");
                jTextF_Ville.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'affichage des informations du client.");
        }
    }

    private void chargerArticlesDansPanier(int idCommande) {
        DefaultTableModel model = (DefaultTableModel) jTable_Panier.getModel();
        model.setRowCount(0); // Vider avant de remplir

         String sql = "SELECT c.id_stock, s.nom_article, c.Qte, c.prix_initial " 
               + "FROM Contient c " 
               + "JOIN Stock s ON c.id_stock = s.id_stock " 
               + "WHERE c.id_commande = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCommande);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    int idStock = rs.getInt("id_stock");
                    String designation = rs.getString("nom_article");
                    int qte = rs.getInt("Qte");
                    double prixU = rs.getDouble("prix_initial");
                    double total = qte * prixU;

                    model.addRow(new Object[]{
                        idStock, designation, qte, prixU, total
                    });
                }

                // Afficher un message si la commande est vide
                if (!hasData) {
                    JOptionPane.showMessageDialog(this, "Aucun article trouvé pour cette commande.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des articles.");
        }
    }
    
    private void ajouterArticleDansCommande(int idCommande, int idStock, int quantite, double prix) throws ClassNotFoundException, SQLException {
        StockCRUD stockCRUD = new StockCRUD();
        PromotionCRUD promotionCRUD = new PromotionCRUD(); // ✅ Ajout de l'instance de PromotionCRUD
        double prixInitial = stockCRUD.getPrixArticle(idStock); // Récupération du prix initial

        if (prixInitial < 0) {
            JOptionPane.showMessageDialog(this, "Erreur : Impossible de récupérer le prix de l'article.");
            return;
        }

        // Demander si une promotion s'applique
        int reponse = JOptionPane.showConfirmDialog(this, "Y a-t-il une promotion sur cet article ?", 
                                                    "Promotion", JOptionPane.YES_NO_OPTION);
        double tauxReduction = 0.0;

        if (reponse == JOptionPane.YES_OPTION) {
            String reductionStr = JOptionPane.showInputDialog(this, "Entrez le pourcentage de réduction (%) :");
            if (reductionStr != null) {
                try {
                    tauxReduction = Double.parseDouble(reductionStr);
                    if (tauxReduction < 0 || tauxReduction > 100) {
                        JOptionPane.showMessageDialog(this, "Veuillez entrer un pourcentage valide entre 0 et 100.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Erreur : Entrez un nombre valide.");
                    return;
                }
            }
        }

        // Calcul du prix après réduction
        double prixReduit = prixInitial * (1 - (tauxReduction / 100));

        // Ajout dans la base de données
        String sql = "INSERT INTO Contient (id_commande, id_stock, Qte, prix_initial) VALUES (?, ?, ?, ?)";
        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCommande);
            pstmt.setInt(2, idStock);
            pstmt.setInt(3, quantite);
            pstmt.setDouble(4, prixReduit);
            pstmt.executeUpdate();
            System.out.println("✅ Article ajouté à la commande en BD !");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout de l'article en BD : " + ex.getMessage());
        }

        // Ajouter la promotion en base si nécessaire
        if (tauxReduction > 0) {
            LocalDate dateDebut = LocalDate.now();
            LocalDate dateFin = dateDebut.plusDays(30); // Promotion valide pour 30 jours par défaut

            // ✅ Enregistrement de la promotion avec description et dates
            promotionCRUD.ajouterPromotion(idStock, "Promo spéciale", tauxReduction, dateDebut, dateFin);
            System.out.println("✅ Promotion enregistrée !");
        }
    }

        
    private double getPrixArticle(int idStock) throws ClassNotFoundException {
        String sql = "SELECT prix_initial FROM Stock WHERE id_stock = ?";
        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idStock);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("prix_initial");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1; // Retourne -1 en cas d'erreur
    }
    
    private void mettreAJourQuantiteEnBase(int idCommande, int idStock, int nouvelleQuantite) throws ClassNotFoundException {
        String sql = "UPDATE Contient SET Qte = ? WHERE id_commande = ? AND id_stock = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nouvelleQuantite);
            pstmt.setInt(2, idCommande);
            pstmt.setInt(3, idStock);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Quantité mise à jour en BD !");
            } else {
                JOptionPane.showMessageDialog(this, "Erreur : La mise à jour n'a pas été effectuée.", "Erreur BD", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour de la quantité en BD : " + ex.getMessage(), "Erreur BD", JOptionPane.ERROR_MESSAGE);
        }
    }



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new java.awt.Panel();
        jbtnQuitter = new javax.swing.JButton();
        jBtn_CréerCommande = new javax.swing.JButton();
        jBtn_AffcherCommande = new javax.swing.JButton();
        jBtn_ajouterAuPanier = new javax.swing.JButton();
        jBtn_Modifier = new javax.swing.JButton();
        jBtn_supprimer = new javax.swing.JButton();
        jBtn_SelectionnerClient = new javax.swing.JButton();
        jBtn_Facturer = new javax.swing.JButton();
        jLabelGestionClient = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable_Panier = new javax.swing.JTable();
        jComboBox_Stocks = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextF_Nom = new javax.swing.JTextField();
        jTextF_Prenom = new javax.swing.JTextField();
        jTextF_Rue = new javax.swing.JTextField();
        jTextF_CodePostal = new javax.swing.JTextField();
        jTextF_Ville = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panel1.setBackground(new java.awt.Color(0, 51, 102));
        panel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jbtnQuitter.setText("Quitter");
        jbtnQuitter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnQuitterActionPerformed(evt);
            }
        });
        panel1.add(jbtnQuitter, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 450, -1, -1));

        jBtn_CréerCommande.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtn_CréerCommande.setText("Créer Commande");
        jBtn_CréerCommande.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn_CréerCommandeActionPerformed(evt);
            }
        });
        panel1.add(jBtn_CréerCommande, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 150, -1));

        jBtn_AffcherCommande.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtn_AffcherCommande.setText("Affichage");
        jBtn_AffcherCommande.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn_AffcherCommandeActionPerformed(evt);
            }
        });
        panel1.add(jBtn_AffcherCommande, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 150, -1));

        jBtn_ajouterAuPanier.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtn_ajouterAuPanier.setText("Ajouter ");
        jBtn_ajouterAuPanier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn_ajouterAuPanierActionPerformed(evt);
            }
        });
        panel1.add(jBtn_ajouterAuPanier, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 150, -1));

        jBtn_Modifier.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtn_Modifier.setText("Modifier");
        jBtn_Modifier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn_ModifierActionPerformed(evt);
            }
        });
        panel1.add(jBtn_Modifier, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 260, 150, -1));

        jBtn_supprimer.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtn_supprimer.setText("Supprimer");
        jBtn_supprimer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn_supprimerActionPerformed(evt);
            }
        });
        panel1.add(jBtn_supprimer, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 150, -1));

        jBtn_SelectionnerClient.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtn_SelectionnerClient.setText("Selectionner Client");
        jBtn_SelectionnerClient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn_SelectionnerClientActionPerformed(evt);
            }
        });
        panel1.add(jBtn_SelectionnerClient, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 160, -1));

        jBtn_Facturer.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtn_Facturer.setText("Payer");
        jBtn_Facturer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn_FacturerActionPerformed(evt);
            }
        });
        panel1.add(jBtn_Facturer, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 400, 150, -1));

        getContentPane().add(panel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 170, 500));

        jLabelGestionClient.setBackground(new java.awt.Color(255, 255, 255));
        jLabelGestionClient.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        jLabelGestionClient.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/imdGestVente.png"))); // NOI18N
        jLabelGestionClient.setText("Gestion Vente");
        getContentPane().add(jLabelGestionClient, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 0, 333, 80));

        jTable_Panier.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID_Stock", "Designation", "Quantite", "Prix_initial", "Total"
            }
        ));
        jScrollPane1.setViewportView(jTable_Panier);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 280, 660, 150));

        jComboBox_Stocks.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(jComboBox_Stocks, new org.netbeans.lib.awtextra.AbsoluteConstraints(606, 230, 180, -1));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Nom");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 90, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Prenom");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 150, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Rue");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 100, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("CodePostal");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 150, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Ville");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 120, -1, -1));
        getContentPane().add(jTextF_Nom, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 100, 130, -1));
        getContentPane().add(jTextF_Prenom, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 150, 130, -1));
        getContentPane().add(jTextF_Rue, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 100, 110, -1));
        getContentPane().add(jTextF_CodePostal, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 150, 110, -1));
        getContentPane().add(jTextF_Ville, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 120, 80, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtn_CréerCommandeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn_CréerCommandeActionPerformed
        if (idClientSelectionne == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un client avant de créer une commande !");
            return;
        }

        try {
             // ✅ Récupérer l'ID de l'utilisateur connecté
           Users userConnecte = Users.getSession();
            if (userConnecte == null) {
                JOptionPane.showMessageDialog(this, "Aucun utilisateur connecté !");
                return;
            }

            int idUserConnecte = userConnecte.getIdUser(); // Récupère l'ID de l'utilisateur connecté

           /*// ✅ Récupérer l'ID de l'employé correspondant
           Employe employe = Employe.getEmployeByUserId(idUserConnecte);
           if (employe == null) {
               JOptionPane.showMessageDialog(this, "Aucun employé trouvé pour cet utilisateur !");
               return;
           }
           int idEmploye = employe.getIdEmploye();*/

            
            
            // Créer la commande avec l'ID de l'employé
            Commande nouvelleCommande = new Commande(idClientSelectionne, idUserConnecte, "En cours", "Non défini", 0);

            //Commande nouvelleCommande = new Commande(idClientSelectionne, idEmploye, "En cours", "Non défini", 0);
            
            //Commande nouvelleCommande = new Commande(idClientSelectionne, "En cours", "Non défini", 0);
            boolean success = commandeCRUD.ajouterCommande(nouvelleCommande);
            
            if (success) {
                // 🔹 Récupérer l'ID de la commande créée
                this.idCommandeEnCours = commandeCRUD.getLastInsertedCommandeId();

                // 🔹 Afficher une boîte de dialogue avec l'ID et l'état
                JOptionPane.showMessageDialog(this, 
                    "✅ Commande créée avec succès !\n\n" +
                    "🆔 ID Commande : " + idCommandeEnCours + "\n" +
                    "📌 État : En cours", 
                    "Commande Créée", JOptionPane.INFORMATION_MESSAGE);

                // 🔹 Charger les articles disponibles après la création
                chargerArticlesStock();
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de la création de la commande !");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }//GEN-LAST:event_jBtn_CréerCommandeActionPerformed

    private void jBtn_AffcherCommandeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn_AffcherCommandeActionPerformed
        // TODO add your handling code here:
        new PageCommande().setVisible(true);
    }//GEN-LAST:event_jBtn_AffcherCommandeActionPerformed

    private void jBtn_ajouterAuPanierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn_ajouterAuPanierActionPerformed
   

        try {
            if (idCommandeEnCours < 0) {
                JOptionPane.showMessageDialog(this, "Créez d’abord une commande.");
                return;
            }

            // ✅ 1️⃣ Vérifier si un article est sélectionné
            if (jComboBox_Stocks.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un article avant d'ajouter au panier !");
                return;
            }

            // ✅ 2️⃣ Récupérer l'article sélectionné
            String selectedItem = jComboBox_Stocks.getSelectedItem().toString(); 
            int idStock = Integer.parseInt(selectedItem.split(" - ")[0]); 
            String designation = selectedItem.split(" - ")[1]; 

            // ✅ 3️⃣ Demander la quantité
            String qteStr = JOptionPane.showInputDialog(this, "Quantité : ", "1");
            if (qteStr == null || qteStr.trim().isEmpty()) return;

            int quantite;
            try {
                quantite = Integer.parseInt(qteStr);
                if (quantite <= 0) {
                    JOptionPane.showMessageDialog(this, "Veuillez entrer une quantité valide (> 0).", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Quantité invalide. Entrez un nombre entier.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ✅ 4️⃣ Récupérer le prix initial réel AVANT application d'une promotion
            double prixInitialReel = stockCRUD.getPrixInitial(idStock);
            if (prixInitialReel < 0) {
                JOptionPane.showMessageDialog(this, "Erreur : Impossible de récupérer le prix de l'article.");
                return;
            }

            double prixFinal = prixInitialReel; // Par défaut, on garde le prix initial

            // ✅ 5️⃣ Vérifier s'il y a une promotion active (supprime d'abord les expirées)
            promotionCRUD.supprimerPromotionsExpirees();
            Promotion promotionActive = promotionCRUD.getPromotionActive(idStock);

            if (promotionActive != null) {
                // ✅ Récupérer directement le prix après réduction en BD
                prixFinal = stockCRUD.getPrixActuel(idStock);

                JOptionPane.showMessageDialog(this, 
                    "⚠ Une promotion de " + promotionActive.getTauxReduction() + "% est DÉJÀ appliquée sur cet article !\n" + 
                    "Le prix actuel enregistré est de : " + prixFinal + " €.", 
                    "Promotion existante", JOptionPane.WARNING_MESSAGE
                );
            } else {
                // ✅ 6️⃣ Aucune promotion existante → proposer d'en ajouter une
                int appliquerPromo = JOptionPane.showConfirmDialog(this, "Voulez-vous appliquer une promotion ?", "Promotion", JOptionPane.YES_NO_OPTION);
                if (appliquerPromo == JOptionPane.YES_OPTION) {
                    String tauxStr = JOptionPane.showInputDialog(this, "Entrez le pourcentage de réduction (ex: 10 pour 10%) :");
                    if (tauxStr != null && !tauxStr.trim().isEmpty()) {
                        try {
                            double tauxReduction = Double.parseDouble(tauxStr);
                            if (tauxReduction < 0 || tauxReduction > 100) {
                                JOptionPane.showMessageDialog(this, "Veuillez entrer une réduction valide (entre 0 et 100).", "Erreur", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            prixFinal = prixInitialReel - ((tauxReduction / 100) * prixInitialReel);

                            // ✅ 7️⃣ Demander la durée de la promotion
                            String joursPromo = JOptionPane.showInputDialog(this, "Durée de la promotion (en jours) :");
                            int dureePromo = Integer.parseInt(joursPromo);
                            if (dureePromo <= 0) {
                                JOptionPane.showMessageDialog(this, "Durée invalide. Entrez un nombre positif.", "Erreur", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            // ✅ 8️⃣ Enregistrer la promotion en BD
                            LocalDate dateDebut = LocalDate.now();
                            LocalDate dateFin = dateDebut.plusDays(dureePromo);
                            promotionCRUD.ajouterPromotion(idStock, "Promotion spéciale", tauxReduction, dateDebut, dateFin);

                            // ✅ 9️⃣ Mettre à jour le prix réduit en BD
                            stockCRUD.mettreAJourPrixReduit(idStock, prixFinal);

                            JOptionPane.showMessageDialog(this, "✅ Nouvelle promotion enregistrée !");
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(this, "Valeur invalide pour la promotion. Entrez un nombre.", "Erreur", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
            }

            // ✅ 🔟 Vérifier si l'article est déjà dans la commande
            String checkSQL = "SELECT Qte FROM Contient WHERE id_commande = ? AND id_stock = ?";
            int quantiteExistante = 0;

            try (Connection conn = Data.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
                checkStmt.setInt(1, idCommandeEnCours);
                checkStmt.setInt(2, idStock);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        quantiteExistante = rs.getInt("Qte");
                    }
                }
            }

            if (quantiteExistante > 0) {
                // ✅ L'article est déjà dans la commande → mise à jour de la quantité
                String updateSQL = "UPDATE Contient SET Qte = Qte + ?, prix_initial = ? WHERE id_commande = ? AND id_stock = ?";
                try (Connection conn = Data.getConnection();
                     PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                    updateStmt.setInt(1, quantite);
                    updateStmt.setDouble(2, prixFinal);
                    updateStmt.setInt(3, idCommandeEnCours);
                    updateStmt.setInt(4, idStock);
                    updateStmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Article déjà ajouté, quantité mise à jour !");
                }
            } else {
                // ✅ L'article n'est pas encore dans la commande → insertion
                String insertSQL = "INSERT INTO Contient (id_commande, id_stock, Qte, prix_initial) VALUES (?, ?, ?, ?)";
                try (Connection conn = Data.getConnection();
                     PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    insertStmt.setInt(1, idCommandeEnCours);
                    insertStmt.setInt(2, idStock);
                    insertStmt.setInt(3, quantite);
                    insertStmt.setDouble(4, prixFinal);
                    insertStmt.executeUpdate();
                    System.out.println("✅ Article ajouté à la commande en BD !");
                }
            }

            // ✅ 🔟 Ajouter l'article dans le panier (JTable)
            DefaultTableModel modelPanier = (DefaultTableModel) jTable_Panier.getModel();
            double totalLigne = quantite * prixFinal;
            modelPanier.addRow(new Object[]{ idStock, designation, quantite, prixFinal, totalLigne });

            JOptionPane.showMessageDialog(this, "Article ajouté au panier avec succès !");
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_jBtn_ajouterAuPanierActionPerformed

    
    private void jBtn_ModifierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn_ModifierActionPerformed
       int selectedRow = jTable_Panier.getSelectedRow();
    
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un article à modifier.", "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String quantiteStr = JOptionPane.showInputDialog(this, "Entrez la nouvelle quantité :");

        if (quantiteStr == null || quantiteStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La quantité ne peut pas être vide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int nouvelleQuantite = Integer.parseInt(quantiteStr);
            if (nouvelleQuantite <= 0) {
                JOptionPane.showMessageDialog(this, "La quantité doit être supérieure à zéro.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Récupérer les infos de l'article sélectionné
            int idStock = (int) jTable_Panier.getValueAt(selectedRow, 0); // ID_Stock
            double prixUnitaire = (double) jTable_Panier.getValueAt(selectedRow, 3); // Prix unitaire

            // Calculer le nouveau total
            double nouveauTotal = prixUnitaire * nouvelleQuantite;

           try {
               // ✅ Mettre à jour la quantité en base de données (table Contient)
               mettreAJourQuantiteEnBase(idCommandeEnCours, idStock, nouvelleQuantite);
           } catch (ClassNotFoundException ex) {
               Logger.getLogger(PageVente.class.getName()).log(Level.SEVERE, null, ex);
           }

            // ✅ Mise à jour de la JTable_Panier
            jTable_Panier.setValueAt(nouvelleQuantite, selectedRow, 2);
            jTable_Panier.setValueAt(nouveauTotal, selectedRow, 4);

            JOptionPane.showMessageDialog(this, "Quantité mise à jour avec succès !");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer un nombre valide pour la quantité.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jBtn_ModifierActionPerformed

    private void jBtn_supprimerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn_supprimerActionPerformed
                                      
        int selectedRow = jTable_Panier.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un article à supprimer du panier.");
            return;
        }

        // Récupérer les informations de l'article sélectionné
        int idStock = (int) jTable_Panier.getValueAt(selectedRow, 0);
        int quantite = (int) jTable_Panier.getValueAt(selectedRow, 2);
        double prixArticle = (double) jTable_Panier.getValueAt(selectedRow, 3);
        double totalLigne = (double) jTable_Panier.getValueAt(selectedRow, 4);

        // Demander confirmation
        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous vraiment supprimer cet article du panier ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // ✅ Supprimer l'article de la base de données
                supprimerArticleDansCommande(idCommandeEnCours, idStock);

                // ✅ Recalculer le montant total après suppression
                double nouveauTotal = calculerMontantTotalCommande(idCommandeEnCours);
                miseAJourTotalCommande(idCommandeEnCours, nouveauTotal);

                // ✅ Mettre à jour l'affichage dans la JTable
                DefaultTableModel model = (DefaultTableModel) jTable_Panier.getModel();
                model.removeRow(selectedRow);

                // ✅ Rafraîchir la table qui affiche la commande
                chargerArticlesDansPanier(idCommandeEnCours);

                // ✅ Afficher le nouveau total
                JOptionPane.showMessageDialog(this, 
                    "Article supprimé avec succès !\nNouveau total : " + nouveauTotal + " €",
                    "Suppression réussie", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la suppression de l'article : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    
    }//GEN-LAST:event_jBtn_supprimerActionPerformed

    private void supprimerArticleDansCommande(int idCommande, int idStock) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM Contient WHERE id_commande = ? AND id_stock = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCommande);
            pstmt.setInt(2, idStock);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Article supprimé de la commande en base.");
            } else {
                JOptionPane.showMessageDialog(this, "L'article n'a pas été trouvé dans la commande.");
            }
        }
    }
    
    private void miseAJourTotalCommande(int idCommande, double nouveauTotal) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Commande SET total = ? WHERE id_commande = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, nouveauTotal);
            pstmt.setInt(2, idCommande);
            pstmt.executeUpdate();
            System.out.println("✅ Total de la commande mis à jour : " + nouveauTotal);
        }
    }


    
    private void jBtn_SelectionnerClientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn_SelectionnerClientActionPerformed
        PageEmploye pageEmploye = new PageEmploye(this); // Passer `this` pour référencer `PageVente`
        pageEmploye.setVisible(true);


        // -> Il n’y a pas de code juste après,
        //    car ce JFrame n’est pas modal : le programme continue.
        //    La sélection sera faite directement depuis la fenêtre clients.
    }//GEN-LAST:event_jBtn_SelectionnerClientActionPerformed

    private void jbtnQuitterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnQuitterActionPerformed
        // TODO add your handling code here:
        this.dispose(); // Ferme la fenêtre
    }//GEN-LAST:event_jbtnQuitterActionPerformed
    
    private void mettreAJourStock(int idCommande) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Stock s JOIN Contient c ON s.id_stock = c.id_stock " +
                     "SET s.quantite_en_stock = s.quantite_en_stock - c.Qte " +
                     "WHERE c.id_commande = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCommande);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✅ Stock mis à jour avec succès !");
            } else {
                JOptionPane.showMessageDialog(this, "Aucune mise à jour du stock effectuée. Vérifiez la commande.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour des stocks : " + e.getMessage());
        }
    }

    
    private void miseAJourStatutCommande(int idCommande, String statut) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Commande SET etat = ? WHERE id_commande = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, statut);
            pstmt.setInt(2, idCommande);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour du statut de la commande.");
        }
    }
    
    private double calculerMontantTotalCommande(int idCommande) throws SQLException, ClassNotFoundException {
        String sql = "SELECT SUM(Qte * prix_initial) AS total FROM Contient WHERE id_commande = ?";
        double total = 0;

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCommande);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    private void jBtn_FacturerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn_FacturerActionPerformed
                                                      
        if (idCommandeEnCours == -1) {
            JOptionPane.showMessageDialog(this, "Aucune commande en cours.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double montantTotal = 0;
        try {
            montantTotal = calculerMontantTotalCommande(idCommandeEnCours);
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(PageVente.class.getName()).log(Level.SEVERE, null, ex);
        }
        JOptionPane.showMessageDialog(this, "Montant total de la commande : " + montantTotal + " €");

        // 🔹 Sélection du mode de paiement
        String[] options = {"Carte", "Espèces", "En deux temps"};
        String choix = (String) JOptionPane.showInputDialog(this, "Choisissez un mode de paiement :", "Paiement",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choix == null) return;

        double montantPaye = 0;
        double montantRestant = 0;

        if ("Carte".equals(choix) || "Espèces".equals(choix)) {
            montantPaye = montantTotal;
        } else if ("En deux temps".equals(choix)) {
            String montantStr = JOptionPane.showInputDialog(this, "Entrez le montant payé en espèces :");
            if (montantStr == null) return;
            montantPaye = Double.parseDouble(montantStr);
            montantRestant = montantTotal - montantPaye;
            JOptionPane.showMessageDialog(this, "Le montant restant à payer est : " + montantRestant + " € (via carte)");
        }

        // 🔹 Récupération des articles et des prix réels avec réduction
        List<Integer> idStocks = new ArrayList<>();
        List<String> articles = new ArrayList<>();
        List<Integer> quantites = new ArrayList<>();
        List<Double> prixUnitaires = new ArrayList<>();
        List<Double> tauxReductions = new ArrayList<>(); // ✅ Stockage des réductions appliquées
        int idEmploye = 1; // À remplacer avec l'ID réel de l'employé
        int idClient = -1; // ✅ Ajout de l'ID du client

        String sql = "SELECT s.id_stock, s.nom_article, c.Qte, c.prix_initial, cm.id_emp, cm.id_client, p.taux_reduction " +
             "FROM Contient c " +
             "JOIN Stock s ON c.id_stock = s.id_stock " +
             "JOIN Commande cm ON c.id_commande = cm.id_commande " +
             "LEFT JOIN applique a ON s.id_stock = a.id_stock " +  // ✅ Utiliser applique pour récupérer l'id de la promo
             "LEFT JOIN promotions p ON a.id_promotion = p.id_promotion " + // ✅ Relier applique aux promotions
             "WHERE c.id_commande = ?";

        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCommandeEnCours);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    idStocks.add(rs.getInt("id_stock"));
                    articles.add(rs.getString("nom_article"));
                    quantites.add(rs.getInt("Qte"));
                    prixUnitaires.add(rs.getDouble("prix_initial"));
                    idEmploye = rs.getInt("id_emp");
                    idClient = rs.getInt("id_client");

                    // ✅ Vérification si une promotion est appliquée
                    double reduction = rs.getDouble("taux_reduction");
                    tauxReductions.add(reduction);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la récupération des articles.");
            return;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PageVente.class.getName()).log(Level.SEVERE, null, ex);
        }

        // 🔹 Génération de la facture avec les données correctes
        String filePath = "Facture_Commande_" + idCommandeEnCours + ".pdf";
        String dateEmission = java.time.LocalDate.now().toString();

        Facture facture = new Facture(filePath, idCommandeEnCours, dateEmission, idEmploye, idClient, 
                                      idStocks, articles, quantites, prixUnitaires, tauxReductions, montantTotal);
        try {
            facture.generatePDF();
            JOptionPane.showMessageDialog(this, "Facture PDF générée avec succès !");
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la génération de la facture PDF.");
        }

        try {
            // 🔹 Mise à jour des stocks et clôture de la commande
            mettreAJourStock(idCommandeEnCours);
            miseAJourStatutCommande(idCommandeEnCours, "Clôturée");
            JOptionPane.showMessageDialog(this, "Paiement effectué avec succès !");
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(PageVente.class.getName()).log(Level.SEVERE, null, ex);
        }

        // 🔹 Vider le panier après facturation
        DefaultTableModel modelPanier = (DefaultTableModel) jTable_Panier.getModel();
        modelPanier.setRowCount(0); // ✅ Vide la `JTable` après la facturation

    }//GEN-LAST:event_jBtn_FacturerActionPerformed

        
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PageVente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PageVente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PageVente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PageVente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new PageVente().setVisible(true);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(PageVente.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtn_AffcherCommande;
    private javax.swing.JButton jBtn_CréerCommande;
    private javax.swing.JButton jBtn_Facturer;
    private javax.swing.JButton jBtn_Modifier;
    private javax.swing.JButton jBtn_SelectionnerClient;
    private javax.swing.JButton jBtn_ajouterAuPanier;
    private javax.swing.JButton jBtn_supprimer;
    private javax.swing.JComboBox<String> jComboBox_Stocks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelGestionClient;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable_Panier;
    private javax.swing.JTextField jTextF_CodePostal;
    private javax.swing.JTextField jTextF_Nom;
    private javax.swing.JTextField jTextF_Prenom;
    private javax.swing.JTextField jTextF_Rue;
    private javax.swing.JTextField jTextF_Ville;
    private javax.swing.JButton jbtnQuitter;
    private java.awt.Panel panel1;
    // End of variables declaration//GEN-END:variables
}
