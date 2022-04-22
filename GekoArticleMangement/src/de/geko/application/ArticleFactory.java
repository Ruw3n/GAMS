package de.geko.application;

import com.opencsv.CSVWriter;
import de.geko.persistence.Database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Ruwen Lamm
 * Creates a csv file based on recursive articles with subArticles
 */
public class ArticleFactory {

    private final String spacing = "\t";
    String[] defaultHeader = {"Artikelnummner", "Artikelname", "Anzahl", "Hinweis", "Lagerbestand", "Einheit", "Preis", "Einkaufspreis"};
    private Article rootArticle;
    private String indentation = "\t";
    private ArrayList<String[]> dataAll = new ArrayList<>();
    private String[] rowdata = new String[2];
    private double totalCostprice, totalPrice;
    private ArrayList<String> additionalInfo = new ArrayList<>();
    private boolean isRoot = true;
    private TreeMap<String, ArrayList<String>> deleteMap = new TreeMap<>();

    /**
     * Constructs ArticleFactory
     *
     * @param rootArticle root Article, which contains recursive Articles
     */
    public ArticleFactory(Article rootArticle) {
        this.rootArticle = rootArticle;

    }

    public Article getRootArticle() {
        return rootArticle;
    }

    /**
     * creates a cvs file
     *
     * @param csvFile location/name for csv-file
     */
    public void createCSVFile(File csvFile) {

        try {


            FileWriter outputFile = new FileWriter(csvFile);
            CSVWriter writer = new CSVWriter(outputFile);
            writer.writeNext(getHeader());
            addFirstLevelData();
            writer.writeNext(getColumn(rootArticle, 0));

            writer.writeAll(dataAll);
            writer.writeNext(new String[]{"", "", "", ""});
            writer.writeNext(new String[]{"", "", "", ""});
            writer.writeNext(new String[]{"Gesamtverkaufspreis", "Gesamteinkaufspreis"});
            writer.writeNext(new String[]{"" + Math.round(totalPrice * 100.0) / 100.0, "" + Math.round(totalCostprice * 100.0) / 100.0});
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Adds first level Articles to cvs-file
     * Therefore only subArticles of root Article
     */
    private void addFirstLevelData() {
        totalCostprice = 0.0;
        totalPrice = 0.0;
        rootArticle.setAmount(1);

        for (Article a : rootArticle.getSubArticle()) {
            String desc;
            if (a.getSubArticleDescription() == null) {
                desc = "";
            } else {
                desc = a.getSubArticleDescription();
            }
            a.setSubArticleDescription(desc);
            totalPrice += a.getPrice1() * a.getAmount();
            totalCostprice += a.getCostPrice() * a.getAmount();

            dataAll.add(getColumn(a, 1));
            if (a.getSubArticle().size() > 0) {
                addRecursiveData(a);
            }
        }
    }

    /**
     * adds recursive SubArticles to data
     *
     * @param article Article
     */
    private void addRecursiveData(Article article) {
        int newLength;
        for (Article a : article.getSubArticle()) {
            totalPrice += a.getPrice1() * a.getAmount();
            totalCostprice += a.getCostPrice() * a.getAmount();

            dataAll.add(getColumn(a, 2));
            if (a.getSubArticle().size() > 0) {
                indentation += spacing;
                addRecursiveData(a);
                newLength = indentation.length() - spacing.length();
                if (newLength > 0) indentation = indentation.substring(0, newLength);
            }


        }
    }

    /**
     * @param size of indentation
     * @return indentation lines
     */
    private String getLines(int size) {
        String res = "";
        for (int i = 0; i < size; i++) {
            res += "----";
        }
        return res;
    }

    /**
     * sets additional Headers for csv-file
     *
     * @param additionalInfo additional Information about an article
     */
    public void setAdditionalInfo(ArrayList<String> additionalInfo) {
        this.additionalInfo.addAll(additionalInfo);
    }

    /**
     * Creates Header for CSV dynamically based on user additional info
     * adds additional info to default header
     *
     * @return header for CSV-file
     */
    private String[] getHeader() {

        String[] header = new String[defaultHeader.length + additionalInfo.size()];
        for (int i = 0; i < defaultHeader.length + (additionalInfo.size()); i++) {
            if (i < defaultHeader.length) header[i] = defaultHeader[i];
            else header[i] = additionalInfo.get(i - defaultHeader.length);
        }


        return header;
    }

    /**
     * @param article   article
     * @param treeLevel 0: root element, 1:first level, 2:below first level
     * @return String[] data for csv-file column(article data)
     */
    private String[] getColumn(Article article, int treeLevel) {

        String index = "";
        if (treeLevel == 0) {
            index = "Bauteilgruppe: ";
            article.setSubArticleDescription("-");
        }
        if (treeLevel == 1) index = "----";
        if (treeLevel == 2) index = "----" + getLines(indentation.length());

        String[] column = new String[defaultHeader.length + additionalInfo.size()];
        column[0] = index + article.getArticleID();
        column[1] = article.getName() + "";
        column[2] = article.getAmount() + "";
        column[3] = article.getSubArticleDescription();
        column[4] = article.getQuantity() + "";
        column[5] = article.getQuantityUnit();
        column[6] = article.getPrice1() + "";
        column[7] = article.getCostPrice() + "";

        String s;

        for (int i = defaultHeader.length; i < column.length; i++) {
            s = additionalInfo.get(i - defaultHeader.length);

            switch (s) {
                case "Hinzugefuegt am" -> column[i] = handelNullString(article.getDateAdded());
                case "Beschreibung" -> column[i] = handelNullString(article.getDescription());
                case "Geaendert am" -> column[i] = handelNullString(article.getDateModified());
                case "Geaendert von" -> column[i] = handelNullString(article.getModifiedBy());
                case "Gewicht" -> column[i] = article.getWeight() + "";
                case "Kategorie" -> column[i] = handelNullString(article.getCategory());
                case "Lieferantennummer" -> column[i] = handelNullString(article.getSupplierNumber());
                case "Notiz" -> column[i] = handelNullString(article.getNote());
            }


        }


        return column;
    }

    private String handelNullString(Object s) {
        if (s == null) return "";
        else return s.toString();
    }

    public void removeFromInventory(Article rootArticle) {
        if (isRoot) {
            isRoot = false;
            for (Article article : rootArticle.getSubArticle()
            ) {
                removeFromInventory(article);
            }
        } else {

            rootArticle.setQuantity(rootArticle.getQuantity() - rootArticle.getAmount());
            rootArticle.updateArticle();
            for (Article article : rootArticle.getSubArticle()
            ) {
                removeFromInventory(article);

            }
        }

    }

    private void fillDeleteMap(Article rootArticle) {
        ArrayList<String> arrayList = new ArrayList<>();

        for (Article article : rootArticle.getSubArticle()) {
            arrayList.add(article.getArticleID());
            if (article.getSubArticle().size() > 0) {
                fillDeleteMap(article);
            }
        }
        deleteMap.put(rootArticle.getArticleID(), arrayList);
    }

    public void deleteArticle() {
        deleteMap.clear();
        fillDeleteMap(rootArticle);
        for (Map.Entry<String, ArrayList<String>> entry : deleteMap.entrySet()
        ) {
            for (String s : entry.getValue()
            ) {
                Database.deleteSubProduct(entry.getKey(), s);
            }
        }
    }

}

