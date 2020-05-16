package sk.tuke.gamestudio.server.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.ScoreService;

import java.util.List;

@RestController
@RequestMapping("/api/score")
public class ScoreRestService {
    @Autowired
    private ScoreService scoreService;

    @GetMapping("/{game}")
    public List<Score> getBestScores(@PathVariable String game) {
        return scoreService.getBestScores(game);
    }

    @PostMapping
    public void addScore(@RequestBody Score score) {
        scoreService.addScore(score);
    }
}