package io.github.nitinkedia7.smartwarningsystem;

import java.util.Date;


public class ClassReview {

    private String name;
    private String state;
    private String status;
    private String review;
    private String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ClassReview() {
    }

    public ClassReview(String name, String state, String status, String review, String uid) {
        this.state = state;
        this.name = name;
        this.status = status;
        this.review = review;
        this.uid = uid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

}
