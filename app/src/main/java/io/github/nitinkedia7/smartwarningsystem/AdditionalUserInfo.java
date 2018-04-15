package io.github.nitinkedia7.smartwarningsystem;


public class AdditionalUserInfo {
    private String fullName;
    private String isEngaged;
    private String token;

    public String getCurrentCourse() {
        return currentCourse;
    }

    public void setCurrentCourse(String currentCourse) {
        this.currentCourse = currentCourse;
    }

    private String currentCourse;

    public AdditionalUserInfo() {
    }

    public AdditionalUserInfo(String fullName, String isEngaged, String token, String currentCourse) {
        this.fullName = fullName;
        this.currentCourse = currentCourse;
        this.isEngaged = isEngaged;
        this.token = token;

    }
    public String getIsEngaged() {
        return isEngaged;
    }

    public void setIsEngaged(String isEngaged) {
        isEngaged = isEngaged;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}