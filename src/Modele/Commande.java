package Modele;

import java.util.Date;

public class Commande {
    private int idCommande;
    private int idClient;
    private int idEmploye;
    private Date dateCommande;
    private String typePaiement;
    private String etat;
    private double total;
    private String Nom_Client;
    private int idUser;

    // Constructeurs
    public Commande() {}

    public Commande(int idCommande, int idClient, int idEmploye, Date dateCommande, String typePaiement, String etat, double total) {
        this.idCommande = idCommande;
        this.idClient = idClient;
        this.idEmploye = idEmploye;
        this.dateCommande = dateCommande;
        this.typePaiement = typePaiement;
        this.etat = etat;
        this.total = total;
    }
    
    /*public Commande(int idClient, int idUser, String etat, String typePaiement, double total) {
        this.idClient = idClient;
        this.idUser = idUser;
        this.etat = etat;
        this.typePaiement = typePaiement;
        this.total = total;
    }*/

    public Commande(int idClient, int idEmploye, String etat, String typePaiement, double total) {
        this.idClient = idClient;
        this.idEmploye = idEmploye;
        this.etat = etat;
        this.typePaiement = typePaiement;
        this.total = total;
    }



    // Getters et Setters
    public int getIdCommande() {
        return idCommande;
    }

    public void setIdCommande(int idCommande) {
        this.idCommande = idCommande;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public int getIdEmploye() {
        return idEmploye;
    }

    public void setIdEmploye(int idEmploye) {
        this.idEmploye = idEmploye;
    }

    public Date getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(Date dateCommande) {
        this.dateCommande = dateCommande;
    }

    public String getTypePaiement() {
        return typePaiement;
    }

    public void setTypePaiement(String typePaiement) {
        this.typePaiement = typePaiement;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
    
    public String getNomClient() {
        return Nom_Client;
    }

    public void setNomClient(String Nom_Client) {
        this.Nom_Client = Nom_Client;
    }
}
