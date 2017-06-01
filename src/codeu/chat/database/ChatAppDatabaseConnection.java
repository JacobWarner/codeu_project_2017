package codeu.chat.database;

/**
 * Created by Jacob Warner on May 18, 2017
 */

import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Uuid;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class ChatAppDatabaseConnection {
    private static final Logger.Log LOG = Logger.newLog(ChatAppDatabaseConnection.class);

    private MongoClient mongoClient;
    private MongoDatabase database;

    //Default sign-in info for MongoDB
    private String username;
    private String password;
    private String databasePath;
    private String URI;

    public ChatAppDatabaseConnection(){
        try{
            MongoClientURI uriConnect =
                    new MongoClientURI("mongodb://DefaultUser:xETluxpH3EtpXeig@codeu-shard-00-00-kskbs.mongodb.net:27017,"
                            +"codeu-shard-00-01-kskbs.mongodb.net:27017,codeu-shard-00-02-kskbs.mongodb.net:27017/"
                            +"ChatAppDatabase?ssl=true&replicaSet=CodeU-shard-0&authSource=admin");

            this.mongoClient = new MongoClient(uriConnect);
            this.database = mongoClient.getDatabase("ChatAppDatabase");
            LOG.info("Successful connection to default database.");

        }catch(MongoException me){
            LOG.info("Failed to connect to database.");
            throw new MongoException(me.getMessage());
        }
    }

    /**
     * This constructor takes login info for MongoDB,
     * in case anyone wants to connect to their own
     * @param user: username for MongoDatabase
     * @param pass: password for MongoDatabase user
     * @param path: path to the database that the user belongs to
     */
    public ChatAppDatabaseConnection(String user, String pass, String path){
        this.username = user;
        this.password = pass;
        this.databasePath = path;
        this.URI = "mongodb://" + user + ":" + pass + "@codeu-shard-00-00-kskbs.mongodb.net:27017,codeu-shard-00-01-kskbs.mongodb.net:27017,codeu-shard-00-02-kskbs.mongodb.net:27017/" + path + "?ssl=true&replicaSet=CodeU-shard-0&authSource=admin";

        try{
            MongoClientURI uriConnect = new MongoClientURI(URI);
            this.mongoClient = new MongoClient(uriConnect);
            this.database = mongoClient.getDatabase(path);
            LOG.info("Successful connection to database, " + path);

        }catch(MongoException me){
            LOG.info("Failed to connect to database.");
            throw new MongoException(me.getMessage());
        }
    }

    /**
     * Convert a User to Document form and store in users collection
     * @param user: the user to be stored
     * @return boolean: true if the insertion of the Document is accepted by the database; false otherwise
     */
    public boolean write(User user) {
        try{
            database.getCollection("users").insertOne(Packer.packUser(user));
            LOG.info("Successfully saved user on database.");
            return true;
        }catch(MongoWriteException | MongoWriteConcernException mwe){
            LOG.info("Failed to save user on database:\nID:%s\nError:%s\n", user.id, mwe.getMessage());
            return false;
        }
    }

    /**
     * Convert a Conversation to Document form and store in conversations collection
     * @param conversation: the conversation to be stored
     * @return boolean: true if the insertion of the Document is accepted by the database; false otherwise
     */
    public boolean write(Conversation conversation){
        try{
            database.getCollection("conversations").insertOne(Packer.packConversation(conversation));
            LOG.info("Successfully saved conversation on database.");
            return true;
        }catch(MongoWriteException | MongoWriteConcernException mwe){
            LOG.info("Failed to save conversation on database:\nID:%s\nError:%s\n", conversation.id, mwe.getMessage());
            return false;
        }
    }

    /**
     * Convert a Message to document form and store in messages collection
     * @param message: the message to store
     * @return boolean: true if the insertion of the Document is accepted by the database; false otherwise
     */
    public boolean write(Message message, Uuid conversation){
        try{
            database.getCollection("messages").insertOne(Packer.packMessage(message, conversation));
            LOG.info("Successfully saved message on database.");
            return true;
        }catch(MongoWriteException | MongoWriteConcernException mwe){
            LOG.info("Failed to save message on database:\nID:%s\nError:%s\n", message.id, mwe.getMessage());
            return false;
        }
    }

    //Not implemented - more to do
    public boolean removeUser(User user) {
        try{
            database.getCollection("messages").deleteMany(eq("author", user.id.id()));
            database.getCollection("conversations").deleteMany(eq("owner", user.id.id()));
            database.getCollection("users").deleteOne(eq("id",user.id.id()));
            return true;
        }catch(MongoWriteException | MongoWriteConcernException me){
            LOG.info("Failed to remove user from database:\nID:%s\nError:%s\n", user.id, me.getMessage());
            return false;
        }
    }

    //Not implemented - more to do
    public boolean removeConversation(Conversation conversation) {
        try{
            database.getCollection("messages").deleteMany(eq("conversation", conversation.id));
            database.getCollection("conversations").deleteOne(eq("id",conversation.id.id()));
            return true;
        }catch(MongoWriteException | MongoWriteConcernException me){
            LOG.info("Failed to remove conversation from database:\nID:%s\nError:%s\n", conversation.id, me.getMessage());
            return false;
        }
    }

    //Not implemented - more to do
    public boolean removeMessage(Message message) {
        try{
            database.getCollection("messages").deleteOne(eq("id",message.id.id()));
            return true;
        }catch(MongoWriteException | MongoWriteConcernException me){
            LOG.info("Failed to remove message from database:\nID:%s\nError:%s\n", message.id, me.getMessage());
            return false;
        }
    }

    public MongoCollection<Document> getUserCollection(){
        return database.getCollection("users");
    }

    public MongoCollection<Document> getConversationsCollection(){
        return database.getCollection("conversations");
    }

    public MongoCollection<Document> getMessagesCollection(){
        return database.getCollection("messages");
    }

    /**
     * Used to clear the database of its user, conversation, and message data collections.
     *
     * NOTE: No need for a "createDatabaseCollections" method, as MongoDB creates
     * one was data is first entered in it.
     */
    public void removeDatabaseCollections(){
        removeMessagesCollection();
        removeConversationsCollection();
        removeUsersCollection();
    }

    public void removeMessagesCollection() {
        database.getCollection("messages").drop();
    }

    public void removeConversationsCollection() {
        database.getCollection("conversations").drop();
    }

    public void removeUsersCollection() {
        database.getCollection("users").drop();
    }


    public MongoDatabase getDatabase() {
        return database;
    }
}