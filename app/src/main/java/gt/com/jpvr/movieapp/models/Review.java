package gt.com.jpvr.movieapp.models;

/**
 * Created by Juan Pablo Villegas on 4/14/2018.
 * Model for a single Review.
 */


public class Review {
    private long id;
    private String author;
    private String content;

    public Review(long id) { this.id = id; }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
