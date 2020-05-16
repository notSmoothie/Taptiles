package sk.tuke.gamestudio.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.taptiles.core.Field;
import sk.tuke.gamestudio.game.taptiles.core.GameState;
import sk.tuke.gamestudio.game.taptiles.core.Tile;
import sk.tuke.gamestudio.service.ScoreService;

import java.util.Date;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping("/taptiles")
public class TaptilesController {
    @Autowired
    private ScoreService scoreService;

    @Autowired
    private UserController userController;

    private Field field;

    private boolean marking;

    @RequestMapping
    public String taptiles(String row, String column) {
        if (field == null)
            newGame();
//        try {
//            if (marking)
//                field.markTile(Integer.parseInt(row), Integer.parseInt(column));
//            else {
//                if (field.getState() == GameState.PLAYING) {
//                    field.openTile(Integer.parseInt(row), Integer.parseInt(column));
//                    if (userController.isLogged() && field.getState() == GameState.SOLVED) {
//                        scoreService.addScore(new Score(
//                                userController.getLoggedUser(),
//                                field.getScore(),
//                                "taptiles",
//                                new Date()
//                        ));
//                    }
//                }
//            }
//        } catch (NumberFormatException e) {
//            //Jaro: Zle poslane nic sa nedeje
//            e.printStackTrace();
//        }
        return "taptiles";
    }

    @RequestMapping("/new")
    public String newGame(Model model) {
        newGame();
        prepareModel(model);
        return "taptiles";
    }

    @RequestMapping("/mark")
    public String changeMark(Model model) {
        marking = !marking;
        prepareModel(model);
        return "taptiles";
    }

    public boolean isMarking() {
        return marking;
    }

    public GameState getGameState() {
        return field.getGameState();
    }

    //Najidealnejsi postup
    public String getHtmlField() {
        StringBuilder sb = new StringBuilder();
        sb.append("THIS CHANGES\n");
        sb.append("<div class='container'>\n");
        for (int row = 0; row < field.getSizeX(); row++) {
            sb.append("<div class='row'>\n");
            for (int column = 0; column < field.getSizeY(); column++) {
                Tile tile = field.getTile(row, column);
                sb.append("<div class='column'>\n");
                if (field.equals(this.field))
                    sb.append("<a href='" +
                            String.format("/taptiles?row=%s&column=%s", row, column)
                            + "'>\n");
                sb.append("<img src='/images/taptiles/" + getImageName(tile) + ".png'>");
                if (field.equals(this.field))
                    sb.append("</a>\n");
                sb.append("</div>\n");
            }
            sb.append("</div>\n");
        }
        sb.append("</div>\n");

        return sb.toString();
    }

    private String getImageName(Tile tile) {
        switch (tile.getState()) {
            case EXCITED:
                return "closed";
            case GROUND:
                return "marked";
            case REMOVED:
                return "removed";
        }
        throw new IllegalArgumentException("State is not supported " + tile.getState());
    }

    private void prepareModel(Model model) {
        model.addAttribute("scores", scoreService.getBestScores("taptiles"));
    }

    private void newGame() {
        field = new Field(4, 4);
    }
}
