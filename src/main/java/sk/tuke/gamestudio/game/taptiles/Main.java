package sk.tuke.gamestudio.game.taptiles;

import sk.tuke.gamestudio.game.taptiles.consoleui.ConsoleUI;
import sk.tuke.gamestudio.game.taptiles.core.Field;

public class Main {
    public static void main(String[] args) {
        Field field = new Field(4, 4);
        ConsoleUI ui = new ConsoleUI(field);
        ui.play();
    }
}
