package io.github.nitinkedia7.smartwarningsystem;

public class Notification {

    private String state;       //Current State of user
    private String comment;     //Comment on the current state
    private String time;        //Time left to react to the notification
    private String status;      //Condition of the notification(Enabled/Disabled)

    public Notification() {
    }

    public Notification(String state, String comment, String time, String status) {
        this.state = state;
        this.comment = comment;
        this.time = time;
        this.status = status;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getState() {
        return state;
    }

    public void setState(String name) {
        this.state = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
