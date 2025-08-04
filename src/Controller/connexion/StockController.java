/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller.connexion;

import Modele.Stocks;
import crud.StockCRUD;
import java.sql.SQLException;
import java.util.List;
import java.sql.Connection;

/**
 *
 * @author Carmelle Adou
 */
public class StockController {
   
    private final StockCRUD stocksCRUD;

    
    public StockController() {
        this.stocksCRUD = new StockCRUD();
    }

    
    public List<Stocks> getAllStocks() throws SQLException, ClassNotFoundException {
        return stocksCRUD.getAllStocks();
    }

   
    public Stocks getStockById(int idStock) throws SQLException, ClassNotFoundException {
        return stocksCRUD.getStockById(idStock);
    }

    
    public boolean ajouterStock(Stocks stock) throws SQLException, ClassNotFoundException {
        return stocksCRUD.ajouterStock(stock);
    }

    
    public boolean modifierStock(Stocks stock) throws SQLException, ClassNotFoundException {
        return stocksCRUD.modifierStock(stock);
    }

    
    public boolean desactiverStock(int idStock) throws SQLException, ClassNotFoundException {
        return stocksCRUD.desactiverStock(idStock);
    }

}


