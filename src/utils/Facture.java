package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Classe permettant de générer une facture PDF
 * @author Carmelle Adou
 */
public class Facture {

    private String filePath;
    private int idCommande;
    private String dateEmission;
    private int idEmploye;
    private int idClient; // ✅ Ajout de l'ID du client
    private List<Integer> idStocks;
    private List<String> articles;
    private List<Integer> quantites;
    private List<Double> prixUnitaires;
    private List<Double> tauxReductions; // ✅ Liste des réductions appliquées
    private double total;

    /**
     * ✅ Constructeur modifié pour inclure les réductions et éviter NullPointerException
     */
    public Facture(String filePath, int idCommande, String dateEmission, int idEmploye, int idClient, 
                   List<Integer> idStocks, List<String> articles, List<Integer> quantites, 
                   List<Double> prixUnitaires, List<Double> tauxReductions, double total) {
        this.filePath = filePath;
        this.idCommande = idCommande;
        this.dateEmission = dateEmission;
        this.idEmploye = idEmploye;
        this.idClient = idClient;
        this.idStocks = idStocks;
        this.articles = articles;
        this.quantites = quantites;
        this.prixUnitaires = prixUnitaires;
        this.tauxReductions = (tauxReductions != null) ? tauxReductions : List.of(); // ✅ Évite NullPointerException
        this.total = total;
    }

    /**
     * ✅ Génération du PDF
     */
    public void generatePDF() throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // ✅ Ajouter un titre
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLUE);
            Paragraph title = new Paragraph("Facture", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // ✅ Ajouter les détails de la commande
            document.add(new Paragraph("ID Commande : " + idCommande));
            document.add(new Paragraph("Date d'émission : " + dateEmission));
            document.add(new Paragraph("ID Employé : " + idEmploye));
            document.add(new Paragraph("ID Client : " + idClient)); // ✅ Ajout de l'ID Client
            document.add(new Paragraph("\n"));

            // ✅ Ajouter le tableau des articles
            PdfPTable table = new PdfPTable(5); // 5 colonnes : ID Stock, Article, Quantité, Prix Unitaire, Réduction
            table.setWidthPercentage(100);

            // ✅ Ajouter l'en-tête du tableau
            PdfPCell cell;
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            BaseColor headerColor = BaseColor.DARK_GRAY;

            String[] headers = {"ID Stock", "Article", "Quantité", "Prix Unitaire (€)", "Réduction (%)"};
            for (String header : headers) {
                cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // ✅ Ajouter les données des articles (sans la colonne Prix Total)
            for (int i = 0; i < articles.size(); i++) {
                double reduction = (tauxReductions.size() > i) ? tauxReductions.get(i) : 0; // ✅ Évite IndexOutOfBoundsException
                double prixUnitaireApresReduction = prixUnitaires.get(i) * (1 - (reduction / 100));

                table.addCell(String.valueOf(idStocks.get(i))); // ID Stock
                table.addCell(articles.get(i)); // Nom de l'article
                table.addCell(String.valueOf(quantites.get(i))); // Quantité
                table.addCell(String.format("%.2f", prixUnitaires.get(i))); // Prix unitaire avant réduction
                table.addCell(String.format("%.1f", reduction)); // ✅ Affichage du taux de réduction
            }

            document.add(table);
            document.add(new Paragraph("\n"));

            // ✅ Ajouter le total général
            Font totalFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Paragraph totalParagraph = new Paragraph("Total : " + String.format("%.2f €", total), totalFont);
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalParagraph);

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
