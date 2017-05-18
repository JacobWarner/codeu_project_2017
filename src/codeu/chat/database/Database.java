package codeu.chat.database;

import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;
import com.mongodb.MongoClientURI;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Database {
    public MongoClient mongoClient;
    public MongoDatabase database;
    public MongoCollection<Document> users;
    public MongoCollection<Document> messages;
    public MongoCollection<Document> conversations;

    private boolean clearDatabase = false;
    private String username = "User";
    private String password = "SuperSecretPassword";
    private String databasePath = "TestDatabase";
    private String URI = "mongodb://" + username + ":" + password + "@codeu-shard-00-00-kskbs.mongodb.net:27017,codeu-shard-00-01-kskbs.mongodb.net:27017,codeu-shard-00-02-kskbs.mongodb.net:27017/" + databasePath + "?ssl=true&replicaSet=CodeU-shard-0&authSource=admin";

    public Database(){
        try{
            MongoClientURI uriConnect = new MongoClientURI(URI);
            this.mongoClient = new MongoClient(uriConnect);
            this.database = mongoClient.getDatabase(databasePath);
            System.out.println("Successful connection to database.");

            if(clearDatabase){
                removeDatabaseCollections();
            }

            createDatabaseCollections();

        }catch(Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    /**
     * Convert a Message to document form and store in messages collection
     * @param message: the message to store
     */
    public boolean write(Message message, Uuid conversation){
        database.getCollection("messages").insertOne(Packer.packMessage(message,conversation));
        return true;
    }

    /**
     * Convert a Conversation to Document form and store in conversations collection
     * @param conversation: the conversation to be stored
     */
    public boolean write(Conversation conversation){
        database.getCollection("conversations").insertOne(Packer.packConversation(conversation));
        return true;
    }

    /**
     * Convert a User to Document form and store in users collection
     * @param user: the user to be stored
     */
    public boolean write(User user) {
        database.getCollection("users").insertOne(Packer.packUser(user));
        return true;
    }

    public void createDatabaseCollections(){
        if(database.getCollection("users") == null){
            database.createCollection("users");
        }
        if(database.getCollection("conversations") == null){
            database.createCollection("conversations");
        }
        if(database.getCollection("messages") == null){
            database.createCollection("messages");
        }
        System.out.println("Created the database collections.");
    }

    public void removeDatabaseCollections(){
        database.getCollection("messages").drop();
        database.getCollection("conversations").drop();
        database.getCollection("users").drop();
        System.out.println("Removed the database collections.");
    }
}