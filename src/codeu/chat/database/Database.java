package codeu.chat.database;

import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Database {
    public MongoClient mongoClient;
    public MongoDatabase database;
    public MongoCollection<Document> users;
    public MongoCollection<Document> messages;
    public MongoCollection<Document> conversations;

    public Database(String path){
        try{
            //TODO: make MongoClientURI to connect to online database
            mongoClient = new MongoClient();
            database = mongoClient.getDatabase(path);
            System.out.println("Successful connection to database.");
            loadCollections();
        }catch(Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    /**
     * Loads all necessary documents for database (users, messages, conversations)
     */
    public void loadCollections(){
        users = database.getCollection("users");
        conversations = database.getCollection("conversations");
        messages = database.getCollection("messages");
        System.out.println("All database collections loaded successfully.");
    }

    /**
     * Convert a Message to document form and store in messages collection
     * @param message: the message to store
     */
    public void write(Message message){
        messages.insertOne(Packer.packMessage(message));
    }

    /**
     * Convert a Conversation to Document form and store in conversations collection
     * @param conversation: the conversation to be stored
     */
    public void write(Conversation conversation){
        conversations.insertOne(Packer.packConversation(conversation));
    }

    /**
     * Convert a User to Document form and store in users collection
     * @param user: the user to be stored
     */
    public void write(User user) {
        users.insertOne(Packer.packUser(user));
    }

    /**
     * Return a collection of Users from the Database
     * @param limit: the maximum number of users in the collection
     * @return an ArrayList of Users
     */
    public Collection<User> getUsers(int limit) throws IOException {
        ArrayList<User> returnedUsers = new ArrayList<User>();

        for(Document doc : users.find()) {
            if (limit-- == 0) break;
            returnedUsers.add(Packer.unpackUser(doc));
        }
        return returnedUsers;
    }

    /**
     * Return a collection of Messages from the Database
     * @param limit: the maximum number of messages in the collection
     * @return an ArrayList of Messages
     */
    public Collection<Message> getMessages(int limit) throws IOException {
        ArrayList<Message> returnedMessages = new ArrayList<Message>();

        for (Document doc : messages.find()) {
            if (limit -- == 0) break;
            returnedMessages.add(Packer.unpackMessage(doc));
        }
        return returnedMessages;
    }

    /**
     * Return a collection of Conversations from the Database
     * @param limit: the maximum number of conversations in the collection
     * @return an ArrayList of Conversations
     */
    public Collection<Conversation> getConversations(int limit) throws IOException {
        ArrayList<Conversation> returnedConversation = new ArrayList<Conversation>();

        for (Document doc : conversations.find()) {
            if (limit -- == 0) break;
            returnedConversation.add(Packer.unpackConversation(doc));
        }
        return returnedConversation;
    }
}