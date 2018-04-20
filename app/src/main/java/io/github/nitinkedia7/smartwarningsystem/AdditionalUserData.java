package io.github.nitinkedia7.smartwarningsystem;


public class AdditionalUserData{
    private String fullName;
    private String isEngaged;
    private String token;
    private String currentSession;

    public AdditionalUserData() {
    }

    public AdditionalUserData(String fullName, String isEngaged, String token, String currentSession) {
        this.fullName = fullName;
        this.currentSession = currentSession;
        this.isEngaged = isEngaged;
        this.token = token;
    }

    public String getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(String currentSession) {
        this.currentSession = currentSession;
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