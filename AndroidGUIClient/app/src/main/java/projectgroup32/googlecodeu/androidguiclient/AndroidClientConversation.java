package projectgroup32.googlecodeu.androidguiclient;

/**
 * Created by travis on 3/17/17.
 */

public class AndroidClientConversation {
    private String title;
    private String lastMessage;

    public AndroidClientConversation(String title, String lastMessage)
    {
        this.title = title;
        this.lastMessage = lastMessage;
    }

    public String getTitle()
    {
        return title;
    }

    public String getLastMessage()
    {
        return lastMessage;
    }
}
