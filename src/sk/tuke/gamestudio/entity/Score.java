package sk.tuke.gamestudio.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Score implements Comparable<Score>, Serializable {
    private String player;

    private int points;

    private String game;

    private Date playedOn;

    public Score(String player, int points, String game, Date playedOn) {
        this.player = player;
        this.points = points;
        this.game = game;
        this.playedOn = playedOn;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public Date getPlayedOn() {
        return playedOn;
    }

    public void setPlayedOn(Date playedOn) {
        this.playedOn = playedOn;
    }

    @Override
    public int compareTo(Score o) {
        return o.points - this.points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return points == score.points &&
                Objects.equals(player, score.player) &&
                Objects.equals(game, score.game) &&
                Objects.equals(playedOn, score.playedOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, points, game, playedOn);
    }

    @Override
    public String toString() {
        return "Score{" +
                "player='" + player + '\'' +
                ", points=" + points +
                ", game='" + game + '\'' +
                ", playedOn=" + playedOn +
                '}';
    }
}
