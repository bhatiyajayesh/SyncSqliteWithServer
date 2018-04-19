package com.example.jayesh.syncsqlitewithserver;

/**
 * Created by Jayesh on 19-Apr-18.
 */

public class Contact {

    private String name;
    private int sync_status;

    Contact(String name, int sync_status) {
        this.setName(name);
        this.setSync_status(sync_status);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSync_status() {
        return sync_status;
    }

    public void setSync_status(int sync_status) {
        this.sync_status = sync_status;
    }
}
