package io.github.nitinkedia7.smartwarningsystem;

public class Student {

    private String name;                //Student name
    private Integer state;              //Current student state
    private String isBlacklisted;       //Whether the student has responded to all the alerts or not
    private String review;              //Review given by the professor
    private String uid;                 //uid of the student
    private Integer blacklistedState;   //Last state when the student was blacklisted

    public Integer getBlacklistedState() { return blacklistedState; }

    public void setBlacklistedState(Integer blacklistedState) { this.blacklistedState = blacklistedState; }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getIsBlacklisted() {
        return isBlacklisted;
    }

    public void setIsBlacklisted(String isBlacklisted) {
        this.isBlacklisted = isBlacklisted;
    }

    public Student() {
    }

    public Student(String name, Integer state, String isBlacklisted, String review, String uid, Integer blacklistedState) {
        this.name = name;
        this.blacklistedState = blacklistedState;
        this.state = state;
        this.isBlacklisted = isBlacklisted;
        this.review = review;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}