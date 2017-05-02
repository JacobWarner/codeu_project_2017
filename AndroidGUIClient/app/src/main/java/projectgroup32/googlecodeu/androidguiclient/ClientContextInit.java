package projectgroup32.googlecodeu.androidguiclient;

import android.util.Log;

import codeu.chat.client.ClientContext;
import codeu.chat.client.Controller;
import codeu.chat.util.RemoteAddress;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.ConnectionSource;

/**
 * Created by travis on 4/30/17.
 */

public class ClientContextInit {
    public static ClientContext init()
    {
        final RemoteAddress address = RemoteAddress.parse("10.0.2.2@2007");
        try {
            final ConnectionSource source = new ClientConnectionSource(address.host, address.port);
            final Controller controller = new Controller(source);
            final codeu.chat.client.View view = new codeu.chat.client.View(source);

            Log.i("Account Activity", "Creating client...");
            return new ClientContext(controller, view);

        } catch (Exception ex) {
            Log.e("Error", "ERROR: Exception setting up client. Check log for details.");
            Log.e(ex.toString(), "Exception setting up client.");
        }
        return null;
    }
}
