package de.geko.persistence;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author Ruwen Lamm
 * Establish connection to GEKO-Database
 */
public class ConnectionManager {

    private static Connection conn;

    static {
        conn = null;
        try {
            Object obj = new JSONParser().parse(new FileReader("src/de/geko/persistence/passwords.json"));
            JSONObject jsonObject = (JSONObject) obj;
            String user = (String) jsonObject.get("user");
            String password = (String) jsonObject.get("password");

            conn = DriverManager.getConnection("jdbc:mysql://192.168.1.11:3306/fakturama5", user, password);
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
