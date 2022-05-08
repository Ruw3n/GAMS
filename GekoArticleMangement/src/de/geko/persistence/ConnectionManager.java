package de.geko.persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ResourceBundle;

/**
 * @author Ruwen Lamm
 * Establish connection to GEKO-Database
 */
public class ConnectionManager {

    private static Connection conn;

    static {
        conn = null;
        try {

            ResourceBundle rb = ResourceBundle.getBundle("connectionParams");

            String url = rb.getString("url");
            String user = rb.getString("user");
            String password = rb.getString("password");


            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connecting to MySQL Database....");

        } catch (Exception e) {
            System.out.println("Error while connecting to MySQL Database");
            e.printStackTrace();
            System.exit(1);

        }

    }

    public static Connection getConnection() {
        return conn;
    }


}
