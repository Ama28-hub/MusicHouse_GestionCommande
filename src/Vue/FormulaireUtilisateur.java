/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package Vue;

import Modele.Users;
import crud.UsersCRUD;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import util.Chiffrement;




/**
 *
 * @author Carmelle Adou
 */
public class FormulaireUtilisateur extends javax.swing.JDialog {
    private Users user; // Objet utilisateur (null en mode ajout)
    private boolean isEditMode; // Savoir si on modifie ou ajoute
    private UsersCRUD usersCRUD;
    
    /**
     * Creates new form FormulaireUtilisateur
     */
    public FormulaireUtilisateur(java.awt.Frame parent, boolean modal, Users userToEdit) {
        super(parent, modal);
        initComponents();
        
        jBtnAnnuler.addActionListener(evt -> dispose()); // Ferme la fenêtre sans rien enregistrer
        jBtnEnreg.addActionListener(evt -> enregistrerUtilisateur());
        
        this.user = userToEdit;
        this.usersCRUD = new UsersCRUD();
        isEditMode = (user != null);

        if (isEditMode) {
            remplirFormulaire(); // Remplir les champs en mode modification
        }
    }
    private void remplirFormulaire() {
        jTextFieldNom.setText(user.getNom());
        jTextFieldPrenom.setText(user.getPrenom());
        jTextFieldEmail.setText(user.getEmail());
        jComboBoxRole.setSelectedItem(user.getRole());
    }

    /**
     * Enregistre un nouvel utilisateur ou met à jour un utilisateur existant
     */
    private void enregistrerUtilisateur() {
        String nom = jTextFieldNom.getText().trim();
        String prenom = jTextFieldPrenom.getText().trim();
        String email = jTextFieldEmail.getText().trim();
        String role = jComboBoxRole.getSelectedItem().toString();
        String password = jTextFieldmdp.getText();
        String titre = jComboBoxTitre.getSelectedItem().toString();
        String entreprise = jTextFieldEntreprise.getText().trim();
        Date dateEmbauche = new java.util.Date(); // Date par défaut

        // Vérification des champs obligatoires
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || (password.isEmpty() && !isEditMode)) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs obligatoires !");
            return;
        }

        try {
            List<Integer> privileges = determinerPrivileges(role);
            boolean success;

            if (isEditMode) {
                // Mise à jour de l'utilisateur existant
                user.setNom(nom);
                user.setPrenom(prenom);
                user.setEmail(email);
                user.setRole(role);

                // Vérifier si un nouveau mot de passe est saisi
                if (!password.isEmpty()) {
                    user.setMotDePasse(Chiffrement.chiffrer(password));
                }

                success = usersCRUD.updateUserAndEmploye(user, titre, entreprise, new java.sql.Date(dateEmbauche.getTime()), privileges);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Utilisateur mis à jour avec succès !");
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour !");
                }
            } else {
                // Création d'un nouvel utilisateur
                String encryptedPassword = Chiffrement.chiffrer(password);
                Users newUser = new Users(0, nom, prenom, email, encryptedPassword);
                success = usersCRUD.insertUserAndEmploye(newUser, titre, entreprise, new java.sql.Date(dateEmbauche.getTime()), privileges);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Utilisateur ajouté avec succès !");
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout !");
                }
            }
            
            dispose(); // Fermer le formulaire après enregistrement
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Détermine les privilèges associés à un rôle donné.
     */
    private List<Integer> determinerPrivileges(String role) {
        List<Integer> privilegeIds = new ArrayList<>();
        
        switch (role.toLowerCase()) {
            case "admin":
                privilegeIds.add(1);
                break;
            case "user":
                privilegeIds.add(2);
                break;
            case "superviseur":
                privilegeIds.add(3);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Rôle inconnu. Aucun privilège associé.");
                break;
        }
        return privilegeIds;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabNom = new javax.swing.JLabel();
        jLabelPrenom = new javax.swing.JLabel();
        jLabelEmail = new javax.swing.JLabel();
        jLabrole = new javax.swing.JLabel();
        jBtnAnnuler = new javax.swing.JButton();
        jBtnEnreg = new javax.swing.JButton();
        jComboBoxRole = new javax.swing.JComboBox<>();
        jTextFieldEmail = new javax.swing.JTextField();
        jTextFieldNom = new javax.swing.JTextField();
        jTextFieldPrenom = new javax.swing.JTextField();
        jLabMDP = new javax.swing.JLabel();
        jTextFieldmdp = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldEntreprise = new javax.swing.JTextField();
        titre = new javax.swing.JLabel();
        jComboBoxTitre = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabNom.setText("Nom:");
        getContentPane().add(jLabNom, new org.netbeans.lib.awtextra.AbsoluteConstraints(127, 39, -1, -1));

        jLabelPrenom.setText("prenom:");
        getContentPane().add(jLabelPrenom, new org.netbeans.lib.awtextra.AbsoluteConstraints(112, 89, -1, -1));

        jLabelEmail.setText("Email:");
        getContentPane().add(jLabelEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 131, -1, -1));

        jLabrole.setText("Role:");
        getContentPane().add(jLabrole, new org.netbeans.lib.awtextra.AbsoluteConstraints(48, 258, -1, -1));

        jBtnAnnuler.setText("Annuler");
        getContentPane().add(jBtnAnnuler, new org.netbeans.lib.awtextra.AbsoluteConstraints(432, 344, 120, -1));

        jBtnEnreg.setText("Enregistrer");
        getContentPane().add(jBtnEnreg, new org.netbeans.lib.awtextra.AbsoluteConstraints(132, 344, -1, -1));

        jComboBoxRole.setEditable(true);
        jComboBoxRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "admin", "user", "superviseur" }));
        getContentPane().add(jComboBoxRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(169, 255, 117, -1));
        getContentPane().add(jTextFieldEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(169, 128, 183, -1));
        getContentPane().add(jTextFieldNom, new org.netbeans.lib.awtextra.AbsoluteConstraints(169, 36, 183, -1));
        getContentPane().add(jTextFieldPrenom, new org.netbeans.lib.awtextra.AbsoluteConstraints(169, 86, 183, -1));

        jLabMDP.setText("Mot de passe :");
        getContentPane().add(jLabMDP, new org.netbeans.lib.awtextra.AbsoluteConstraints(87, 197, -1, -1));
        getContentPane().add(jTextFieldmdp, new org.netbeans.lib.awtextra.AbsoluteConstraints(169, 194, 183, -1));

        jLabel1.setText("Entreprise:");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 49, 106, -1));
        getContentPane().add(jTextFieldEntreprise, new org.netbeans.lib.awtextra.AbsoluteConstraints(525, 46, 178, -1));

        titre.setText("Titre :");
        getContentPane().add(titre, new org.netbeans.lib.awtextra.AbsoluteConstraints(452, 102, 68, -1));

        jComboBoxTitre.setEditable(true);
        jComboBoxTitre.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "assistant", "manager", "superviseur" }));
        getContentPane().add(jComboBoxTitre, new org.netbeans.lib.awtextra.AbsoluteConstraints(526, 99, 163, -1));

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
            java.util.logging.Logger.getLogger(FormulaireUtilisateur.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormulaireUtilisateur.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormulaireUtilisateur.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormulaireUtilisateur.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FormulaireUtilisateur dialog = new FormulaireUtilisateur(new javax.swing.JFrame(), true, null);
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
    private javax.swing.JComboBox<String> jComboBoxRole;
    private javax.swing.JComboBox<String> jComboBoxTitre;
    private javax.swing.JLabel jLabMDP;
    private javax.swing.JLabel jLabNom;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelEmail;
    private javax.swing.JLabel jLabelPrenom;
    private javax.swing.JLabel jLabrole;
    private javax.swing.JTextField jTextFieldEmail;
    private javax.swing.JTextField jTextFieldEntreprise;
    private javax.swing.JTextField jTextFieldNom;
    private javax.swing.JTextField jTextFieldPrenom;
    private javax.swing.JTextField jTextFieldmdp;
    private javax.swing.JLabel titre;
    // End of variables declaration//GEN-END:variables
}
