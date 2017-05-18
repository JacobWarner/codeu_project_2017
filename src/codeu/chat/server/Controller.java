// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.server;

import java.util.Collection;

import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.RandomUuidGenerator;
import codeu.chat.common.RawController;
import codeu.chat.common.User;
import codeu.chat.database.Database;
import codeu.chat.util.Logger;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

public final class Controller implements RawController, BasicController {

  private static final Logger.Log LOG = Logger.newLog(Controller.class);

  private final Model model;
  private final Database database;

  private final Uuid.Generator uuidGenerator;
  private final Uuid serverId;

  public Controller(Uuid serverId, Model model, Database database) {
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());
    this.serverId = serverId;
    this.database = database;

    loadDatabase();
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {
    return newMessage(createId(), author, conversation, body, Time.now());
  }

  @Override
  public User newUser(String name, String PasswordHash, String salt) {
    return newUser(createId(), name, Time.now(), PasswordHash, salt);
  }

  @Override
  public Conversation newConversation(String title, Uuid owner, String passHash, String salt) {
    return newConversation(createId(), title, owner, Time.now(),passHash, salt);
  }

  @Override
  public Message newMessage(
      Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

    final User foundUser = model.userById().first(author);
    final Conversation foundConversation = model.conversationById().first(conversation);

    Message message = null;


    if (foundUser != null && foundConversation != null && isIdFree(id)) {

      message = new Message(id, Uuid.NULL, Uuid.NULL, creationTime, author, body);

      if(database.write(message, foundConversation.id)){
        model.add(message);
        LOG.info("Message added: %s", message.id);

        // Find and update the previous "last" message so that it's "next" value
        // will point to the new message.

        if (Uuid.equals(foundConversation.lastMessage, Uuid.NULL)) {

          // The conversation has no messages in it, that's why the last message is NULL (the first
          // message should be NULL too. Since there is no last message, then it is not possible
          // to update the last message's "next" value.

        } else {
          final Message lastMessage = model.messageById().first(foundConversation.lastMessage);
          lastMessage.next = message.id;
        }

        // If the first message points to NULL it means that the conversation was empty and that
        // the first message should be set to the new message. Otherwise the message should
        // not change.

        foundConversation.firstMessage =
                Uuid.equals(foundConversation.firstMessage, Uuid.NULL)
                        ? message.id
                        : foundConversation.firstMessage;

        // Update the conversation to point to the new last message as it has changed.

        foundConversation.lastMessage = message.id;

        if (!foundConversation.users.contains(foundUser)) {
          foundConversation.users.add(foundUser.id);
        }
      }else{
        message = null;
        LOG.info("New message failure. Could not save to database: (message.id=%s message.author=%s message.conversation=%s message.creation=%s message.body=%s)",
                id,author,conversation,creationTime,body);
      }


    }

    return message;
  }

  @Override
  public User newUser(Uuid id, String name, Time creationTime, String PasswordHash, String salt) {

    User user = null;

    if (isIdFree(id)) {

      user = new User(id, name, creationTime, PasswordHash, salt);

      if(database.write(user)){
        model.add(user);

        LOG.info(
                "newUser success (user.id=%s user.name=%s user.time=%s user.passHash=%s)",
                id, name, creationTime, PasswordHash);
      }else{
        LOG.info("New user failure. Could  not save to database: (user.id=%s user.name=%s user.time=%s)",
                id,name,creationTime);
      }


    } else {

      LOG.info(
          "newUser fail - id in use (user.id=%s user.name=%s user.time=%s)",
          id, name, creationTime);
    }

    return user;
  }

  @Override
  public Conversation newConversation(Uuid id, String title, Uuid owner, Time creationTime, String passHash, String salt) {

    final User foundOwner = model.userById().first(owner);

    Conversation conversation = null;

    if (foundOwner != null && isIdFree(id)) {
      conversation = new Conversation(id, owner, creationTime, title, passHash, salt);

      if(database.write(conversation)){
        model.add(conversation);
        LOG.info("Conversation added: " + conversation.id);
      }else{
        conversation = null;
        LOG.info("New conversation fail. Could not save to database (conversation.id=%s conversation.title=%s conversation.owner=%s conversation.time=%s)",
                id,title,owner,creationTime);
      }


    }else{
      LOG.info("newConversation fail - id in use: " + id);
    }

    return conversation;
  }

  public Uuid buildUuid(int id){
    return new Uuid(serverId, id);
  }

  private Uuid createId() {

    Uuid candidate;

    for (candidate = uuidGenerator.make(); isIdInUse(candidate); candidate = uuidGenerator.make()) {

      // Assuming that "randomUuid" is actually well implemented, this
      // loop should never be needed, but just incase make sure that the
      // Uuid is not actually in use before returning it.

    }

    return candidate;
  }

  private boolean isIdInUse(Uuid id) {
    return model.messageById().first(id) != null
        || model.conversationById().first(id) != null
        || model.userById().first(id) != null;
  }

  private boolean isIdFree(Uuid id) {
    return !isIdInUse(id);
  }

  private void loadDatabase(){
    loadUsers();
    loadConversations();
    loadMessages();
    System.out.println("----------------------------LOADED DATABASE----------------------------");
  }

  private void loadUsers(){
    MongoCollection<Document> users = database.database.getCollection("users");

    for(Document doc : users.find()){
        final Uuid userID = buildUuid(doc.getInteger("id"));

        if(isIdFree(userID)){
          String name = doc.getString("name");
          Time creation = Time.fromMs(doc.getLong("creation"));
          final String passHash = doc.getString("passHash");
          final String salt = doc.getString("salt");

          model.add(new User(userID, name, creation, passHash, salt));
          LOG.info("Loaded User: \nid: %s\nname: %s\ncreation: %s\n", userID, name, creation);
        }else{
          LOG.info("Loading User failure. ID already in use: " + userID);
        }
    }
  }

  private void loadConversations(){
    MongoCollection<Document> conversations = database.database.getCollection("conversations");

    for(Document doc : conversations.find()){
      final Uuid conversationID = buildUuid(doc.getInteger("id"));
      final Uuid ownerID = buildUuid(doc.getInteger("owner"));
      final User foundOwner = model.userById().first(ownerID);

      if(foundOwner != null && isIdFree(conversationID)){
        String title = doc.getString("title");
        Time creation = Time.fromMs(doc.getLong("creation"));
        String passHash = doc.getString("passHash");
        String salt = doc.getString("salt");

        model.add(new Conversation(conversationID, foundOwner.id, creation, title, passHash, salt));
        LOG.info("Loaded Conversation: \nid: %s\ntitle: %s\nowner: %s\ncreation: %s\n", conversationID, title, foundOwner.name, creation);
      }else{
        LOG.info("Loading Conversation failure. Owner not found or ID already in use: " + conversationID);
      }
    }
  }

  private void loadMessages(){
    MongoCollection<Document> messages = database.database.getCollection("messages");
    FindIterable<Document> sortedMessages = messages.find().sort(new BasicDBObject("creation",1));

    for(Document doc : sortedMessages){
      final Uuid messageID = buildUuid(doc.getInteger("id"));
      Time creation = Time.fromMs(doc.getLong("creation"));
      final Uuid conversationID = buildUuid(doc.getInteger("conversation"));
      Conversation foundConversation = model.conversationById().first(conversationID);
      final Uuid authorID = buildUuid(doc.getInteger("author"));
      User foundUser = model.userById().first(authorID);
      String content = doc.getString("content");

      if(foundUser != null && foundConversation != null && isIdFree(messageID)){
        model.add(new Message(messageID, Uuid.NULL, Uuid.NULL, creation, foundUser.id, content));

        // Find and update the previous "last" message so that it's "next" value
        // will point to the new message.

        if (Uuid.equals(foundConversation.lastMessage, Uuid.NULL)) {

          // The conversation has no messages in it, that's why the last message is NULL (the first
          // message should be NULL too. Since there is no last message, then it is not possible
          // to update the last message's "next" value.

        } else {
          final Message lastMessage = model.messageById().first(foundConversation.lastMessage);
          lastMessage.next = messageID;
        }

        // If the first message points to NULL it means that the conversation was empty and that
        // the first message should be set to the new message. Otherwise the message should
        // not change.
        if(Uuid.equals(foundConversation.firstMessage, Uuid.NULL)){
          foundConversation.firstMessage = messageID;
        }

        // Update the conversation to point to the new last message as it has changed.

        foundConversation.lastMessage = messageID;

        if (!foundConversation.users.contains(foundUser)) {
          foundConversation.users.add(foundUser.id);
        }

        LOG.info("Loaded Message: \nid: %s\nauthor: %s\nconversation: %s\ncontent: %s\ncreation: %s\n", messageID, foundUser.name, foundConversation.title, content, creation);
      }else{
        LOG.info("Loading Message failure. No user/conversation found or ID is in use: " + messageID);
      }


    }
  }
}
