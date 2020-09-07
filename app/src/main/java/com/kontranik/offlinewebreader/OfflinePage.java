package com.kontranik.offlinewebreader;

import java.io.Serializable;
import java.util.Date;

public class OfflinePage implements Serializable {

    private long id;
    private String origin;
    private String name;
    private String filename;
    private byte[] image;
    private float position;
    private Long created;

    public OfflinePage(String origin, String name, String filename, byte[] image, float position) {
        this.origin = origin;
        this.name = name;
        this.filename = filename;
        this.image = image;
        this.position = position;
        this.created = new Date().getTime();
    }

    public OfflinePage(long id, String origin, String name, String filename, byte[] image, float position, Long created) {
        this.id = id;
        this.origin = origin;
        this.name = name;
        this.filename = filename;
        this.image = image;
        this.position = position;
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {
        this.position = position;
    }


    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

}
