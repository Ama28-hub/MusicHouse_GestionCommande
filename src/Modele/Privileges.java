/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modele;

/**
 *
 * @author Carmelle Adou
 */
public class Privileges {
    private int idPrivilege;
    private String description;
    private String niveauAcces;

    public Privileges() { }

    public Privileges(int idPrivilege, String description, String niveauAcces) {
        this.idPrivilege = idPrivilege;
        this.description = description;
        this.niveauAcces = niveauAcces;
    }

    // Getters et Setters
    public int getIdPrivilege() {
        return idPrivilege;
    }

    public void setIdPrivilege(int idPrivilege) {
        this.idPrivilege = idPrivilege;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNiveauAcces() {
        return niveauAcces;
    }

    public void setNiveauAcces(String niveauAcces) {
        this.niveauAcces = niveauAcces;
    }
    
}

