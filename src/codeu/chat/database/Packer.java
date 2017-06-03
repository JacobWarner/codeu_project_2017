package codeu.chat.database;

import codeu.chat.util.Uuid;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import org.bson.Document;

class Packer {

    /**
     * Converts a User object to a Document
     * @param user: the User to be converted
     * @return a Document derived from Message
     */
    static Document packUser(User user) {
        return new Document("id", user.id.id())
                .append("name", user.name)
                .append("creation", user.creation.inMs())
                .append("passHash", user.getPasswordHash())
                .append("salt", user.getSalt());
    }

    /**
     * Converts a Message object to a Document
     * @param message: the Message to be converted
     * @param conversation: the ID of the Conversation the Message belongs to
     * @return a Document object derived from Message
     */
    static Document packMessage(Message message, Uuid conversation) {
        return new Document("id", message.id.id())
                .append("creation", message.creation.inMs())
                .append("author", message.author.id())
                .append("content", message.content)
                .append("conversation", conversation.id());
    }


    /**
     * Converts a Conversation object to a Document
     * @param conversation: the Conversation to be converted
     * @return a Document derived from Conversation
     */
    static Document packConversation(Conversation conversation) {
        return new Document("id", conversation.id.id())
                .append("owner", conversation.owner.id())
                .append("creation", conversation.creation.inMs())
                .append("title", conversation.title)
                .append("passHash", conversation.getPassHash())
                .append("salt", conversation.getSalt());
    }
}
