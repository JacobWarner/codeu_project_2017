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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import codeu.chat.common.Password;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.Store;

public final class ClientUser {

  private static final Logger.Log LOG = Logger.newLog(ClientUser.class);

  private static final Collection<Uuid> EMPTY = Arrays.asList(new Uuid[0]);
  private final Controller controller;
  private final View view;

  private User current = null;

  private final Map<Uuid, User> usersById = new HashMap<>();

  // This is the set of users known to the server, sorted by name.
  private Store<String, User> usersByName = new Store<>(String.CASE_INSENSITIVE_ORDER);

  public ClientUser(Controller controller, View view) {
    this.controller = controller;
    this.view = view;
  }

  // Validate the username string
  public static boolean isValidName(String userName) {
    if(userName == null || userName.equals("") || userName.length() <= 1 || userName.length() > 20){
      return false;
    }
    Pattern p = Pattern.compile("[\"()<>/;\\\\*%$^&+=:|~`]");
    Matcher m = p.matcher(userName);
    if (m.find()) {
      return false;
    }
    return true;
  }

  //Validate the password string
  public static boolean isValidPassword(String password) {
    if(password == null || password.equals("") || password.length() < 6 || password.length() > 64){
      return false;
    }
    Pattern p = Pattern.compile("[\"()<>/;\\\\*%$^&+=:|~` 0-9]");
    Matcher m = p.matcher(password);
    int specialCharacters = 0;
    while(m.find()){
      specialCharacters++;
    }
    if (specialCharacters < 2) {
      return false;
    }
    return true;
  }

  public boolean hasCurrent() {
    return (current != null);
  }

  public User getCurrent() {
    return current;
  }

  public boolean signInUser(String name, String password) {
    updateUsers();

    final User prev = current;
    if (name != null) {
      final User newCurrent = usersByName.first(name);
      if (newCurrent != null) {
        boolean userAccess = newCurrent.isPassword(password);
        if (userAccess) current = newCurrent;
      }
    }
    return (prev != current);
  }

  public boolean signOutUser() {
    boolean hadCurrent = hasCurrent();
    current = null;
    return hadCurrent;
  }

  public void showCurrent() {
    printUser(current);
  }

  public void addUser(String name, String password) {
    final boolean validInputs = isValidName(name) && isValidPassword(password);
    String salt = Password.generateSalt();
    String passwordHash = Password.getHashCode(password, salt);

    final User user = (validInputs) ? controller.newUser(name, passwordHash, salt) : null;

    if (user == null) {
      System.out.format(
          "Error: user not created - %s.\n", (validInputs) ? "server failure" : "bad input value");
      throwNull();
    } else {
      LOG.info("New user complete, Name= \"%s\" UUID=%s", user.name, user.id);
      updateUsers();
    }
  }

  public void showAllUsers() {
    updateUsers();
    for (final User u : usersByName.all()) {
      printUser(u);
    }
  }

  public User lookup(Uuid id) {
    return (usersById.containsKey(id)) ? usersById.get(id) : null;
  }

  public String getName(Uuid id) {
    final User user = lookup(id);
    return (user == null) ? null : user.name;
  }

  public Iterable<User> getUsers() {
    return usersByName.all();
  }

  public void updateUsers() {
    usersById.clear();
    usersByName = new Store<>(String.CASE_INSENSITIVE_ORDER);

    for (final User user : view.getUsersExcluding(EMPTY)) {
      usersById.put(user.id, user);
      usersByName.insert(user.name, user);
    }
  }

  public static void printUser(User user) {
    System.out.println(user.getUserInfo());
  }

  public User getUserByName(String uname) {
    usersByName = new Store<>(String.CASE_INSENSITIVE_ORDER);
    return usersByName.first(uname);
  }

  private static void throwNull(){
    throw new NullPointerException();
  }
}
