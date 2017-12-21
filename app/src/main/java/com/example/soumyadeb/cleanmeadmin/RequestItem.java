package com.example.soumyadeb.cleanmeadmin;

/**
 * Created by Soumya Deb on 21-12-2017.
 */

public class RequestItem {
    private String dustbin_id, timestamp, image;

    public RequestItem() {
    }

    public RequestItem(String dustbin_id, String timestamp, String image) {
        this.dustbin_id = dustbin_id;
        this.timestamp = timestamp;
        this.image = image;
    }

    public String getDustbin_id() {
        return dustbin_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getImage() {
        return image;
    }

    public void setDustbin_id(String dustbin_id) {
        this.dustbin_id = dustbin_id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
