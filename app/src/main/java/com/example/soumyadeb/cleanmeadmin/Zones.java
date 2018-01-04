package com.example.soumyadeb.cleanmeadmin;

/**
 * Created by Soumya Deb on 03-01-2018.
 */

public class Zones {
    private String name, id, userId, type;

    public Zones(String name, String id, String userId, String type) {
        this.name = name;
        this.id = id;
        this.userId = userId;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }
}
