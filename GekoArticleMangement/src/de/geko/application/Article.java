package de.geko.application;

import de.geko.persistence.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;

/**
 * @author Ruwen Lamm
 * Abstract model of product from database
 * Only contains subset of actual attributes from database table(FKT_PRODUCT)
 */
public class Article {
    private final String articleID;
    private double amount;
    private ArrayList<Article> subArticle = new ArrayList<>();
    private double price1, price2, price3, price4, price5, costPrice, weight;
    private double quantity;
    private String quantityUnit, name, description, note, supplierNumber, modifiedBy, category, subArticleDescription;
    private Date dateAdded, dateModified;
    private long categoryID;

    public Article(Article article) {
        this.articleID = article.articleID;
        this.amount = article.amount;
        this.subArticle = article.subArticle;
        this.price1 = article.price1;
        this.price2 = article.price2;
        this.price3 = article.price3;
        this.price4 = article.price4;
        this.price5 = article.price5;
        this.costPrice = article.costPrice;
        this.weight = article.weight;
        this.quantity = article.quantity;
        this.quantityUnit = article.quantityUnit;
        this.name = article.name;
        this.description = article.description;
        this.note = article.note;
        this.supplierNumber = article.supplierNumber;
        this.modifiedBy = article.modifiedBy;
        this.category = article.category;
        this.subArticleDescription = article.subArticleDescription;
        this.dateAdded = article.dateAdded;
        this.dateModified = article.dateModified;
        this.categoryID = article.categoryID;
    }

    public Article(String articleID) {
        this.articleID = articleID;
        getAllAttributes();
    }

    /**
     * creates new article
     *
     * @param articleID
     * @param amount
     * @param subArticleDescription
     */
    public Article(String articleID, double amount, String subArticleDescription) {
        this.articleID = articleID;
        this.subArticleDescription = subArticleDescription;
        setAmount(amount);
        getAllAttributes();
    }

    /**
     * retrieves and sets all attributes od object
     */
    private void getAllAttributes() {
        Connection conn = ConnectionManager.getConnection();
        String query = "SELECT price1,price2,price3,price4,price5,quantity,quantityunit,NAME,COSTPRICE,DESCRIPTION,NOTE,SUPPLIERITEMNUMBER,MODIFIEDBY,WEIGHT,FK_CATEGORY FROM FKT_PRODUCT WHERE ITEMNUMBER = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, articleID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                this.price1 = rs.getDouble(1);
                this.price2 = rs.getDouble(2);
                this.price3 = rs.getDouble(3);
                this.price4 = rs.getDouble(4);
                this.price5 = rs.getDouble(5);
                this.quantity = rs.getDouble(6);
                this.quantityUnit = rs.getString(7);
                this.name = rs.getString(8);
                this.costPrice = rs.getDouble(9);
                this.description = rs.getString(10);
                this.note = rs.getString(11);
                this.supplierNumber = rs.getString(12);
                this.modifiedBy = rs.getString(13);
                this.weight = rs.getDouble(14);
                setCategory(rs.getInt(15));


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getSubArticleDescription() {
        return subArticleDescription;
    }

    public void setSubArticleDescription(String subArticleDescription) {
        this.subArticleDescription = subArticleDescription;
    }

    public void addSubItem(Article sub) {
        subArticle.add(sub);
    }

    public ArrayList<Article> getSubArticle() {
        return subArticle;
    }

    public String getArticleID() {
        return articleID;
    }

    public double getPrice1() {
        return price1;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public String getDescription() {
        return description;
    }

    public String getNote() {
        return note;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPrice2() {
        return price2;
    }

    public double getPrice3() {
        return price3;
    }

    public double getPrice4() {
        return price4;
    }

    public double getPrice5() {
        return price5;
    }

    public double getWeight() {
        return weight;
    }

    public String getSupplierNumber() {
        return supplierNumber;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public String getCategory() {
        return category;
    }

    private void setCategory(long categoryId) {
        Connection conn = ConnectionManager.getConnection();
        String query = "SELECT NAME FROM FKT_CATEGORY WHERE ID=?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                this.category = rs.getString(1);
                this.categoryID = categoryId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void updateArticle() {
        Connection conn = ConnectionManager.getConnection();
        String sql = "UPDATE FKT_PRODUCT SET price1 = ? ,price2 = ?,price3 = ?,price4 = ?,price5 = ?,quantity = ?,quantityunit = ?,NAME = ?,COSTPRICE = ?,DESCRIPTION = ?,NOTE = ?,SUPPLIERITEMNUMBER = ?,MODIFIEDBY = ?,WEIGHT = ?,FK_CATEGORY = ? WHERE ITEMNUMBER = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {


            statement.setDouble(1, price1);
            statement.setDouble(2, price2);
            statement.setDouble(3, price3);
            statement.setDouble(4, price4);
            statement.setDouble(5, price5);
            statement.setDouble(6, quantity);
            statement.setString(7, quantityUnit);
            statement.setString(8, name);
            statement.setDouble(9, costPrice);
            statement.setString(10, description);
            statement.setString(11, note);
            statement.setString(12, supplierNumber);
            statement.setString(13, modifiedBy);
            statement.setDouble(14, weight);
            statement.setLong(15, categoryID);
            statement.setString(16, articleID);

            statement.execute();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public double getTotalCostPrice() {
        return amount * costPrice;
    }

    public double getTotalPrice() {
        return amount * price1;
    }
}
