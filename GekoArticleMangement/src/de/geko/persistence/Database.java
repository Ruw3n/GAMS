package de.geko.persistence;

import de.geko.application.Article;
import de.geko.application.Category;
import de.geko.application.SubArticleRelation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Ruwen Lamm
 * Fetches Articles From Database and stores them in ArrayList
 * Also contains further operations that manipulate Article Objetcs and in the database
 */
public class Database {
    private static ArrayList<Article> articles = new ArrayList<>();
    private static boolean rootExist = false;
    private int counter = 0;

    /**
     * Perform recursive product-search and store articles in ArrayList with the corresponding sub-articles
     *
     * @param rootArticleID root article-ID
     */
    public static void recursiveProductSearchRoot(String rootArticleID) {

        Connection conn = ConnectionManager.getConnection();
        String query = "SELECT * FROM PART_LI con JOIN FKT_PRODUCT pro on(pro.ITEMNUMBER = con.ITEMNUMBER) where con.ZSB =?";
        int iItem, iContains;


        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, rootArticleID);
            ResultSet rs = stmt.executeQuery();


            while (rs.next()) {
                rootExist = true;

                String item = "ZSB";
                String contains = "ITEMNUMBER";
                String amount = "MENGE";
                String desc = "note";

                iItem = getIndexOfArticle(rs.getString(item));
                iContains = getIndexOfArticle(rs.getString(contains));
                // ArrayList does not contain Article
                if (iItem == -1) {
                    articles.add(new Article(rs.getString(item), rs.getDouble(amount), rs.getString(desc)));
                    iItem = getIndexOfArticle(rs.getString(item));

                }
                // ArrayList does not contain subArticle
                if (iContains == -1) {
                    articles.add(new Article(rs.getString(contains), rs.getDouble(amount), rs.getString(desc)));
                    iContains = getIndexOfArticle(rs.getString(contains));

                }

                //Adds SubArticle to Article
                articles.get(iItem).addSubItem(articles.get(iContains));

                //System.out.println("ITEMNUMBER: " + rs.getString(item) + " contains " + rs.getString(contains));
                recursiveProductSearchRoot(rs.getString(contains));

            }


        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    /**
     * @param articleID article Id
     * @return -1 => ArrayList does not contain Article
     * else => ArrayListIndex of article
     */
    private static int getIndexOfArticle(String articleID) {
        int index = -1;
        for (int i = 0; i < articles.size(); i++) {
            if (articleID.equals(articles.get(i).getArticleID())) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * @param articleID with root
     * @return root article with corresponding recursive sub articles
     * null=> if article does not exist
     */
    public static Article getArticleTreeRoot(String articleID) {
        articles.clear();
        recursiveProductSearchRoot(articleID);
        if (rootExist) {
            rootExist = false;
            return articles.get(0);

        } else
            return null;
    }

    /**
     * adds sub article to article
     * NOTE: booth articles must exist in Product-table(FKT_PRODUCT) in Database
     *
     * @param articleID    articleID
     * @param subArticleID subArticle
     * @param amount       amount of subArticle
     * @param desc         description
     */
    public static void addSubProduct(String articleID, String subArticleID, double amount, String desc) {
        if (!(doesArticleExist(articleID) && doesArticleExist(subArticleID)))
            throw new IllegalArgumentException("Unterartikel und/oder Überartikel sind\nnicht im Lagerbestand.");
        else if (doesSubArticleExist(articleID, subArticleID))
            throw new IllegalArgumentException("Unterartikel ist bereits der\nBauteilgruppe untergeordent.");
        else {
            Connection conn = ConnectionManager.getConnection();
            String sql = "INSERT INTO PART_LI VALUES (?,?,?,?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, articleID);
                stmt.setString(2, subArticleID);
                stmt.setDouble(3, amount);
                stmt.setString(4, desc);
                stmt.execute();


            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * deletes SubArticle from Article
     *
     * @param articleID    articleID
     * @param subArticleID subArticleID
     */
    public static void deleteSubProduct(String articleID, String subArticleID) {
        if (!(doesArticleExist(articleID) && doesArticleExist(subArticleID)))
            throw new IllegalArgumentException("Unterartikel und/oder Überartikel sind\n nicht im Lagerbestand.");
        else if (!doesSubArticleExist(articleID, subArticleID))
            throw new IllegalArgumentException("Unterartikel und/oder Überartikel bilden keine Bauteilgruppe.");
        else {
            Connection conn = ConnectionManager.getConnection();
            String sql = "DELETE FROM PART_LI WHERE ZSB=? AND ITEMNUMBER=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, articleID);
                stmt.setString(2, subArticleID);
                stmt.execute();


            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * @param articleID article id
     * @return true if article exists in the product table else false
     */
    public static boolean doesArticleExist(String articleID) {
        Connection conn = ConnectionManager.getConnection();
        String sql = "SELECT NAME FROM FKT_PRODUCT WHERE ITEMNUMBER=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, articleID);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param articleID    article id
     * @param subArticleID sub article id
     * @return true if article contains the sub article, else false
     */
    private static boolean doesSubArticleExist(String articleID, String subArticleID) {
        Connection conn = ConnectionManager.getConnection();
        String sql = "SELECT * FROM PART_LI WHERE ZSB=? AND ITEMNUMBER=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, articleID);
            statement.setString(2, subArticleID);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * updates the sub article relation int the given relation table (PART_LI)
     *
     * @param articleID
     * @param subArticleID
     * @param amount
     * @param desc
     */
    public static void updateSubArticle(String articleID, String subArticleID, double amount, String desc) {
        String sql = "UPDATE PART_LI SET MENGE=?, NOTE=? WHERE ZSB=? AND ITEMNUMBER =?";
        Connection conn = ConnectionManager.getConnection();
        if (!(doesArticleExist(articleID) && doesArticleExist(subArticleID)))
            throw new IllegalArgumentException("Unterartikel und/oder Überartikel sind\n nicht im Lagerbestand.");
        else if (!doesSubArticleExist(articleID, subArticleID))
            throw new IllegalArgumentException("Unterartikel und/oder Überartikel bilden keine Bauteilgruppe.");
        else {
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setDouble(1, amount);
                preparedStatement.setString(2, desc);
                preparedStatement.setString(3, articleID);
                preparedStatement.setString(4, subArticleID);
                preparedStatement.execute();

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * @return all existing categories from the database table (FKT_CATEGORY)
     */
    public static ArrayList<Category> getAllCategories() {
        ArrayList<Category> categories = new ArrayList<>();
        String sql = "SELECT ID,NAME,FK_PARENT_CATEGORY FROM FKT_CATEGORY";
        Connection conn = ConnectionManager.getConnection();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                categories.add(new Category(rs.getString(2), rs.getInt(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;

    }

    /**
     * @param categoryID category id
     * @return All articles that correspond to the category
     */
    public static ArrayList<Article> searchArticlesByCategory(long categoryID) {
        ArrayList<Article> articles = new ArrayList<>();
        String sql = "SELECT ITEMNUMBER FROM FKT_PRODUCT WHERE FK_CATEGORY=?";
        Connection conn = ConnectionManager.getConnection();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, categoryID);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                articles.add(new Article(rs.getString(1)));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articles;
    }

    /**
     * @param articleID    article id
     * @param subArticleID article id
     * @return get sub Article relation from Database (PART_LI)
     */
    public static SubArticleRelation getSubArticleRelation(String articleID, String subArticleID) {

        Connection conn = ConnectionManager.getConnection();
        String sql = "SELECT * FROM PART_LI WHERE ZSB=? AND ITEMNUMBER=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, articleID);
            statement.setString(2, subArticleID);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new SubArticleRelation(rs.getString("note"), rs.getString("ZSB"), rs.getString("ITEMNUMBER"), rs.getDouble("MENGE"));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;

    }


}
