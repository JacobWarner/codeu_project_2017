package projectgroup32.googlecodeu.androidguiclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Created by travis on 3/17/17.
 */

public class ConversationActivity extends AppCompatActivity {
    ListView list;
    ConversationListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        ArrayList<AndroidClientConversation> conversations = new ArrayList<>();
        for(int i = 0; i < 10; i++)
        {
            conversations.add(i, new AndroidClientConversation("title " + i, "message"));
        }

        list = (ListView)findViewById(R.id.listViewConversations);
        adapter = new ConversationListAdapter(this, conversations);
        list.setAdapter(adapter);


        // Click event for single list row
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(ConversationActivity.this, MessageActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        getMenuInflater().inflate(R.menu.conversations_action_bar, menu);
        inflater.inflate(R.menu.conversations_menu, menu);
        MenuItem item = menu.findItem(R.id.conversations_spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter adapterSort = ArrayAdapter.createFromResource(this,
                R.array.conversations_sort_menu, android.R.layout.simple_spinner_item);

        adapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSort);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                return true;
            case R.id.menu_account:
                Intent intent = new Intent();
                startActivity(new Intent(ConversationActivity.this, AccountActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
