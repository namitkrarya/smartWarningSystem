package io.github.nitinkedia7.smartwarningsystem;

/**
 * Created by nk7 on 11/04/18.
 */

public class StudentState {

    private String name;
    private Integer state;
    private String isBlacklisted;
    private String review;
    private String uid;

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

    public StudentState() {
    }

    public StudentState(String name, Integer state, String isBlacklisted, String review, String uid) {
        this.name = name;
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