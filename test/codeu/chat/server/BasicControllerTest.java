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

import static org.junit.Assert.*;

import codeu.chat.database.ChatAppDatabaseConnection;
import org.junit.*;

import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

public final class BasicControllerTest {

  private Model model;
  private BasicController controller;
  private static ChatAppDatabaseConnection database;

  @BeforeClass
  public static void onlyOnce() {
    database = new ChatAppDatabaseConnection("Tester", "jJZn8LnLucZUJqph", "TesterDatabase");
  }

  @Before
  public void doBefore() {
    model = new Model();
    controller = new Controller(Uuid.NULL, model, database);
  }

  @Test
  public void testAddUser() {

    final User user = controller.newUser("user", "TestPasswordHash", "saltCode");

    assertFalse("Check that user has a valid reference", user == null);
  }

  @Test
  public void testAddConversation() {

    final User user = controller.newUser("user", "TestPasswordHash", "saltCode");

    assertFalse("Check that user has a valid reference", user == null);

    final Conversation conversation =
        controller.newConversation("conversation", user.id, "pashHash", "salt");

    assertFalse("Check that conversation has a valid reference", conversation == null);
  }

  @Test
  public void testAddMessage() {

    final User user = controller.newUser("user", "TestPasswordHash", "saltCode");

    assertFalse("Check that user has a valid reference", user == null);

    final Conversation conversation =
        controller.newConversation("conversation", user.id, "passHash", "salt");

    assertFalse("Check that conversation has a valid reference", conversation == null);

    final Message message = controller.newMessage(user.id, conversation.id, "Hello World");

    assertFalse("Check that the message has a valid reference", message == null);
  }

  /**
   * I could have wrote this is a DatabaseTest class, but I decided that
   * since I had to enter a database as a parem to the Controller, I might
   * as well test it here. -Jacob Warner
   */
  @Test
  public void testWriteDatabase() {

    final User user = controller.newUser("user", "TestPasswordHash", "saltCode");
    final Conversation conversation =
            controller.newConversation("conversation", user.id, "passHash", "salt");
    final Message message = controller.newMessage(user.id, conversation.id, "Hello World");

    assertTrue("Check the writing of a user to the database.", database.write(user));
    assertTrue("Check the writing of a conversation to the database.", database.write(conversation));
    assertTrue("Check the writing of a  to the database.", database.write(message, conversation.id));
  }

  /**
   * Fongo (in-memory java implementation of MongoDB) wasn't working for testing, so I decided to
   * create a different database purely for testing. However, the database is free, so I am limited
   * on space. Hence, the cleaning up. -Jacob Warner
   *  Jacob Warner
   */
  @AfterClass
  public static void cleanUp() {
    database.removeDatabaseCollections();
    database.getDatabase().drop();
  }
}
