/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vue;

import Modele.Adresse;
import Modele.Client;
import crud.ClientsCRUD;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;


/**
 *
 * @author Carmelle Adou
 */
public class PageEmploye extends javax.swing.JFrame {
    private ClientsCRUD clientsCRUD;
    private javax.swing.JFrame previousPage;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private PageVente pageVente; // Ajout d'un attribut pour stocker la fenêtre de vente

    
    
    /**
     * Creates new form PageAdmin
     */
    public PageEmploye(javax.swing.JFrame previousPage) {
        this.previousPage = previousPage;
        initComponents();
        this.clientsCRUD = new ClientsCRUD();
        chargerClients();
        rowSorter = new TableRowSorter<>((DefaultTableModel) jTableClients.getModel());
        jTableClients.setRowSorter(rowSorter);

        initListeners();
    }
    
    
    public PageEmploye(PageVente pageVente) {
        this.pageVente = pageVente;
        initComponents();
        this.clientsCRUD = new ClientsCRUD();
        chargerClients(); // Appel pour remplir la table
    }


    

    private void initListeners() {
        jBtnAjouter.addActionListener(evt -> ajouterClient());
        jbtnmodifier.addActionListener(evt -> modifierClient());
        jbtnsupprimer.addActionListener(evt -> supprimerClient());
        //jbtnExit.addActionListener(evt -> System.exit(0));
        jBtnretour.addActionListener(evt -> retournerPagePrecedente());
        jbtnsearch.addActionListener(evt -> rechercherClient());
        
        itemStock.addActionListener(evt -> ouvrirNouvellePage(new PageStocks(this)));
        itemVente.addActionListener(evt -> {
            try {
                ouvrirNouvellePage(new PageVente(/*this*/));
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(PageEmploye.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private void ouvrirNouvellePage(javax.swing.JFrame nouvellePage) {
        nouvellePage.setVisible(true);
        this.dispose();
    }
    
    private void retournerPagePrecedente() {
        if (previousPage != null) {
            System.out.println("Retour à la page précédente : " + previousPage.getClass().getSimpleName()); // Debug
            previousPage.setVisible(true);
        } else {
            System.out.println("Impossible de retourner, aucune page précédente enregistrée !");
        }
        this.dispose();
    }
    
    private void chargerClients() {
        try {
            if (clientsCRUD == null) {
                clientsCRUD = new ClientsCRUD(); // Instanciation si null
            }

            List<Client> listeClients = clientsCRUD.getAllClients();
            DefaultTableModel model = (DefaultTableModel) jTableClients.getModel();

            // ✅ Vider complètement le tableau avant de le recharger
            model.setRowCount(0);

            for (Client client : listeClients) {
                Adresse adresse = client.getAdresse();
                model.addRow(new Object[]{
                    client.getIdClient(),
                    client.getNom(),
                    client.getPrenom(),
                    adresse.getRue(),
                    adresse.getCodePostal(),
                    adresse.getVille(),
                    client.getEmail(),
                    client.getTelephone()
                });
            }

            // ✅ Rafraîchir l'affichage automatiquement
            model.fireTableDataChanged();
            jTableClients.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement des clients : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void rechercherClient() {
        String searchText = jTextField1.getText().trim();
        if (rowSorter == null) {
            System.out.println("Erreur : Le rowSorter n'a pas été initialisé !");
            return;
        }
        if (searchText.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    
    private void ajouterClient() {
        new FormClient(this, true, null).setVisible(true);
        chargerClients();
    }

    private void modifierClient() {
        int selectedRow = jTableClients.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client à modifier.");
            return;
        }

        try {
            int id = Integer.parseInt(jTableClients.getValueAt(selectedRow, 0).toString());
            Client client = clientsCRUD.getClientById(id);

            if (client != null) {
                // ✅ Ouvrir le formulaire de modification
                FormClient form = new FormClient(this, true, client);
                form.setVisible(true);

                // ✅ Mise à jour des données en base et recharge de la JTable
                chargerClients();
                System.out.println("🔍 Vérification après modification :");
                for (int i = 0; i < jTableClients.getRowCount(); i++) {
                    System.out.println("Client " + jTableClients.getValueAt(i, 0) + " | " + 
                                        jTableClients.getValueAt(i, 1) + " " + 
                                        jTableClients.getValueAt(i, 2) + " | " + 
                                        jTableClients.getValueAt(i, 4));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Client introuvable en base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la récupération du client : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void supprimerClient() {
        int selectedRow = jTableClients.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client à supprimer.");
            return;
        }

        try {
            int id = Integer.parseInt(jTableClients.getValueAt(selectedRow, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment supprimer ce client ?", "Confirmation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = clientsCRUD.archiverClient(id);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Client archivé avec succès !");
                    chargerClients();
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur : impossible d'archiver ce client.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression du client : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTableClients = new javax.swing.JTable();
        jLabelGestionClient = new javax.swing.JLabel();
        panel1 = new java.awt.Panel();
        jBtnAjouter = new javax.swing.JButton();
        jbtnmodifier = new javax.swing.JButton();
        jbtnsupprimer = new javax.swing.JButton();
        jBtn_Quitter = new javax.swing.JButton();
        jBtn_SelectionnerClient1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jbtnsearch = new javax.swing.JButton();
        jBtnretour = new javax.swing.JButton();
        jMenuGestion = new javax.swing.JMenuBar();
        jMenuGerer = new javax.swing.JMenu();
        itemStock = new javax.swing.JMenuItem();
        itemVente = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTableClients.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "identifiant", "Nom", "Prenom", "rue", "code postal", "ville"
            }
        ));
        jScrollPane1.setViewportView(jTableClients);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 120, 690, 220));

        jLabelGestionClient.setBackground(new java.awt.Color(255, 255, 255));
        jLabelGestionClient.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        jLabelGestionClient.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/imgGestClient.png"))); // NOI18N
        jLabelGestionClient.setText("Gestion Client");
        getContentPane().add(jLabelGestionClient, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 10, 240, 90));

        panel1.setBackground(new java.awt.Color(0, 51, 102));
        panel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jBtnAjouter.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jBtnAjouter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/imgAdd.png"))); // NOI18N
        jBtnAjouter.setText("Ajouter");
        panel1.add(jBtnAjouter, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 130, -1));

        jbtnmodifier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/imgmodif.png"))); // NOI18N
        jbtnmodifier.setText("Modifier");
        panel1.add(jbtnmodifier, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 140, -1));

        jbtnsupprimer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/imgsupp.png"))); // NOI18N
        jbtnsupprimer.setText("Supprimer");
        panel1.add(jbtnsupprimer, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        jBtn_Quitter.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jBtn_Quitter.setText("Quitter");
        jBtn_Quitter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn_QuitterActionPerformed(evt);
            }
        });
        panel1.add(jBtn_Quitter, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 346, 90, 40));

        jBtn_SelectionnerClient1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jBtn_SelectionnerClient1.setText("Selectionner");
        jBtn_SelectionnerClient1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn_SelectionnerClient1ActionPerformed(evt);
            }
        });
        panel1.add(jBtn_SelectionnerClient1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, 150, 40));

        getContentPane().add(panel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 170, 420));
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 80, 230, 30));

        jbtnsearch.setText("search");
        getContentPane().add(jbtnsearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 80, 90, 30));

        jBtnretour.setText("Retour");
        getContentPane().add(jBtnretour, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 360, -1, -1));

        jMenuGerer.setText("Gestion");

        itemStock.setText("Gestion Stocks");
        jMenuGerer.add(itemStock);

        itemVente.setText("Gestion Ventes");
        jMenuGerer.add(itemVente);

        jMenuGestion.add(jMenuGerer);

        setJMenuBar(jMenuGestion);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtn_QuitterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn_QuitterActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this, "Êtes-vous sûr de vouloir quitter ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose(); // Ferme la fenêtre si l'utilisateur confirme
        }
    }//GEN-LAST:event_jBtn_QuitterActionPerformed

    private void jBtn_SelectionnerClient1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn_SelectionnerClient1ActionPerformed
        int selectedRow = jTableClients.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client !");
            return;
        }

        int idClient = Integer.parseInt(jTableClients.getValueAt(selectedRow, 0).toString());
        String nom = jTableClients.getValueAt(selectedRow, 1).toString();
        String prenom = jTableClients.getValueAt(selectedRow, 2).toString();
        String rue = jTableClients.getValueAt(selectedRow, 3).toString();
        String ville = jTableClients.getValueAt(selectedRow, 4).toString();
        String codePostal = jTableClients.getValueAt(selectedRow, 5).toString();

        if (pageVente != null) {
            pageVente.setClientSelectionne(idClient, nom, prenom, rue, ville, codePostal);
            this.dispose(); // Fermer la fenêtre après sélection
        } else {
            JOptionPane.showMessageDialog(this, "Erreur : la page Vente n'est pas accessible !");
        }

        pageVente.setClientSelectionne(idClient, nom, prenom, rue, ville, codePostal);
        this.dispose(); // Fermer la fenêtre

    }//GEN-LAST:event_jBtn_SelectionnerClient1ActionPerformed

    
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
            java.util.logging.Logger.getLogger(PageAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PageAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PageAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PageAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PageEmploye(null).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem itemStock;
    private javax.swing.JMenuItem itemVente;
    private javax.swing.JButton jBtnAjouter;
    private javax.swing.JButton jBtn_Quitter;
    private javax.swing.JButton jBtn_SelectionnerClient1;
    private javax.swing.JButton jBtnretour;
    private javax.swing.JLabel jLabelGestionClient;
    private javax.swing.JMenu jMenuGerer;
    private javax.swing.JMenuBar jMenuGestion;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableClients;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton jbtnmodifier;
    private javax.swing.JButton jbtnsearch;
    private javax.swing.JButton jbtnsupprimer;
    private java.awt.Panel panel1;
    // End of variables declaration//GEN-END:variables
}
