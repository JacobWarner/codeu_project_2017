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

package codeu.chat.client.commandline;

import java.util.Scanner;

import codeu.chat.client.ClientContext;
import codeu.chat.client.Controller;
import codeu.chat.client.View;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.Password;
import codeu.chat.util.Logger;
import codeu.chat.util.Method;

// Chat - top-level client application.
public final class Chat {

  private static final Logger.Log LOG = Logger.newLog(Chat.class);

  private static final String PROMPT = ">>";

  private static final String EXIT = "exit";
  private static final String HELP = "help";
  private static final String SIGN_IN = "sign-in";
  private static final String SIGN_OUT = "sign-out";
  private static final String CURRENT = "current";
  private static final String U_ADD = "u-add";
  private static final String U_LIST_ALL = "u-list-all";
  private static final String C_ADD = "c-add";
  private static final String C_LIST_ALL = "c-list-all";
  private static final String C_SELECT = "c-select";
  private static final String C_PUBLIC = "c-public";
  private static final String C_PRIVATE = "c-private";
  private static final String M_ADD = "m-add";
  private static final String M_LIST_ALL = "m-list-all";
  private static final String M_SHOW = "m-show";

  private boolean alive = true;
  private Scanner lineScanner;
  private Scanner tokenScanner;


  private final ClientContext clientContext;

  // Constructor - sets up the Chat Application
  public Chat(Controller controller, View view) {
    clientContext = new ClientContext(controller, view);
  }

  // Print help message.
  private static void help() {
    System.out.println("Chat commands:");
    System.out.println("   exit      - exit the program.");
    System.out.println("   help      - this help message.");
    System.out.println("   sign-in <username>  - sign in as user <username>.");
    System.out.println("   sign-out  - sign out current user.");
    System.out.println("   current   - show current user, conversation, message.");
    System.out.println("User commands:");
    System.out.println("   u-add <name>  - add a new user.");
    System.out.println("   u-list-all    - list all users known to system.");
    System.out.println("Conversation commands:");
    System.out.println("   c-add <title>    - add a new conversation.");
    System.out.println("   c-list-all       - list all conversations known to system.");
    System.out.println("   c-public <conversation> - select public conversation to join.");
    System.out.println("   c-private <conversation> - select private conversation to join");
    System.out.println("Message commands:");
    System.out.println("   m-add <body>     - add a new message to the current conversation.");
    System.out.println("   m-list-all       - list all messages in the current conversation.");
    System.out.println("   m-show <count>   - show <count> previous messages.");
  }

  // Prompt for new command.
  private void promptForCommand() {
    System.out.print(PROMPT);
  }



  // Parse and execute a single command.
  private void doOneCommand(Scanner lineScan) {
    final Scanner tokenScan = new Scanner(lineScan.nextLine());
    if (!tokenScan.hasNext()) {
      return;
    }

    //Setting class scanners for private methods to use
    lineScanner = lineScan;
    tokenScanner = tokenScan;

    final String token = tokenScanner.next();

    switch(token) {
      case EXIT:
        alive = false;
        break;
      case HELP:
        help();
        break;
      case SIGN_IN:
        signIn();
        break;
      case SIGN_OUT:
        signOut();
        break;
      case CURRENT:
        showCurrent();
        break;
      case U_ADD:
        userAdd();
        break;
      case U_LIST_ALL:
        showAllUsers();
        break;
      case C_ADD:
        conversationAdd();
        break;
      case C_LIST_ALL:
        clientContext.conversation.showAllConversations();
        break;
      case C_SELECT:
        System.out.println("Please use c-private or c-public");
        break;
      case C_PUBLIC:
        joinPublicConversation();
        break;
      case C_PRIVATE:
        joinPrivateConversation();
        break;
      case M_ADD:
        messageAdd();
        break;
      case M_LIST_ALL:
        messageListAll();
        break;
//      case M_NEXT:
//        messageNext();
//        break;
      case M_SHOW:
        messageShow();
        break;
      default:
        noCommand(token);
        break;
    }
    tokenScanner.close();
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////SWITCH CASE METHODS/////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  private void signIn(){
    if (!tokenScanner.hasNext()) {
      System.out.println("ERROR: No user name supplied.");
    } else {
      String userName = tokenScanner.nextLine().trim();
      System.out.print("Please enter the password: ");
      String password = lineScanner.next().trim();
      signInUser(userName, password);
    }
  }

  private void signOut() {
    if (!clientContext.user.hasCurrent()) {
      System.out.println("ERROR: Not signed in.");
    } else {
      signOutUser();
    }
  }

  private void userAdd() {
    if (!tokenScanner.hasNext()) {
      System.out.println("ERROR: Username not supplied.");
    } else {
      System.out.print("Please enter a password: ");
      String password = lineScanner.nextLine().trim();
      addUser(tokenScanner.nextLine().trim(), password);
    }
  }

  private void conversationAdd() {
    if (!clientContext.user.hasCurrent()) {
      System.out.println("ERROR: Not signed in.");
    } else {
      if (!tokenScanner.hasNext()) {
        System.out.println("ERROR: Conversation title not supplied.");
      } else {
        final String title = tokenScanner.nextLine().trim();
        String response = null;
        boolean isPrivate = false;
        String passHash = "defaultPassword123!";
        String salt = Password.generateSalt();
        while (response == null) {
          System.out.print("Add password to conversation? (y/n): ");
          response = lineScanner.nextLine().trim();
          if (response.equalsIgnoreCase("y")) isPrivate = true;
          else if (response.equalsIgnoreCase("n")) isPrivate = false;
        }
        if (isPrivate) {
          System.out.print("Please enter a password: ");
          passHash = Password.getHashCode(lineScanner.nextLine().trim(), salt);
        }
        clientContext.conversation.startConversation(
                title, clientContext.user.getCurrent().id, passHash, salt);
      }
    }
  }

  private void joinPublicConversation() {
    if (!tokenScanner.hasNext()) {
      System.out.println("ERROR: No conversation name supplied.");
    } else {
      String name = tokenScanner.nextLine().trim();
      joinConversation(name);
    }
  }

  private void joinPrivateConversation() {
    if (!tokenScanner.hasNext()) {
      System.out.println("ERROR: No conversation name supplied.");
    } else {
      String name = tokenScanner.nextLine().trim();
      System.out.print("Please enter the password: ");
      String password = lineScanner.next().trim();
      joinConversation(name, password);
    }
  }

  private void messageAdd() {
    if (!clientContext.user.hasCurrent()) {
      System.out.println("ERROR: Not signed in.");
    } else if (!clientContext.conversation.hasCurrent()) {
      System.out.println("ERROR: No conversation selected.");
    } else {
      if (!tokenScanner.hasNext()) {
        System.out.println("ERROR: Message body not supplied.");
      } else {
        clientContext.message.addMessage(
                clientContext.user.getCurrent().id,
                clientContext.conversation.getCurrentId(),
                tokenScanner.nextLine().trim());
      }
    }
  }

  private void messageListAll() {
    if (!clientContext.conversation.hasCurrent()) {
      System.out.println("ERROR: No conversation selected.");
    } else {
      clientContext.message.showAllMessages();
    }
  }

  // TODO: Implement m-next command to jump to an index in the message chain.
  private void messageNext() {
    Method.notImplemented();
    if (!clientContext.conversation.hasCurrent()) {
      System.out.println("ERROR: No conversation selected.");
    } else if (!tokenScanner.hasNextInt()) {
      System.out.println("Command requires an integer message index.");
    } else {
      clientContext.message.selectMessage(tokenScanner.nextInt());
    }
  }

  private void messageShow() {
    if (!clientContext.conversation.hasCurrent()) {
      System.out.println("ERROR: No conversation selected.");
    } else {
      final int count = (tokenScanner.hasNextInt()) ? tokenScanner.nextInt() : 1;
      clientContext.message.showMessages(count);
    }
  }

  private void noCommand(String token) {
    System.out.format("Command not recognized: %s\n", token);
    System.out.format(
            "Command line rejected: %s%s\n",
            token, (tokenScanner.hasNext()) ? tokenScanner.nextLine() : "");
    System.out.println("Type \"help\" for help.");
  }
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////END OF SWITCH CASE METHODS/////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  // Sign in a user.
  private void signInUser(String name, String password) {
    if (!clientContext.user.signInUser(name, password)) {
      System.out.println("Error: sign in failed (invalid name or password?)");
    }
  }

  // Sign out a user.
  private void signOutUser() {
    if (!clientContext.user.signOutUser()) {
      System.out.println("Error: sign out failed (not signed in?)");
    }else{
      if(clientContext.conversation.hasCurrent()){
        clientContext.conversation.setCurrent(null);
      }
    }
  }

  // Helper for showCurrent() - shows message info.
  // Changed this to show most recent message - this is
  // information the user would want to know over the head message
  private void showCurrentMessage() {
    if (clientContext.conversation.currentMessageCount() == 0) {
      System.out.println(" -- no messages in conversation --");
    } else {
      System.out.format(
          " conversation has %d messages.\n", clientContext.conversation.currentMessageCount());
      if (!clientContext.message.hasCurrent()) {
        System.out.println(" -- no current message --");
      } else {
        System.out.println("\nMost Recent Message:");
        clientContext.message.showRecent(clientContext.conversation.currentMessageCount());
      }
    }
  }

  // Show current user, conversation, and message(s)
  private void showCurrent() {
    if (clientContext.user.hasCurrent()) {
      System.out.println("User:");
      clientContext.user.showCurrent();
      System.out.println();
    }else{
      System.out.println("No current user.\n");
    }

    if (clientContext.conversation.hasCurrent()) {
      System.out.println("Conversation:");
      clientContext.conversation.showCurrent();
      showCurrentMessage();
      System.out.println();
    }else{
      System.out.println("No current conversation.");
    }
  }

  // Add a new user.
  private void addUser(String name, String password) {
    clientContext.user.addUser(name, password);
  }

  // Display all users known to server.
  private void showAllUsers() {
    clientContext.user.showAllUsers();
  }

  public boolean handleCommand(Scanner lineScanner) {

    try {
      promptForCommand();
      doOneCommand(lineScanner);
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during command processing. Check log for details.");
      LOG.error(ex, "Exception during command processing");
    }

    // "alive" may have been set to false while executing a command. Return
    // the result to signal if the user wants to keep going.

    return alive;
  }

  private void joinConversation(String name, String password) {
    final ConversationSummary previous = clientContext.conversation.getCurrent();
    ConversationSummary newCurrent;

    clientContext.conversation.joinConversation(name, password);
    newCurrent = clientContext.conversation.getCurrent();

    //Executed if the selected conversation exists and is new
    if(newCurrent != null && newCurrent != previous){
      clientContext.conversation.updateAllConversations(true);
      clientContext.message.resetCurrent(true);
    }
  }

  private void joinConversation(String name){
    joinConversation(name, "defaultPassword123!");
  }
}
