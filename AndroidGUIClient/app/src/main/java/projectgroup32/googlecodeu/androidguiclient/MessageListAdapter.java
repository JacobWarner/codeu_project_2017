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

public class MessageListAdapter extends BaseAdapter {
    private Activity activity;
    protected ArrayList<AndroidClientMessage> data;
    private static LayoutInflater inflater=null;

    public MessageListAdapter(Activity a, ArrayList<AndroidClientMessage> d) {
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
            vi = inflater.inflate(R.layout.message_item, null);

        TextView author = (TextView)vi.findViewById(R.id.textAuthor);
        TextView messageBody = (TextView)vi.findViewById(R.id.textMessageBody);
        TextView time = (TextView)vi.findViewById(R.id.textTime);
        AndroidClientMessage c = data.get(position);

        // Setting all values in listview
        author.setText(c.getAuthor());
        messageBody.setText(c.getContent());
        time.setText(c.getTime());
        return vi;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
