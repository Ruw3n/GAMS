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

            Object obj = new JSONParser().parse(new FileReader("passwords.json"));
            JSONObject jsonObject = (JSONObject) obj;
            String user = (String) jsonObject.get("user");
            String password = (String) jsonObject.get("password");
            String ip = (String) jsonObject.get("ip");


            conn = DriverManager.getConnection("jdbc:mysql://" + ip, user, password);
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
