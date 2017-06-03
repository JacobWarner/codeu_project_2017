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

package codeu.chat.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.util.Logger;
import codeu.chat.util.Method;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.Store;

public final class ClientConversation {

  private final static Logger.Log LOG = Logger.newLog(ClientConversation.class);

  private final Controller controller;
  private final View view;

  private ConversationSummary currentSummary = null;
  private Conversation currentConversation = null;

  private final ClientUser userContext;
  private ClientMessage messageContext = null;

  // This is the set of conversations known to the server.
  private final Map<Uuid, ConversationSummary> summariesByUuid = new HashMap<>();

  // This is the set of conversations known to the server, sorted by title.
  private Store<String, ConversationSummary> summariesSortedByTitle =
      new Store<>(String.CASE_INSENSITIVE_ORDER);

  public ClientConversation(Controller controller, View view, ClientUser userContext) {
    this.controller = controller;
    this.view = view;
    this.userContext = userContext;
  }

  public void setMessageContext(ClientMessage messageContext) {
    this.messageContext = messageContext;
  }

  // Validate the title of the conversation
  private static boolean isValidTitle(String title) {
    if(title == null || title.equals("") || title.length() <= 1 || title.length() > 64){
      return false;
    }
    Pattern p = Pattern.compile("[\"()<>/;\\\\*%$^&+=:|~`]");
    Matcher m = p.matcher(title);
    return !m.find();
  }

  public boolean hasCurrent() {
    return (currentSummary != null);
  }

  public ConversationSummary getCurrent() {
    return currentSummary;
  }

  public Uuid getCurrentId() { return (currentSummary != null) ? currentSummary.id : null; }

  public int currentMessageCount() {
    return messageContext.currentMessageCount();
  }

  public void showCurrent() {
    printConversation(currentSummary, userContext);
  }

  public void startConversation(String title, Uuid owner, String passHash, String salt) {
    final boolean validInputs = (isValidTitle(title)) && (owner != null) && (ClientUser.isValidPassword(passHash));

    final Conversation conv = (validInputs) ? controller.newConversation(title, owner, passHash, salt) : null;

    if (conv == null) {
      System.out.println(String.format("Error: conversation not created - %s.\n",
          (validInputs) ? "server failure" : "bad input value"));
    } else {
      System.out.println(String.format("New conversation: Title= \"%s\" UUID= %s\n", conv.title, conv.id));

      updateAllConversations(true);
      setCurrent(conv.summary);
    }
  }

  public void setCurrent(ConversationSummary conversation) { currentSummary = conversation; }

  public void showAllConversations() {
    updateAllConversations(true);

    for (final ConversationSummary c : summariesByUuid.values()) {
      printConversation(c, userContext);
    }
  }

  // Get a single conversation from the server.
  Conversation getConversation(Uuid conversationId) {
    return view.getConversations(Collections.singletonList(conversationId)).iterator().next();
  }

  private void leaveCurrentConversation() {
    Method.notImplemented();
  }

  private void updateCurrentConversation() {
    if (currentSummary == null) {
      currentConversation = null;
    } else {
      currentConversation = getConversation(currentSummary.id);
      if (currentConversation == null) {
        LOG.info("GetConversation: current=%s, current.id=%s, but currentConversation == null",
            currentSummary, currentSummary.id);
      } else {
        LOG.info("Get Conversation: Title=\"%s\" UUID=%s first=%s last=%s\n",
            currentConversation.title, currentConversation.id, currentConversation.firstMessage,
            currentConversation.lastMessage);
      }
    }
  }

  public int conversationsCount() {
   return summariesByUuid.size();
  }

  public Iterable<ConversationSummary> getConversationSummaries() {
    return summariesSortedByTitle.all();
  }

  // Update the list of known Conversations.
  // If the input currentChanged is true, then re-establish the state of
  // the current Conversation, including its messages.
  public void updateAllConversations(boolean currentChanged) {
    summariesByUuid.clear();
    summariesSortedByTitle = new Store<>(String.CASE_INSENSITIVE_ORDER);

    for (final ConversationSummary cs : view.getAllConversations()) {
      summariesByUuid.put(cs.id, cs);
      summariesSortedByTitle.insert(cs.title, cs);
    }

    if (currentChanged) {
      updateCurrentConversation();
      messageContext.resetCurrent(true);
    }
  }

  // Print Conversation.  User context is used to map from owner UUID to name.
  public static void printConversation(ConversationSummary c, ClientUser userContext) {
    if (c == null) {
      System.out.println("Null conversation");
    } else {
      final String name = (userContext == null) ? null : userContext.getName(c.owner);
      final String ownerName = (name == null) ? "" : String.format(" (%s)", name);
      System.out.format(" Title: %s\n", c.title);
      System.out.format("    Id: %s owner: %s%s created %s\n", c.id, c.owner, ownerName, c.creation);
      if(c.isPrivate()){
        System.out.format("Status: Private\n\n");
      }else{
        System.out.format("Status: Public\n\n");
      }

    }
  }

  public static void printConversation(ConversationSummary c) {
    printConversation(c, null);
  }

  //Checks if the conversation exists
  public boolean exists(String title){
    updateAllConversations(true);
    ConversationSummary conSum = summariesSortedByTitle.first(title);
    return (conSum != null);
  }


  //TODO: If there are two public conversations with the same title, we run into an issue...
  public boolean joinConversation(String title){
    return joinConversation(title, "defaultPassword123!");
  }


  public boolean joinConversation(String title, String password){
    if(!userContext.hasCurrent()){
      System.out.println("Join failed. Not signed into a user.");
      return false;
    }

    if(exists(title)){
      ConversationSummary conn = summariesSortedByTitle.first(title);
      if(conn!= null && conn.isPassword(password)) {
        setCurrent(conn);
        return true;
      }
      else {
        System.out.println("Incorrect password. Staying in current conversation.");
        return false;
      }
    }
    return false;
  }

}
