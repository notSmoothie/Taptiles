package sk.tuke.gamestudio.game.taptiles.consoleui;

import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.taptiles.core.*;
import sk.tuke.gamestudio.services.ScoreService;
import sk.tuke.gamestudio.services.ScoreServiceJDBC;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ConsoleUI {
    private static final String GAME_NAME = "taptiles";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    public static final String RED_BOLD_BRIGHT = "\033[1;91m";
    private ScoreService scoreService = new ScoreServiceJDBC();

    private static Pattern INPUT_PATTERN;

    private final Field field;
    private Point highlightedPoint = null;

    public ConsoleUI(Field field) {
        this.field = field;

        INPUT_PATTERN = Pattern.compile("([A-" + (char) (field.getSizeX() + 'A' - 1) + "])([1-" + field.getSizeY() + 1 + "])");

    }

    public void play() {

        //printScores();

        while (true) {
            printField();
            if (field.getGameState() == GameState.WON)
                break;
            processInput();
        }
        System.out.println("You won!!!");
        //scoreService.addScore(new Score(System.getProperty("user.name"), field.getScore(), GAME_NAME, new Date()));

    }

    private void processInput() {
        while (true) {
            System.out.print(ANSI_YELLOW + "Enter input " + ANSI_RESET + "(e.g. A1, E(" + ANSI_CYAN + "X" + ANSI_RESET + ")it, (" + ANSI_CYAN + "R" + ANSI_RESET + ")estart, (" + ANSI_CYAN + "U" + ANSI_RESET + ")ndo): ");
            String input = new Scanner(System.in).nextLine().trim().toUpperCase();

            if ("X".equals(input))
                System.exit(0);

            if ("R".equals(input)) {
                field.Reset();
                System.out.println("Game was reset.");
                break;
            }

            if ("U".equals(input)) {
                if (!field.Undo())
                    System.out.println("There is nothing to undo!");
                else
                    System.out.println("Undone");
                break;
            }

            Matcher matcher = INPUT_PATTERN.matcher(input);
            if (matcher.matches()) {
                try {
                    int x = matcher.group(1).charAt(0) - 'A';
                    int y = Integer.parseInt(matcher.group(2)) - 1;
                    if (x >= 0 && x < field.getSizeX() && y >= 0 && y < field.getSizeY()) {
                        if (highlightedPoint == null) {
                            highlightedPoint = new Point(x, y);
                            if (field.getTile(highlightedPoint).getState() == TileState.GROUND)
                                field.getTile(highlightedPoint).setState(TileState.EXCITED);
                        } else {
                            if (!field.Connect(highlightedPoint, new Point(x, y))) {
                                field.getTile(highlightedPoint).setState(TileState.GROUND);
                            }
                            highlightedPoint = null;
                        }
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ANSI_RED + "Nesprávny formát vstupu" + ANSI_RESET);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(ANSI_RED + "Nesprávny formát vstupu" + ANSI_RESET);
                }
            } else {
                System.out.println(ANSI_RED + "Nesprávny vstup!" + ANSI_RESET);
            }
        }
    }


    private void printField() {
        printFieldHeader();
        printFieldBody();
    }

    private void printFieldHeader() {
        System.out.print("    " + ANSI_PURPLE);
        for (int x = 0; x < field.getSizeX(); x++) {
            System.out.print((char) (x + 'A') + " ");
        }
        System.out.println(ANSI_RESET);
    }

    private void printFieldBody() {
        for (int y = 0; y < field.getSizeY(); y++) {
            System.out.print(ANSI_PURPLE + (y + 1) + ANSI_RESET + "  |");
            for (int x = 0; x < field.getSizeX(); x++) {
                printTile(x, y);
            }
            System.out.println();
        }
    }

    private void printTile(int x, int y) {
        Tile t = field.getTile(x, y);
        switch (t.getState()) {
            case GROUND:
                System.out.print(t.getSymbol() + "|");
                break;
            case EXCITED:
                System.out.print(RED_BOLD_BRIGHT + Character.toUpperCase(t.getSymbol()) + ANSI_RESET + "|");
                break;
            case REMOVED:
                System.out.print(" |");
                break;
        }
    }

    private void printScores() {
        List<Score> scores = scoreService.getTopScores(GAME_NAME);

        Collections.sort(scores);

        System.out.println("Top scores:");
        for (Score s : scores) {
            System.out.println(s);
        }
    }


}

