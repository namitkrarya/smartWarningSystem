package io.github.nitinkedia7.smartwarningsystem;


public class AdditionalUserInfo {
    private String userType;



    private String token;
    private String fullName;

    public AdditionalUserInfo(String userType, String fullName, String token) {
        this.fullName = fullName;
        this.userType = userType;
        this.token = token;

    }
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
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
