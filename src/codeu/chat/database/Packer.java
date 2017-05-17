package codeu.chat.database;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import org.bson.Document;

import java.io.IOException;

public class Packer {
    /**
     * Convert a Document to a User object
     * @param doc: the Document to be converted
     * @return a User derived from doc
     */
    public static User unpackUser(Document doc) throws IOException {
        System.out.println("Unpacking UserID: " + Uuid.parse(doc.getString("id")));
        return new User(
                Uuid.parse(doc.getString("id")),
                doc.getString("name"),
                Time.fromMs(doc.getLong("creation")),
                doc.getString("passHash"),
                doc.getString("salt")
        );
    }


    /**
     * Convert a Document to a Message object
     * @param doc: the Document to be converted
     * @return a Message derived from doc
     */
    public static Message unpackMessage(Document doc) throws IOException {
        return new Message(
                Uuid.parse(doc.getString("id")),
                Uuid.parse(doc.getString("next")),
                Uuid.parse(doc.getString("previous")),
                Time.fromMs(doc.getLong("creation")),
                Uuid.parse(doc.getString("conversation")),
                Uuid.parse(doc.getString("author")),
                doc.getString("content")
        );
    }


    /**
     * Convert a Document to a Conversation object
     * @param doc: the Document to be converted
     * @return a Conversation derived from doc
     */
    public static Conversation unpackConversation(Document doc) throws IOException {
        return new Conversation(
                Uuid.parse(doc.getString("id")),
                Uuid.parse(doc.getString("owner")),
                Time.fromMs(doc.getLong("creation")),
                doc.getString("title"),
                doc.getString("passHash"),
                doc.getString("salt")
        );
    }


    /**
     * Converts a User object to a Document
     * @param user: the User to be converted
     * @return a Document derived from Message
     */
    public static Document packUser(User user) {
        System.out.println("Packing UserID: " + user.id.toString());
        Document doc = new Document("id", user.id.toString())
                .append("name", user.name)
                .append("creation", user.creation.inMs())
                .append("passHash", user.getPasswordHash())
                .append("salt", user.getSalt());
        return doc;
    }


    /**
     * Converts a Message object to a Document
     * @param message: the Message to be converted
     * @return a Document derived from Message
     */
    public static Document packMessage(Message message, Uuid conversation) {
        Document doc = new Document("id", message.id.toString())
                .append("next", message.next.toString())
                .append("previous", message.previous.toString())
                .append("creation", message.creation.inMs())
                .append("author", message.author.toString())
                .append("content", message.content.toString())
                .append("conversation", conversation.toString());
        return doc;
    }


    /**
     * Converts a Conversation object to a Document
     * @param conversation: the Conversation to be converted
     * @return a Document derived from Conversation
     */
    public static Document packConversation(Conversation conversation) {
        Document doc = new Document("id", conversation.id.toString())
                .append("owner", conversation.owner.toString())
                .append("creation", conversation.creation.inMs())
                .append("title", conversation.title)
                .append("passHash", conversation.getPassHash())
                .append("salt", conversation.getSalt())
                .append("users", conversation.users)
                .append("firstMessage", conversation.firstMessage.toString())
                .append("lastMessage", conversation.lastMessage.toString());
        return doc;
    }
}
