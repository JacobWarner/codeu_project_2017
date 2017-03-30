package projectgroup32.googlecodeu.androidguiclient;

/**
 * Created by travis on 3/17/17.
 */

public class AndroidClientMessage {
    private String author;
    private String content;
    private String time;

    public AndroidClientMessage(String author, String content, String time) {
        this.author = author;
        this.content = content;
        this.time = time;
    }

    public String getAuthor(){
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }
}
