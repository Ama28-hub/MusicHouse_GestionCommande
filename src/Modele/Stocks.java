/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modele;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Carmelle Adou
 */
public class Stocks {
    
    private int idStock;
    private String nomArticle;
    private String descriptionArticle;
    private int quantiteEnStock;
    private int seuilMin;
    //private double prixUnitaire;
    private Double prixInitial;
    private String Etat;

    public void setEtat(String Etat) {
        this.Etat = Etat;
    }

    public String getEtat() {
        return Etat;
    }

    public Stocks(int idStock, String nomArticle, String descriptionArticle, int quantiteEnStock, int seuilMin, double prixInitial, String Etat) {
        this.idStock = idStock;
        this.nomArticle = nomArticle;
        this.descriptionArticle = descriptionArticle;
        this.quantiteEnStock = quantiteEnStock;
        this.seuilMin = seuilMin;
        this.prixInitial = prixInitial;
        this.Etat = Etat;
    }

    public int getIdStock() {
        return idStock;
    }

    public void setIdStock(int idStock) {
        this.idStock = idStock;
    }

    public String getNomArticle() {
        return nomArticle;
    }

    public void setNomArticle(String nomArticle) {
        this.nomArticle = nomArticle;
    }

    public String getDescriptionArticle() {
        return descriptionArticle;
    }

    public void setDescriptionArticle(String descriptionArticle) {
        this.descriptionArticle = descriptionArticle;
    }

    public int getQuantiteEnStock() {
        return quantiteEnStock;
    }

    public void setQuantiteEnStock(int quantiteEnStock) {
        this.quantiteEnStock = quantiteEnStock;
    }

    public int getSeuilMin() {
        return seuilMin;
    }

    public void setSeuilMin(int seuilMin) {
        this.seuilMin = seuilMin;
    }

    public Double getPrixInitial() {
        return prixInitial;
    }

    public void setPrixInitial(Double prixInitial) {
        this.prixInitial = prixInitial;
    }

    public static List<Stocks> getStockList(Connection conn) throws SQLException {
        List<Stocks> stockList = new ArrayList<>();
        String query = "SELECT * FROM Stock";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                stockList.add(new Stocks(
                    rs.getInt("id_stock"),
                    rs.getString("nom_article"), rs.getString("description_article"),
                    rs.getInt("quantite_en_stock"), rs.getInt("seuil_min"), 
                    rs.getDouble("prix_initial"), rs.getString("etat")));
            }
        }
        return stockList;
    }
}

