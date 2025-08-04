/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modele;

/**
 *
 * @author Carmelle Adou
 */
public class Adresse {
    
    private int idAdresse;
    private String rue;
    private String ville;
    private String codePostal;

    public Adresse(int idAdresse, String rue, String ville, String codePostal) {
        this.idAdresse = idAdresse;
        this.rue = rue;
        this.ville = ville;
        this.codePostal = codePostal;
    }

    public int getIdAdresse() { 
        return idAdresse;
    }
    public String getRue() { 
        return rue; 
    }
    public String getVille() { 
        return ville; 
    }
    public String getCodePostal() {
        return codePostal;
    }

    public void setRue(String rue) { 
        this.rue = rue;
    }
    public void setVille(String ville) {
        this.ville = ville; 
    }
    public void setCodePostal(String codePostal) { 
        this.codePostal = codePostal; 
    }

    public void setIdAdresse(int idAdresse) {
        this.idAdresse = idAdresse;
    }
    
}
