package projectgroup32.googlecodeu.androidguiclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by travis on 3/17/17.
 */

public class MessageActivity extends AppCompatActivity {
    ListView list;
    MessageListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        ArrayList<AndroidClientMessage> messages = new ArrayList<>();
        String message = "Hello??";
        for(int i = 1; i < 11; i++)
        {
            messages.add(i-1, new AndroidClientMessage("John Doe", message,
                    "Mar 1 " + i + ":00PM"));
            message += "Can you hear me now?";
        }

        list = (ListView)findViewById(R.id.listViewMessages);
        adapter = new MessageListAdapter(this, messages);
        list.setAdapter(adapter);

        // Click event for single list row
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }
}
