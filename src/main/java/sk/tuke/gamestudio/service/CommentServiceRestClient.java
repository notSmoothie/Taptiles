package sk.tuke.gamestudio.service;

import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.entity.Comment;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommentServiceRestClient implements CommentService {
    private static final String URL = "http://localhost:8080/api/comment";

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public void addComment(Comment comment) {
        restTemplate.postForEntity(URL, comment, Comment.class);
    }

    @Override
    public List<Comment> getComments(String game) throws CommentException {
        return Arrays.asList(Objects.requireNonNull(restTemplate.getForEntity(URL + "/" + game, Comment[].class).getBody()));
    }
}