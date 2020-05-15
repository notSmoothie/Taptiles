package sk.tuke.gamestudio.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@NamedQuery( name = "Comment.getComments",
        query = "SELECT c FROM Comment c WHERE c.game=:game")

public class Comment {
    @Id
    @GeneratedValue
    private int ident;
    private String player;
    private String game;
    private String comment;
    private Date commentedon;

    public Comment() {}

    public Comment(String player, String game, String comment, Date commentedon) {
        this.game = game;
        this.player = player;
        this.comment = comment;
        this.commentedon = commentedon;
    }

    public int getIdent() { return ident; }

    public void setIdent(int ident) { this.ident = ident; }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCommentedon() {
        return commentedon;
    }

    public void setCommentedon(Date commentedon) {
        this.commentedon = commentedon;
    }

    @Override
    public String toString() {
        return "Score{" +
                "ident=" + ident + '\'' +
                "game='" + game + '\'' +
                ", player='" + player + '\'' +
                ", comment=" + comment +
                ", commentedon=" + commentedon +
                '}';
    }
}
