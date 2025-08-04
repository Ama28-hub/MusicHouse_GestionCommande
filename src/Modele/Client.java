package Modele;

import java.util.Date; 

/**
 *
 * @author Carmelle Adou
 */
public class Client {
   
    private int idClient;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Adresse adresse;
    private boolean archive; // Pour respecter le RGPD
    private Date dateNaissance;

    // Constructeur avec tous les paramètres
    public Client(int idClient, String nom, String prenom, String email, String telephone, Adresse adresse, Date dateNaissance1) {
        this.idClient = idClient;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.archive = false; // Par défaut, le client n'est pas archivé
        this.dateNaissance = dateNaissance;
    }

    // Getters
    public int getIdClient() { 
        return idClient; 
    }
    
    public String getNom() { 
        return nom;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getTelephone() {
        return telephone; 
    }
    
    public Adresse getAdresse() { 
        return adresse; 
    }

    public boolean isArchive() { 
        return archive;
    }

    public Date getDateNaissance() { 
        return dateNaissance; 
    } // ✅ Ajout du getter

    // Setters
    public void setNom(String nom) { 
        this.nom = nom;
    }
    
    public void setPrenom(String prenom) { 
        this.prenom = prenom; 
    }
    
    public void setEmail(String email) { 
        this.email = email;
    }
    
    public void setTelephone(String telephone) { 
        this.telephone = telephone; 
    }
    
    public void setAdresse(Adresse adresse) {
        this.adresse = adresse; 
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }
    
    public void setArchive(boolean archive) { 
        this.archive = archive;
    }

    public void setDateNaissance(Date dateNaissance) { 
        this.dateNaissance = dateNaissance; 
    } // ✅ Ajout du setter
}
