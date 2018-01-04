package com.example.soumyadeb.cleanmeadmin;

/**
 * Created by Soumya Deb on 21-12-2017.
 */

public class RequestItem {
    private String dustbin_id, timestamp, image, zone, zone_name;

    public RequestItem() {
    }

    public RequestItem(String dustbin_id, String timestamp, String image, String zone, String zone_name) {
        this.dustbin_id = dustbin_id;
        this.timestamp = timestamp;
        this.image = image;
        this.zone = zone;
        this.zone_name = zone_name;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getZone_name() {
        return zone_name;
    }

    public void setZone_name(String zone_name) {
        this.zone_name = zone_name;
    }

    public String getZone() {
        return zone;
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
