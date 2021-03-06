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
//

package codeu.chat;

import java.io.IOException;
import java.util.Scanner;


import codeu.chat.common.*;
import codeu.chat.server.NoOpRelay;
import codeu.chat.server.RemoteRelay;
import codeu.chat.server.Server;
import codeu.chat.util.Logger;
import codeu.chat.util.RemoteAddress;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;
import codeu.chat.util.connections.ServerConnectionSource;

final class ServerMain {

  private static final Logger.Log LOG = Logger.newLog(ServerMain.class);

  public static void main(String[] args) {

    Logger.enableConsoleOutput();

    try {
      Logger.enableFileOutput("chat_server_log.log");
    } catch (IOException ex) {
      LOG.error(ex, "Failed to set logger to write to file");
    }

    LOG.info("============================= START OF LOG =============================");

    Uuid id = null;
    try {
      id = Uuid.parse(args[0]);
    } catch (IOException ex) {
      System.out.println("Invalid id - shutting down server");
      System.exit(1);
    }

    final byte[] secret = Secret.parse(args[1]);
    final int myPort = Integer.parseInt(args[2]);

    final RemoteAddress relayAddress = args.length <= 3  ? null : RemoteAddress.parse(args[3]);

    Scanner scan = new Scanner(System.in);
    System.out.println("\n\n Would you like to connect to your own MongoDB database (y/n)? Otherwise, it will connect to the default database.");
    char q = scan.next().charAt(0);
    String info = null;
    if(q == 'y' || q == 'Y'){
        System.out.println("Please enter USERNAME:PASSWORD:DATABASE_PATH. For example, Bob:Password1234:TestDatabase.");
        info = scan.next();
    }
    scan.close();

    final String databaseInfo = info;

    try (
        final ConnectionSource serverSource = ServerConnectionSource.forPort(myPort);
        final ConnectionSource relaySource = relayAddress == null ? null : new ClientConnectionSource(relayAddress.host, relayAddress.port)
    ) {

      LOG.info("Starting server...");
      runServer(id, secret, serverSource, relaySource, databaseInfo);

    } catch (IOException ex) {

      LOG.error(ex, "Failed to establish connections");
    }
  }

  private static void runServer(Uuid id,
                                byte[] secret,
                                ConnectionSource serverSource,
                                ConnectionSource relaySource,
                                String databaseInfo){

    final Relay relay = relaySource == null ? new NoOpRelay() : new RemoteRelay(relaySource);

    final Server server = new Server(id, secret, relay, databaseInfo);

    LOG.info("Created server.");

    while (true) {

      try {

        LOG.info("Established connection...");
        final Connection connection = serverSource.connect();
        LOG.info("Connection established.");

        server.handleConnection(connection);

      } catch (IOException ex) {
        LOG.error(ex, "Failed to establish connection.");
      }
    }
  }
}
