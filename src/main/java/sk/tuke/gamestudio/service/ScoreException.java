package sk.tuke.gamestudio.service;

import java.sql.SQLException;

public class ScoreException extends RuntimeException {
    public ScoreException(SQLException message) {
        super(message);
    }

    public ScoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
