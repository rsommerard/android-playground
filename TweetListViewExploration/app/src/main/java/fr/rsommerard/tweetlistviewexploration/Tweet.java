package fr.rsommerard.tweetlistviewexploration;

/**
 * Created by Romain on 17/10/2016.
 */

public class Tweet {
    private int color;
    private String pseudo;
    private String text;

    public Tweet(int color, String pseudo, String text) {
        this.color = color;
        this.pseudo = pseudo;
        this.text = text;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
