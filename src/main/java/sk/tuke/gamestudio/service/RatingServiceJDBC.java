package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Rating;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
    CREATE TABLE rating (
        player VARCHAR(64) NOT NULL,
        game VARCHAR(64) NOT NULL,
        points INTEGER NOT NULL,
        playedon TIMESTAMP NOT NULL
    );
     */


public class RatingServiceJDBC implements RatingService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "postgres";

    public static final String INSERT_RATING =
            "INSERT INTO rating (game, player, rating, ratedon) VALUES (?, ?, ?, ?)";

    public static final String SELECT_RATING =
            "SELECT game, player, rating, ratedon FROM rating WHERE game = ?";


    @Override
    public void setRating(Rating rating) throws RatingException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement ps = connection.prepareStatement(INSERT_RATING)) {
                ps.setString(1, rating.getGame());
                ps.setString(2, rating.getPlayer());
                ps.setInt(3, rating.getRating());
                ps.setDate(4, new Date(rating.getRatedon().getTime()));

                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RatingException("Error saving rating", e);
        }
    }

    @Override
    public int getRating(String game, String player) throws RatingException {
        int rating = 0;

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_RATING)) {
                ps.setString(1, game);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (rs.getString(1).equals(player) && rs.getString(2).equals(game)) {
                            rating = rs.getInt(3);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RatingException("Error loading rating", e);
        }

        return rating;
    }

    @Override
    public int getAverageRating(String game) throws RatingException {
        int rating = 0;
        int averct = 0;
        int average = 0;
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_RATING)) {
                ps.setString(1, game);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rating = +rs.getInt(3);
                        averct++;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RatingException("Error loading rating", e);
        }
        average = rating / averct;
        return average;
    }

}
