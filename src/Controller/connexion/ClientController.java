/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller.connexion;

import Modele.Client;
import Modele.Adresse;
import crud.ClientsCRUD;
import java.sql.SQLException;
import java.util.List;
/**
 *
 * @author Carmelle Adou
 */
public class ClientController {
    
    
    private ClientsCRUD clientsCRUD;

    public ClientController() {
        this.clientsCRUD = new ClientsCRUD();
    }

    /**
     * Vérifie si les champs d'un client sont valides avant l'ajout/modification.
     */
    private boolean validerClient(Client client) {
        if (client.getNom().isEmpty() || client.getPrenom().isEmpty() || client.getEmail().isEmpty() || client.getTelephone().isEmpty()) {
            return false;
        }

        Adresse adresse = client.getAdresse();
        if (adresse == null || adresse.getRue().isEmpty() || adresse.getVille().isEmpty() || adresse.getCodePostal().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Ajoute un client après validation.
     */
    public boolean ajouterClient(Client client) throws SQLException, ClassNotFoundException {
        if (!validerClient(client)) {
            throw new IllegalArgumentException("Tous les champs doivent être remplis !");
        }

        // Vérifier si un client avec cet email existe déjà
        List<Client> clientsExistants = clientsCRUD.getAllClients();
        for (Client c : clientsExistants) {
            if (c.getEmail().equalsIgnoreCase(client.getEmail())) {
                throw new IllegalArgumentException("Cet email est déjà utilisé !");
            }
        }

        return clientsCRUD.ajouterClient(client);
    }

    /**
     * Récupère un client par son ID.
     */
    public Client getClientById(int idClient) throws SQLException, ClassNotFoundException {
        return clientsCRUD.getClientById(idClient);
    }

    /**
     * Met à jour un client après validation.
     */
    public boolean modifierClient(Client client) throws SQLException, ClassNotFoundException {
        if (!validerClient(client)) {
            throw new IllegalArgumentException("Tous les champs doivent être remplis !");
        }

        return clientsCRUD.modifierClient(client);
    }

    /**
     * Archive un client (RGPD).
     */
    public boolean archiverClient(int idClient) throws SQLException, ClassNotFoundException {
        return clientsCRUD.archiverClient(idClient);
    }

    /**
     * Récupère tous les clients actifs (non archivés).
     */
    public List<Client> getAllClients() throws SQLException, ClassNotFoundException {
        return clientsCRUD.getAllClients();
    }
    
}


