package gt.com.jpvr.movieapp.models;

/**
 * Created by Juan Pablo Villegas on 4/14/2018.
 * Model for a single Video.
 */

public class Video {
    private long id;
    private String key;
    private String name;
    private String site;
    private String size;
    private String type;

    public Video(long id) { this.id = id; }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
