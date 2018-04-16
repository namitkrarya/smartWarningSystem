package io.github.nitinkedia7.smartwarningsystem;

public class notification {
    private String state, comment;
    private String time;
    private String status;

    public notification() {
    }

    public notification(String state, String comment, String time, String status) {
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
