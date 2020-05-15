package sk.tuke.gamestudio.service;

import org.springframework.transaction.annotation.Transactional;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Transactional
public class RatingServiceJPA implements RatingService {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void setRating(Rating rating) throws RatingException {
        boolean hasResult = entityManager.createNamedQuery("Rating.getRating")
                .setParameter("game", rating.getGame())
                .setParameter("player", rating.getPlayer()).getResultList().isEmpty();

        if (!hasResult) {
            ((Rating) (entityManager.createNamedQuery("Rating.getRating")
                    .setParameter("game", rating.getGame())
                    .setParameter("player", rating.getPlayer()).getSingleResult())).setRating(rating.getRating());
        } else {
            entityManager.persist(rating);
        }
    }

    @Override
    public int getAverageRating(String game) throws RatingException {
        return ((Double) entityManager.createNamedQuery("Rating.getAverageRating").setParameter("game", game).getSingleResult()).intValue();
    }

    @Override
    public int getRating(String game, String player) throws RatingException {
        return ((Rating) (entityManager.createNamedQuery("Rating.getRating")
                .setParameter("game", game)
                .setParameter("player", player).getSingleResult())).getRating();
    }
}