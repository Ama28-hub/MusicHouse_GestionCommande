/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modele;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/**
 *
 * @author PC
 */
public class Employe {
   private int idEmploye;
    private int idUser;  // Lien avec User
    private String nom;
    private String prenom;

    public static Employe employeActif;  // Variable statique pour stocker l'employé connecté

    // Constructeur
    public Employe(int idEmploye, int idUser, String nom, String prenom) {
        this.idEmploye = idEmploye;
        this.idUser = idUser;
        this.nom = nom;
        this.prenom = prenom;
    }

    // Getters
    public int getIdEmploye() {
        return idEmploye;
    }

    public int getIdUser() {
        return idUser;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    // ✅ Méthode pour récupérer un employé à partir de son `idUser`
    public static Employe getEmployeByUserId(int idUser) {
        Employe employe = null;
        String sql = "SELECT id_emp, id_user, nom, prenom FROM employe WHERE id_user = ?";
        
        try (Connection conn = Data.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUser);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    employe = new Employe(
                        rs.getInt("id_emp"),
                        rs.getInt("id_user"),
                        rs.getString("nom"),
                        rs.getString("prenom")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return employe;
    }
}
