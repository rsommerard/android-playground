package fr.rsommerard.cardviewexploration;

/**
 * Created by Romain on 16/10/2016.
 */
public class MyObject {
    private String text;
    private String imageUrl;

    public MyObject(String text, String imageUrl) {
        this.text = text;
        this.imageUrl = imageUrl;
    }

    //getters & setters

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}