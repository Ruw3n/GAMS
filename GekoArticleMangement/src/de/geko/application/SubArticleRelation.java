package de.geko.application;

public class SubArticleRelation {
    private String description;
    private String itemNumber;
    private String subItemNumber;
    private double amount;
    private int layer;

    public SubArticleRelation(String description, String itemNumber, String subItemNumber, double amount, int layer) {
        this.description = description;
        this.itemNumber = itemNumber;
        this.subItemNumber = subItemNumber;
        this.amount = amount;
        this.layer = layer;
    }

    public SubArticleRelation() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getSubItemNumber() {
        return subItemNumber;
    }

    public void setSubItemNumber(String subItemNumber) {
        this.subItemNumber = subItemNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }
}
