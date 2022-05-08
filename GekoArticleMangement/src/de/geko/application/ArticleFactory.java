package de.geko.application;

import com.opencsv.CSVWriter;
import de.geko.persistence.Database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Ruwen Lamm
 * Manages Articles and their relation to other articles
 * Methodes to create CSV-files for ordersist and structure list based on one rootarticle
 */
public class ArticleFactory {

    private final String spacing = "\t";
    private final String[] defaultHeader = {"Artikelnummner", "Artikelname", "Anzahl", "Hinweis", "Lagerbestand", "Einheit", "Preis", "Einkaufspreis", "Preissumme", "Einkauspreissumme"};
    private Article rootArticle;
    private String indentation = "\t";
    private ArrayList<String[]> dataAll = new ArrayList<>();
    private double totalCostprice, totalPrice;
    private ArrayList<String> additionalInfo = new ArrayList<>();
    private boolean isRoot = true;
    private TreeMap<String, ArrayList<String>> deleteMap = new TreeMap<>();
    private HashMap<Integer, Article> orderItems = new HashMap<>();


    public ArticleFactory(Article rootArticle) {
        this.rootArticle = rootArticle;

    }

    public Article getRootArticle() {
        return rootArticle;
    }

    /**
     * TODO: Add COSTPRICE and TOTALPRICE for only all Subarticles
     */
    private void generateCSVFile(File csvFile) {
        try {


            FileWriter outputFile = new FileWriter(csvFile);
            CSVWriter writer = new CSVWriter(outputFile);
            writer.writeNext(getHeader());

            writer.writeNext(getColumn(rootArticle, 0));
            writer.writeAll(dataAll);
            writer.writeNext(new String[]{"", "", "", ""});
            writer.writeNext(new String[]{"", "", "", ""});
            writer.writeNext(new String[]{"Gesamtverkaufspreis", "Gesamteinkaufspreis"});
            writer.writeNext(new String[]{"" + Math.round(totalPrice * 100.0) / 100.0, "" + Math.round(totalCostprice * 100.0) / 100.0});
            writer.writeNext(new String[]{"", "", "", ""});
            writer.writeNext(new String[]{"", "", "", ""});
            writer.writeNext(new String[]{"Gesamtverkaufspreis Unterartikel", "Gesamteinkaufspreis Unterartikel"});
            writer.writeNext(new String[]{"" + Math.round((totalPrice - rootArticle.getPrice1()) * 100.0) / 100.0, "" + Math.round((totalCostprice - rootArticle.getCostPrice()) * 100.0) / 100.0});
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a csv file where all the articles and their sub articles are listed
     * The columns are based on the user input, but the default header and the corresponding values will always be generated
     *
     * @param csvFile
     */
    public void createHierarchyItemCSV(File csvFile) {
        dataAll.clear();
        addFirstLevelData();
        generateCSVFile(csvFile);


    }

    /**
     * Creates a csv file where all the articles are listed, if an article exists more than once the amount values of that article will be updated accordingly
     * The columns are based on the user input, but the default header and the corresponding values will always be generated
     *
     * @param csvFile
     */
    public void createOrderListCSV(File csvFile) {
        dataAll.clear();
        orderItems.clear();
        totalPrice = 0.0 + rootArticle.getPrice1();
        totalCostprice = 0.0 + rootArticle.getCostPrice();
        rootArticle.setAmount(1);

        fillOrderRecursive(rootArticle);
        for (Map.Entry<Integer, Article> set : orderItems.entrySet()) {
            totalCostprice += set.getValue().getCostPrice() * set.getValue().getAmount();
            totalPrice += set.getValue().getPrice1() * set.getValue().getAmount();
            dataAll.add(getColumn(set.getValue(), 3));
        }
        generateCSVFile(csvFile);


    }

    /**
     * Do recursive search to get all articles and their sub articles.
     * If an article occurs multiple times adjust the amount value in the hashmap
     * The hashmap therfore only lists only one article
     *
     * @param article
     */
    private void fillOrderRecursive(Article article) {
        for (Article a : article.getSubArticle()
        ) {
            if (orderItems.containsKey(a.hashCode())) {
                orderItems.get(a.hashCode()).setAmount(a.getAmount() + orderItems.get(a.hashCode()).getAmount());
            } else {
                orderItems.put(a.hashCode(), new Article(a));
            }
            if (a.getSubArticle().size() > 0) {
                fillOrderRecursive(a);
            }

        }

    }

    /**
     * get frist level articles, and do recursive search
     */
    private void addFirstLevelData() {

        rootArticle.setAmount(1);
        totalPrice = 0.0 + rootArticle.getPrice1();
        totalCostprice = 0.0 + rootArticle.getCostPrice();

        for (Article a : rootArticle.getSubArticle()) {

            totalPrice += a.getPrice1() * a.getAmount();
            totalCostprice += a.getCostPrice() * a.getAmount();

            dataAll.add(getColumn(a, 1));
            if (a.getSubArticle().size() > 0) {
                addRecursiveData(a);
            }
        }
    }

    /**
     * Do recursive search to get all articles and their sub articles.
     * calculate indentation for each article that illustrates the parent and child relation
     *
     * @param article
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
     * returns the right formatted column for the csv list
     *
     * @param article   article
     * @param treeLevel 0: root element, 1:first level, 2:below first level, 3: OrderList
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
        if (treeLevel == 3) index = "";

        String[] column = new String[defaultHeader.length + additionalInfo.size()];
        column[0] = index + article.getArticleID();
        column[1] = article.getName() + "";
        column[2] = article.getAmount() + "";
        column[3] = handelNullString(article.getSubArticleDescription());
        column[4] = article.getQuantity() + "";
        column[5] = article.getQuantityUnit();
        column[6] = article.getPrice1() + "";
        column[7] = article.getCostPrice() + "";
        column[8] = Math.round(article.getTotalPrice() * 100.0) / 100.0 + "";
        column[9] = Math.round(article.getTotalCostPrice() * 100.0) / 100.0 + "";


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

