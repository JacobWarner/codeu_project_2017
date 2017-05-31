package projectgroup32.googlecodeu.androidguiclient;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by travis on 3/17/17.
 */

public class ConversationListAdapter extends BaseAdapter {
    private Activity activity;
    protected ArrayList<AndroidClientConversation> data;
    private static LayoutInflater inflater=null;

    public ConversationListAdapter(Activity a, ArrayList<AndroidClientConversation> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.conversation_item, null);

        TextView title = (TextView)vi.findViewById(R.id.textConversationTitle); // title
        TextView lastMsg = (TextView)vi.findViewById(R.id.textConversationLastMsg); // title
        AndroidClientConversation c = data.get(position);

        // Setting all values in listview
        title.setText(c.getTitle());
        lastMsg.setText(c.getLastMessage());
        return vi;
    }
}
