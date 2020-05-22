package sk.tuke.gamestudio.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.taptiles.core.*;
import sk.tuke.gamestudio.service.*;

import java.util.Date;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping("/taptiles")
public class TaptilesController {
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private CommentService commentService;

    private Field field;

    private String playerName;

    private boolean marking;

    public Point highlightedPoint = null;

    @RequestMapping
    public String taptiles(String row, String column, Model model) {
        if (field == null)
            newGame(4);
        if (!(row == null || column == null))
            process(row, column);
        if (field.getGameState() == GameState.WON || field.getGameState() == GameState.LOST) {
            scoreService.addScore(new Score("taptiles", playerName, field.getScore(), new Date()));
        }
        prepareCommentModel(model);
        return "taptiles";
    }

    @RequestMapping("/hint")
    public String hint(Model model) {
        if (highlightedPoint != null) {
            field.checkConnects(highlightedPoint.getX(), highlightedPoint.getY(), true);
        }
        prepareCommentModel(model);
        return "taptiles";
    }

    @RequestMapping("/back")
    public String back(Model model) {
        if (field.undo()) {
            field.setScore(field.getScore() - field.getLastScore());
            field.setBonusMulti(0);
        }
        prepareCommentModel(model);
        return "taptiles";
    }

    @RequestMapping("/menu")
    public String menu(Model model) {
        prepareRatingModel(model);
        return "menu";
    }

    @RequestMapping("/player")
    public String Player(Model model) {
        return "player";
    }

    @RequestMapping("menu/rating")
    public String rate(@RequestParam(value = "rate", required = false) int rate, Model model) {
        try {
            ratingService.setRating(new Rating(playerName, "taptiles", rate, new Date()));
        } catch (RatingException e) {
            e.printStackTrace();
        }
        prepareRatingModel(model);
        return "menu";
    }

    public void process(String row, String column) {
        field.unHint();
        {
            try {
                int x = Integer.parseInt(row);
                int y = Integer.parseInt(column);

                if (x >= 0 && x < field.getSizeX() && y >= 0 && y < field.getSizeY()) {
                    if (field.getTile(new Point(x, y)).getState() == TileState.REMOVED) return;

                    if (highlightedPoint == null) {
                        highlightedPoint = new Point(x, y);

                        if (field.getTile(highlightedPoint).getState() == TileState.REMOVED) {
                            highlightedPoint = null;
                        } else {
                            if (field.getTile(highlightedPoint).getState() == TileState.GROUND)
                                field.getTile(highlightedPoint).setState(TileState.EXCITED);
                        }
                    } else {
                        if (!field.connect(highlightedPoint, new Point(x, y))) {
                            field.getTile(highlightedPoint).setState(TileState.GROUND);

                            highlightedPoint = new Point(x, y);

                            field.getTile(highlightedPoint).setState(TileState.EXCITED);
                            return;
                        }
                        highlightedPoint = null;
                    }

                }
            } catch (NumberFormatException e) {
                System.out.println("Wrong input format");
            }
            field.checkLost();
        }
    }

    @RequestMapping("/new")
    public String newGame(Model model, @RequestParam(value = "difficulty", required = false) String diff) {
        try {
            switch (diff) {
                case "easy":
                    newGame(4);
                    break;
                case "medium":
                    newGame(6);
                    break;
                case "hard":
                    newGame(8);
                    break;
                default:
                    newGame(4);
            }
        } catch (NullPointerException e) {
            return "difficulty";
        }
        prepareCommentModel(model);
        return "taptiles";
    }

    @RequestMapping("/hof")
    public String hofMake(Model model) {
        prepareModel(model);
        return "hof";
    }

    @RequestMapping("/addComment")
    public String addComment(String comment, Model model) {
        try {
            commentService.addComment(new Comment(playerName, "taptiles", comment, new Date()));
        } catch (NullPointerException | CommentException ignored) {
        }
        prepareCommentModel(model);
        return "taptiles";
    }

    @RequestMapping("/addPlayer")
    public String addPlayer(String player, Model model) {
        if (!player.isEmpty()) {
            this.playerName = player;
        } else {
            this.playerName = "Mr.Random";
        }
        prepareRatingModel(model);
        return "menu";
    }

    public boolean isMarking() {
        return marking;
    }


    public GameState getGameState() {
        return field.getGameState();
    }

    public String getHtmlField() {
        StringBuilder sb = new StringBuilder();
        if (field.getGameState() == GameState.PLAYING) {
            for (int row = 0; row < field.getSizeX(); row++) {
                sb.append("<div class='column'>\n");
                for (int column = 0; column < field.getSizeY(); column++) {
                    Tile tile = field.getTile(row, column);
                    sb.append("<div class='row'>\n");
                    if (field.equals(this.field))
                        sb.append("<a href='" +
                                String.format("/taptiles?row=%s&column=%s", row, column)
                                + "'>\n");
                    sb.append("<div style='width:calc(100vw/" + getTileSize() + "); " +
                            "height:calc(100vw/" + getTileSize() + "') " +
                            "class='" + getImageClass(tile) + "'>");
                    sb.append(getImageName(tile));
                    sb.append("</div>");
                    if (field.equals(this.field))
                        sb.append("</a>\n");
                    sb.append("</div>\n");
                }
                sb.append("</div>\n");
            }
        } else if (field.getGameState() == GameState.WON) {
            sb.append("<div class='radial-gradient' style='width:calc(100vw/3);" +
                    " font-size:3vh; flex-direction:column; " +
                    "height:calc(100vw/3); text-align:center; font-weight:bold'>");
            sb.append("You won.");
            sb.append("<br>");
            sb.append("Final score was: " + field.getScore());
            sb.append("<div class='radial-gradient' style='font-size:3vh'>");
            sb.append("<a href='/taptiles/new'>");
            sb.append("PLAY AGAIN");
            sb.append("</a>");
            sb.append("</div>");
            sb.append("<div class='radial-gradient' style='font-size:3vh'>");
            sb.append("<a href='/taptiles/menu'>");
            sb.append("MENU");
            sb.append("</a>");
            sb.append("</div>");
            sb.append("</div>");
        } else {
            sb.append("<div class='radial-gradient' style='width:calc(100vw/3);" +
                    " font-size:3vh; flex-direction:column; " +
                    "height:calc(100vw/3); text-align:center; font-weight:bold'>");
            sb.append("You lost.");
            sb.append("<br>");
            sb.append("Final score was: " + field.getScore());
            sb.append("<div class='radial-gradient' style='font-size:3vh'>");
            sb.append("<a href='/taptiles/new'>");
            sb.append("PLAY AGAIN");
            sb.append("</a>");
            sb.append("</div>");
            sb.append("<div class='radial-gradient' style='font-size:3vh'>");
            sb.append("<a href='/taptiles/menu'>");
            sb.append("MENU");
            sb.append("</a>");
            sb.append("</div>");
            sb.append("</div>");
        }
        return sb.toString();
    }

    public String getPlayerScore() {
        int score = field.getScore();
        StringBuilder sbb = new StringBuilder();
        sbb.append("Score: ");
        sbb.append(score);
        return sbb.toString();
    }

    public String getRatingField(Model model) {
        prepareRatingModel(model);
        return "menu";
    }

    public int getTileSize() {
        return field.getSizeX() * 3;
    }

    private Character getImageName(Tile tile) {
        if (tile.getState() == TileState.REMOVED)
            return ' ';
        return Character.toUpperCase(tile.getSymbol());
    }

    private String getImageClass(Tile tile) {
        if (tile.getState() == TileState.REMOVED)
            return "radial-gradient-removed";
        if (tile.getState() == TileState.EXCITED)
            return "radial-gradient-excited";
        if (tile.getState() == TileState.HINTED)
            return "radial-gradient-hinted";
        return "radial-gradient";
    }

    private void prepareModel(Model model) {
        try {
            model.addAttribute("scores", scoreService.getBestScores("taptiles"));
        } catch (NullPointerException | ScoreException e) {
            model.addAttribute("scores", null);
        }
    }

    private void prepareRatingModel(Model model) {
        try {
            model.addAttribute("rating", ratingService.getAverageRating("taptiles"));
        } catch (NullPointerException | RatingException e) {
            model.addAttribute("rating", null);
        }
    }

    private void prepareCommentModel(Model model) {
        try {
            model.addAttribute("comments", commentService.getComments("taptiles"));
        } catch (NullPointerException | CommentException e) {
            model.addAttribute("comments", null);
        }
    }

    private void newGame(int diff) {
        field = new Field(diff, diff);
    }
}
