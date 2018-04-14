package io.github.nitinkedia7.smartwarningsystem;

/**
 * Created by namit on 13/4/18.
 */

public class Alert {

    public Alert() {
    }

    public Alert(String body, String title) {
        this.body = body;
        this.title = title;
    }
    public String title;
    public String body;
}
