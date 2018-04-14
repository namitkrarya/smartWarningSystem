package io.github.nitinkedia7.smartwarningsystem;

import java.util.Date;


public class ClassStatus {

    private String state;
    private String name;
    private String status;

    public ClassStatus() {
    }

    public ClassStatus(String state, String status, String name) {
        this.state = state;
        this.name = name;
        this.status = status;
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

}
