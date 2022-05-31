package de.geko.gui;

import de.geko.application.*;
import de.geko.persistence.Database;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * @author Ruwen Lamm
 * Controlles fxml page and delegates action listener
 */
public class DialogController implements Initializable {

    @FXML
    public Pane rootPane;
    @FXML
    public TextField productSe;
    @FXML
    protected Button orderBtn;
    @FXML
    protected TextField itemnumber;
    @FXML
    protected Label errorText;
    @FXML
    protected Label errorTextInv;
    @FXML
    protected GridPane addInfoGrid;
    @FXML
    protected TextField itemnumberInv;
    @FXML
    protected TextField itemnumberGr;
    @FXML
    protected Label errorTextGr;
    @FXML
    protected TextField subItemnumberGr;
    @FXML
    protected TextField amountGr;
    @FXML
    protected TextArea descGr;
    @FXML
    protected Button addSubGr;
    @FXML
    protected Button deleteSubGr;
    @FXML
    protected Label successTextGr;
    @FXML
    protected Label successTextInv;
    @FXML
    protected Pane confPane;
    @FXML
    protected Button abortBtn;
    @FXML
    protected Button contBtn;
    @FXML
    protected ChoiceBox<String> categorySe;
    @FXML
    protected TableView tableSe;
    @FXML
    protected Button structureBtn;
    @FXML
    protected Label subItemName;
    @FXML
    protected Label itemName;
    @FXML
    protected TableView tableInv;
    @FXML
    protected Spinner<Integer> layerGr;
    @FXML
    protected Spinner<Integer> layerCSV;

    private ArrayList<Category> categories;


    private void createCSV(CSVTypes type) {

        LocalDate date = LocalDate.now();

        ArrayList<String> addInfo = new ArrayList<>();

        for (Node test : addInfoGrid.getChildren()) {
            CheckBox cb = (CheckBox) test;
            if (cb.isSelected()) addInfo.add(cb.getText());
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Erstelle CSV-Datei");
        fileChooser.setInitialFileName(type.name() + "_btg_" + itemnumber.getText() + "_" + date + ".csv");
        File f;
        f = fileChooser.showSaveDialog(CurrentWindow.getInstance().getCurrStage());
        long start = ZonedDateTime.now().toInstant().toEpochMilli();
        //Article rootArticle = Database.getArticleTreeRoot(itemnumber.getText());
        Article rootArticle = Database.getArticleTreeRootFast(itemnumber.getText());
        System.out.println(rootArticle.getSubArticle().size());
        long end = ZonedDateTime.now().toInstant().toEpochMilli();
        System.out.println(end - start + " milliseconds to fetch from database");
        int layer = layerCSV.getValue();
        if (rootArticle == null) {
            errorText.setText("Bauteilgruppe existiert nicht!");
        } else {
            errorText.setText("");
            ArticleFactory articleFactory = new ArticleFactory(rootArticle);
            articleFactory.setAdditionalInfo(addInfo);
            if (type == CSVTypes.structure_list) {
                articleFactory.createHierarchyItemCSV(f, layer);
            } else if (type == CSVTypes.order_list) {
                articleFactory.createOrderListCSV(f, layer);
            }
        }
    }

    @FXML
    public void createOrderCSV() {
        createCSV(CSVTypes.order_list);
    }

    /**
     * Creates csv-file
     */
    @FXML
    public void createStructureCSV() {
        createCSV(CSVTypes.structure_list);

    }

    public void addSubArticle() {

        boolean ok;
        successTextGr.setText("");
        errorTextGr.setText("");
        double amount = 0.0;
        String itemId = itemnumberGr.getText();
        String subItemId = subItemnumberGr.getText();
        String desc = descGr.getText();
        int layer = layerGr.getValue();
        if (itemId.equals("") || subItemId.equals("")) {
            errorTextGr.setText("Bitte beide Felder\n(Bauteilgruppe und Unterartikel) ausfüllen.");
        } else {
            try {
                amount = Double.parseDouble(amountGr.getText());
                ok = true;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                errorTextGr.setText("Falsches Format für die Menge, nutze: x.x");
                ok = false;

            }
            try {
                if (ok) {
                    Database.addSubProduct(itemId, subItemId, amount, desc, layer);

                    successTextGr.setText("Unterartikel: " + subItemId + "\nwurde erfolgreich der Bauteilgruppe: " + itemId + " hinzugefügt.");
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                errorTextGr.setText(e.getMessage());
            }


        }


    }

    @FXML
    public void removeInventory() {
        Article rootArticle = Database.getArticleTreeRoot(itemnumberInv.getText());
        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        System.out.println(itemnumberInv.getText());

        if (rootArticle == null) {
            errorTextInv.setText("Bauteilgruppe existiert nicht!");
        } else {
            tableInv.setVisible(true);

            ArticleFactory articleFactory = new ArticleFactory(rootArticle);
            articleFactory.removeFromInventory(rootArticle);

            for (Article a : articleFactory.getArticlesInvLessZero()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(a.getArticleID());
                row.add(a.getName());
                row.add(a.getQuantity() + "");
                data.add(row);
            }
            tableInv.setItems(data);

            successTextInv.setText("Lagerbestände der Bauteilgruppe: " + itemnumberInv.getText() + " erfolgreich abgezogen.\n Alle Bauteile, welche nun einen Lagebestand <0 werden nun in der Tabelle angezeigt");

        }


    }

    @FXML
    public void removeSubArticle() {
        String subItemId = subItemnumberGr.getText();
        String itemId = itemnumberGr.getText();
        successTextGr.setText("");
        errorTextGr.setText("");
        if (itemId.equals("") || subItemId.equals("")) {
            errorTextGr.setText("Bitte beide Felder\n (Bauteilgruppe und Unterartikel) ausfüllen.");

        } else {

            try {
                Database.deleteSubProduct(itemId, subItemId);
                successTextGr.setText("Aus\nder Bauteilgruppe: " + itemId + "wurde der Unterartikel " + subItemId + " erfolgriech entfernt.");

            } catch (Exception e) {
                e.printStackTrace();
                errorTextGr.setText(e.getMessage());
            }


        }

    }

    @FXML
    public void updateSubArticle() {

        boolean ok;
        double amount = 0.0;
        String subItemId = subItemnumberGr.getText();
        String itemId = itemnumberGr.getText();
        String desc = descGr.getText();
        int layer = layerGr.getValue();
        successTextGr.setText("");
        errorTextGr.setText("");
        if (itemId.equals("") || subItemId.equals("")) {
            errorTextGr.setText("Bitte beide Felder\n(Bauteilgruppe und Unterartikel) ausfüllen.");
        } else {
            try {
                amount = Double.parseDouble(amountGr.getText());
                ok = true;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                errorTextGr.setText("Falsches Format für die Menge, nutze: x.x");
                ok = false;

            }
            if (ok) {
                try {
                    Database.updateSubArticle(itemId, subItemId, amount, desc, layer);
                    successTextGr.setText("Unterartikel erfolreich bearbeitet.");
                } catch (Exception e) {
                    e.printStackTrace();
                    errorTextGr.setText(e.getMessage());
                }
            }

        }
    }

    @FXML
    public void removeWholeArticle() {
        confPane.setVisible(true);
        abortBtn.setOnAction(actionEvent -> confPane.setVisible(false));
        contBtn.setOnAction(actionEvent -> {
            confPane.setVisible(false);
            String itemNumber = itemnumberGr.getText();
            errorTextGr.setText("");
            successTextGr.setText("");
            if (itemNumber.equals("")) {
                errorTextGr.setText("Bitte Bauteilgruppen-ID eingeben.");
            } else {
                Article root = Database.getArticleTreeRoot(itemnumberGr.getText());
                if (root == null) {
                    errorTextGr.setText("Bauteilgruppe existiert nicht");
                } else {
                    ArticleFactory articleFactory = new ArticleFactory(root);
                    articleFactory.deleteArticle();
                    successTextGr.setText("Bauteilgruppe entfernt.");
                }

            }
        });
    }


    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        SpinnerValueFactory spinnerValueFactoryGr = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, 4, -1);
        SpinnerValueFactory spinnerValueFactoryCSV = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, 4, -1);

        layerGr.setValueFactory(spinnerValueFactoryGr);
        layerCSV.setValueFactory(spinnerValueFactoryCSV);
        tableInv.setVisible(true);
        tableInv.setEditable(true);
        categories = Database.getAllCategories();
        categories.sort(new CategoryComp());
        ObservableList<String> categoryNames = FXCollections.observableArrayList();
        for (Category c : categories) {
            categoryNames.add(c.getName());
        }
        categorySe.setItems(categoryNames);

        int i = 0;
        ArrayList<String> columNamesSearch = new ArrayList<>();
        columNamesSearch.add("BauteilID");
        columNamesSearch.add("Name");
        columNamesSearch.add("Beschreibung");
        columNamesSearch.add("Kategorie");
        columNamesSearch.add("Anzahl");
        columNamesSearch.add("Einheit");

        ArrayList<String> columNamesInv = new ArrayList<>();
        columNamesInv.add("BauteilID");
        columNamesInv.add("Name");
        columNamesInv.add("Neuer Lagerbestand");


        for (String s : columNamesSearch) {
            final int j = i;

            TableColumn tc = new TableColumn(s);
            tc.setCellValueFactory((Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> new SimpleStringProperty(param.getValue().get(j).toString()));
            tableSe.getColumns().add(tc);
            if (i > 1)
                tc.setEditable(true);
            i++;
        }
        tableSe.setEditable(true);
        i = 0;
        for (String s : columNamesInv) {
            final int j = i;

            TableColumn tc = new TableColumn(s);
            tc.setCellValueFactory((Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> new SimpleStringProperty(param.getValue().get(j).toString()));
            tableInv.getColumns().add(tc);
            if (i > 1)
                tc.setEditable(true);
            i++;
        }
        tableInv.setEditable(false);


    }

    @FXML
    public void search() {
        ObservableList<ObservableList> data = FXCollections.observableArrayList();

        if (!productSe.getText().equals("")) {
            ObservableList<String> row = FXCollections.observableArrayList();
            if (Database.doesArticleExist(productSe.getText())) {

                Article article = new Article(productSe.getText());
                row.add(article.getArticleID() + "");
                row.add(article.getName() + "");
                row.add(article.getDescription() + "");
                row.add(article.getCategory() + "");
                row.add(article.getQuantity() + "");
                row.add(article.getQuantityUnit() + "");
                data.add(row);
            }

        } else {

            ArrayList<Long> ids = getIdsOfCategory(categorySe.getSelectionModel().getSelectedItem());
            ArrayList<Article> articles;
            articles = (Database.searchArticlesByCategory(ids.get(0)));


            for (Article article : articles) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(article.getArticleID() + "");
                row.add(article.getName() + "");
                row.add(article.getDescription() + "");
                row.add(article.getCategory() + "");
                row.add(article.getQuantity() + "");
                row.add(article.getQuantityUnit() + "");
                data.add(row);
            }
        }
        tableSe.setItems(data);

    }

    private ArrayList<Long> getIdsOfCategory(String categoryName) {
        ArrayList<Long> ids = new ArrayList<>();
        for (Category c : categories) {
            if (c.getName().equals(categoryName)) {
                ids.add(c.getId());

            }
        }
        return ids;
    }

    @FXML
    public void fillGrForm() {
        boolean showSub = false;
        amountGr.setText("");
        descGr.setText("");

        if (!itemnumberGr.getText().equals("")) {
            Article article = new Article(itemnumberGr.getText());
            if (article.getName() != null) {
                itemName.setText(article.getName());
                showSub = true;
            } else {
                itemName.setText("");
            }
        }
        if (!subItemnumberGr.getText().equals("")) {
            Article subArticle = new Article(subItemnumberGr.getText());
            if (subArticle.getName() != null) {
                subItemName.setText(subArticle.getName());
                if (showSub) {
                    SubArticleRelation subArticleRelation = Database.getSubArticleRelation(itemnumberGr.getText(), subItemnumberGr.getText());
                    if (subArticleRelation != null) {
                        amountGr.setText(subArticleRelation.getAmount() + "");
                        descGr.setText(subArticleRelation.getDescription());
                        layerGr.getValueFactory().setValue(subArticleRelation.getLayer());
                    }
                }
            } else {
                subItemName.setText("");
            }

        }

    }


}
