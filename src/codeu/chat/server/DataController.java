package codeu.chat.server;

/**
 * Created by jachrostek on 4/7/17.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


public class DataController {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://cse.unl.edu:3306/jchrostek";

    private static final String USER = "jchrostek";
    private static final String PASS = "T_4r3K";

    private static Connection conn = null;

    private DataController() {}

    private static void connect() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void disconnect() {
        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearDatabase() {
        DataController.connect();
        try {
            Statement stmt = conn.createStatement();
            String sql;

            sql = "DROP TABLE IF EXISTS Messages";
            stmt.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS Access";
            stmt.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS Conversations";
            stmt.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS Users";
            stmt.executeUpdate(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DataController.disconnect();
    }

    public static void makeDatabase() {
        DataController.connect();
        try {
            Statement stmt = conn.createStatement();
            String sql;

            sql =
                    "CREATE TABLE IF NOT EXISTS Users("
                            + "UuidID INT(8) NOT NULL,"
                            + "UserName VARCHAR(20) NOT NULL,"
                            + "PasswordHash VARCHAR(100) NOT NULL,"
                            + "Salt VARCHAR(100) NOT NULL,"
                            + "Time TIMESTAMP(3),"
                            + "PRIMARY KEY(UuidID) )";
            stmt.executeUpdate(sql);

            sql =
                    "CREATE TABLE IF NOT EXISTS Conversations("
                            + "UuidID INT(8) NOT NULL,"
                            + "Owner INT(8) NOT NULL,"
                            + "Time TIMESTAMP(3),"
                            + "Title VARCHAR(20),"
                            + "PasswordHash VARCHAR(100),"
                            + "Salt VARCHAR(100),"
                            + "PRIMARY KEY(UuidID),"
                            + "FOREIGN KEY(Owner) REFERENCES Users(UuidID) )";
            stmt.executeUpdate(sql);

            sql =
                    "CREATE TABLE IF NOT EXISTS Messages("
                            + "UuidID INT(8) NOT NULL,"
                            + "Owner INT(8) NOT NULL,"
                            + "Conversation INT(8) NOT NULL,"
                            + "Content VARCHAR(1000),"
                            + "Time TIMESTAMP(3),"
                            + "PRIMARY KEY(UuidID),"
                            + "FOREIGN KEY(Owner) REFERENCES Users(UuidID) )";
            stmt.executeUpdate(sql);

            sql =
                    "CREATE TABLE IF NOT EXISTS Access("
                            + "UserID INT(8) NOT NULL,"
                            + "ConversationID INT(8) NOT NULL,"
                            + "FOREIGN KEY(UserID) REFERENCES Users(UuidID),"
                            + "FOREIGN KEY(ConversationID) REFERENCES Conversations(UuidID) )";
            stmt.executeUpdate(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DataController.disconnect();
    }

    public static void addUser(
            int ID, String userName, String passwordHash, String salt, String time) {
        try {
            DataController.connect();
            Statement stmt = conn.createStatement();
            String sql =
                    "INSERT INTO Users VALUES('"
                            + ID
                            + "','"
                            + userName
                            + "','"
                            + passwordHash
                            + "','"
                            + salt
                            + "','"
                            + time
                            + "')";
            stmt.executeUpdate(sql);
            DataController.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addConversation(int ID, int owner, String time, String title, String passwordHash, String salt) {
        try{
            DataController.connect();
            Statement stmt = conn.createStatement();
            String sql =
                    "INSERT INTO Conversations VALUES('"
                            + ID
                            + "','"
                            + owner
                            + "','"
                            + time
                            + "','"
                            + title
                            + "','"
                            + passwordHash
                            + "','"
                            + salt
                            + "')";
            stmt.executeUpdate(sql);
            DataController.disconnect();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void addMessage(int ID, int ownerID, int conversationID, String content, String time) {
        try{
            DataController.connect();
            Statement stmt = conn.createStatement();
            String sql =
                    "INSERT INTO Messages VALUES('"
                            + ID
                            + "','"
                            + ownerID
                            + "','"
                            + conversationID
                            + "','"
                            + content
                            + "','"
                            + time
                            + "')";
            stmt.executeUpdate(sql);
            DataController.disconnect();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void addAccess(int userID, int conversationID) {
        try{
            DataController.connect();
            Statement stmt = conn.createStatement();
            String sql =
                    "INSERT INTO Access VALUES('"
                            + userID
                            + "','"
                            + conversationID
                            + "')";
            stmt.executeUpdate(sql);
            DataController.disconnect();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

} //end class
