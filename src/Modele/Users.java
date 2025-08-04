package Modele;

import java.util.List;


public class Users {

    private int idUser;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;  // stocké en clair dans l'objet *après* lecture ou saisie
    private String role;
    private List<Privileges> privileges; 
    private String titre;
    private String nomEntreprise;
    
    // ✅ Instance statique pour la session
    private static Users userSession = null;


    // Constructeur
    public Users(int idUser, String nom, String prenom, String email, String motDePasse) {
        this.idUser = idUser;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse; // ici, en clair dans l'objet
        this.role = role;
    }
    
    // ✅ Démarrer une session utilisateur
    public static void startSession(Users user) {
        userSession = user;
    }

    // ✅ Récupérer l'utilisateur connecté
    public static Users getSession() {
        return userSession;
    }

    // ✅ Vérifier si un utilisateur est connecté
    public static boolean isLoggedIn() {
        return userSession != null;
    }

    // ✅ Déconnexion
    public static void logout() {
        userSession = null;
    }


    // Autres constructeurs vides ou partiels si besoin
    public Users() { }

    // Getters / Setters simples
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse; // déjà en clair dans l'objet
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse; // stocke en clair
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Gestion des privilèges
    public List<Privileges> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privileges> privileges) {
        this.privileges = privileges;
    }

    public void afficherPrivileges() {
        if (privileges == null || privileges.isEmpty()) {
            System.out.println("Aucun privilège attribué.");
        } else {
            System.out.println("Privilèges : " + privileges);
        }
    }

    // Champs liés à l'Employé
    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getNomEntreprise() {
        return nomEntreprise;
    }

    public void setNomEntreprise(String nomEntreprise) {
        this.nomEntreprise = nomEntreprise;
    }

    /**
     * Vérifie si le mot de passe saisi correspond au mot de passe stocké (en clair dans l'objet).
     * @param motDePasseSaisi Mot de passe saisi par l'utilisateur
     * @return true si correspond, false sinon
     */
    public boolean verifyPassword(String motDePasseSaisi) {
        // Ici, 'motDePasse' est déjà en clair dans l'objet
        return motDePasseSaisi != null && motDePasseSaisi.equals(this.motDePasse);
    }
}
