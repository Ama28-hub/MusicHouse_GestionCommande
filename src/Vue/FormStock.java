/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package Vue;

import Modele.Stocks;
import Controller.connexion.StockController;
import javax.swing.JOptionPane;
import crud.StockCRUD;


/**
 *
 * @author Carmelle Adou
 */
public class FormStock extends javax.swing.JDialog {
    private  StockController stockController;
    private Stocks stock;
    /**
     * Creates new form FormClient
     */
    public FormStock (java.awt.Frame parent, boolean modal, Stocks stock) {
        super(parent, modal);
        initComponents();
        this.stockController = new StockController();
        this.stock = stock;
        
        if (stock != null) {
           chargerStock(stock); 
        }

        jBtnEnreg.addActionListener(evt -> enregistrerStock());
        jBtnAnnuler.addActionListener(evt -> this.dispose());
    }
    
    private void chargerStock(Stocks stock){
        jTextFieldNom.setText(stock.getNomArticle());
        jTextFieldDescription.setText(stock.getDescriptionArticle());
        jTextFieldQuantite.setText(String.valueOf(stock.getQuantiteEnStock()));
        jTextFieldSeuilMin.setText(String.valueOf(stock.getSeuilMin()));
        
    }
    
    private void enregistrerStock() {
        // 1) Récupérer et valider les champs texte
        String nom = jTextFieldNom.getText().trim();
        String description = jTextFieldDescription.getText().trim();

        int quantite;
        int seuilMin;
        double prix;

        try {
            quantite = Integer.parseInt(jTextFieldQuantite.getText().trim());
            seuilMin = Integer.parseInt(jTextFieldSeuilMin.getText().trim());
            prix     = Double.parseDouble(jTextFieldPrix.getText().trim());

            if (quantite < 0 || seuilMin < 0 || prix < 0) {
                JOptionPane.showMessageDialog(this,
                    "Les valeurs numériques ne peuvent pas être négatives.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Veuillez entrer des valeurs valides pour la quantité, le seuil et le prix.",
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2) Vérifier que les champs textuels obligatoires ne sont pas vides
        if (nom.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Tous les champs doivent être remplis !",
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3) Selon qu’il s’agit d’un nouvel enregistrement ou d’une modification
        try {
            if (stock == null) {
                // Cas d’un nouvel enregistrement
                Stocks nouveauStock = new Stocks(
                    0,              // id_stock = 0 -> Auto-incrément en base
                    nom,
                    description,
                    quantite,
                    seuilMin,
                    //prix,
                    0.0,           // prixInitial (exemple) ou une valeur par défaut
                    "Actif"        // état par défaut
                );

                boolean success = stockController.ajouterStock(nouveauStock);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Stock ajouté avec succès !");
                    this.dispose();  // Fermer le formulaire
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'ajout du stock.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Cas d’une modification d’un stock existant
                stock.setNomArticle(nom);
                stock.setDescriptionArticle(description);
                stock.setQuantiteEnStock(quantite);
                stock.setSeuilMin(seuilMin);
                //stock.setPrixUnitaire(prix);
                // stock.setPrixInitial(...) si nécessaire
                // stock.setEtat(...) si vous gérez aussi un champ état

                boolean success = stockController.modifierStock(stock);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Stock modifié avec succès !");
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de la modification du stock.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur : " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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

        jTextFieldNom = new javax.swing.JTextField();
        jTextFieldDescription = new javax.swing.JTextField();
        jTextFieldQuantite = new javax.swing.JTextField();
        jTextFieldSeuilMin = new javax.swing.JTextField();
        jLabelDescription = new javax.swing.JLabel();
        jLabelNom = new javax.swing.JLabel();
        jLabelQuantite = new javax.swing.JLabel();
        jLabelSeuilMin = new javax.swing.JLabel();
        jBtnEnreg = new javax.swing.JButton();
        jBtnAnnuler = new javax.swing.JButton();
        jTextFieldPrix = new javax.swing.JTextField();
        jLabelPrix1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(jTextFieldNom, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 20, 190, -1));
        getContentPane().add(jTextFieldDescription, new org.netbeans.lib.awtextra.AbsoluteConstraints(189, 80, 190, -1));
        getContentPane().add(jTextFieldQuantite, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 130, 80, -1));
        getContentPane().add(jTextFieldSeuilMin, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 200, 60, -1));

        jLabelDescription.setText("Description:");
        getContentPane().add(jLabelDescription, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 80, -1, 30));

        jLabelNom.setText("Nom :");
        getContentPane().add(jLabelNom, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, -1, -1));

        jLabelQuantite.setText("seuil Min:");
        getContentPane().add(jLabelQuantite, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 130, -1, -1));

        jLabelSeuilMin.setText("Quantite:");
        getContentPane().add(jLabelSeuilMin, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 200, -1, -1));

        jBtnEnreg.setText("Enregister");
        getContentPane().add(jBtnEnreg, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 380, -1, -1));

        jBtnAnnuler.setText("Annuler");
        getContentPane().add(jBtnAnnuler, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 380, 112, -1));
        getContentPane().add(jTextFieldPrix, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 260, 60, -1));

        jLabelPrix1.setText("Prix :");
        getContentPane().add(jLabelPrix1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 260, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(FormClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FormStock dialog = new FormStock(new javax.swing.JFrame(), true,null);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnAnnuler;
    private javax.swing.JButton jBtnEnreg;
    private javax.swing.JLabel jLabelDescription;
    private javax.swing.JLabel jLabelNom;
    private javax.swing.JLabel jLabelPrix1;
    private javax.swing.JLabel jLabelQuantite;
    private javax.swing.JLabel jLabelSeuilMin;
    private javax.swing.JTextField jTextFieldDescription;
    private javax.swing.JTextField jTextFieldNom;
    private javax.swing.JTextField jTextFieldPrix;
    private javax.swing.JTextField jTextFieldQuantite;
    private javax.swing.JTextField jTextFieldSeuilMin;
    // End of variables declaration//GEN-END:variables
}
