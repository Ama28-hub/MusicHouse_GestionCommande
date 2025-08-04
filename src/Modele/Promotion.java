/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modele;

import java.util.Date;

/**
 *
 * @author Carmelle Adou
 */
public class Promotion {
  
    private int idPromotion;
    private String description;
    private double tauxReduction;
    private Date dateDebut;
    private Date dateFin;

    // Constructeur
    public Promotion(int idPromotion, double tauxReduction) {
        this.idPromotion = idPromotion;
        this.description = description;
        this.tauxReduction = tauxReduction;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // Getters et Setters
    public int getIdPromotion() {
        return idPromotion;
    }

    public void setIdPromotion(int idPromotion) {
        this.idPromotion = idPromotion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTauxReduction() {
        return tauxReduction;
    }

    public void setTauxReduction(double tauxReduction) {
        this.tauxReduction = tauxReduction;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }
}


