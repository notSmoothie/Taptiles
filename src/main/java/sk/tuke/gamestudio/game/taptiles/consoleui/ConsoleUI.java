package sk.tuke.gamestudio.game.taptiles.consoleui;

import org.springframework.beans.factory.annotation.Autowired;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.taptiles.core.*;
import sk.tuke.gamestudio.service.*;

import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ConsoleUI {
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private CommentService commentService;

    private static final String GAME_NAME = "taptiles";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_REDB = "\u001b[31;1m";
    public static final String RED_BOLD_BRIGHT = "\033[1;91m";

    private String playerName;

    private static Pattern INPUT_PATTERN;
    private static Pattern DIFF_PATTERN;


    private Field field;
    private Point highlightedPoint = null;
    private String Difficulty;
    private boolean inMenu;

    public ConsoleUI(Field field) {
        this.field = field;
        inMenu = true;
        DIFF_PATTERN = Pattern.compile("[1-3]");
    }

    public void play() {
        printBeforeGameScreen();

        System.out.println("What " + RED_BOLD_BRIGHT + "DIFFICULTY " + ANSI_RESET + "do you feel like " + ANSI_CYAN + "(1-3)" + ANSI_RESET + "?");
        clearConsoleStuff(8);
        System.out.print("Difficulty:  ");
        String diff = new Scanner(System.in).nextLine().trim();
        Matcher diffMatch = DIFF_PATTERN.matcher(diff);
        if (diffMatch.matches()) {
            switch (diff) {
                case "1": {
                    this.field = new Field(4, 4);
                    Difficulty = ANSI_GREEN + "EASY" + ANSI_RESET;
                    break;
                }
                case "2": {
                    this.field = new Field(6, 6);
                    Difficulty = ANSI_YELLOW + "MEDIUM" + ANSI_RESET;
                    break;
                }
                case "3": {
                    this.field = new Field(8, 8);
                    Difficulty = ANSI_RED + "HARD" + ANSI_RESET;
                    break;
                }
                default: {
                    this.field = new Field(4, 4);
                    Difficulty = "EASY";
                    System.out.print("Starting game at lowest difficulty.");
                }
            }
        } else {
            this.field = new Field(4, 4);
            System.out.print("Starting game at lowest difficulty.");
        }
        field.setStartMillis();
        INPUT_PATTERN = Pattern.compile("([A-" + (char) (field.getSizeX() + 'A' - 1) + "])([1-" + field.getSizeY() + 1 + "])");
        while (true) {
            printField();
            if (field.getGameState() == GameState.WON)
                break;
            processInput();
        }

        printAfterGameScreen();
    }

    private void processInput() {

        while (true && field.getGameState() == GameState.PLAYING) {
            System.out.println(ANSI_YELLOW + "Enter input " + ANSI_RESET + "(e.g. A1, E(" + ANSI_CYAN + "X" + ANSI_RESET + ")it, (" + ANSI_CYAN + "R" + ANSI_RESET + ")estart, (" + ANSI_CYAN + "U" + ANSI_RESET + ")ndo): ");
            String input = new Scanner(System.in).nextLine().trim().toUpperCase();

            if ("X".equals(input))
                System.exit(0);


            if ("S".equals(input)) {
                field = new Field(4, 4);
                ConsoleUI ui = new ConsoleUI(field);
                break;
            }

            if ("R".equals(input)) {
                field.reset();
                System.out.println("Game was reset.");
                field.setScore(0);
                break;
            }


            if ("U".equals(input)) {
                if (!field.undo())
                    System.out.println("There is nothing to undo!");
                else
                    field.setScore(field.getScore() - field.getLastScore());
                System.out.println("Undone");
                break;
            }

            Matcher matcher = INPUT_PATTERN.matcher(input);
            if (matcher.matches()) {
                try {
                    int x = matcher.group(1).charAt(0) - 'A';
                    int y = Integer.parseInt(matcher.group(2)) - 1;

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
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ANSI_RED + "Wrong input format" + ANSI_RESET);
                }
            } else {
                System.out.println(ANSI_RED + "Wrong input!" + ANSI_RESET);
            }
        }

    }

    //PRINTING STARTS HERE

    private void printField() {
        clearConsoleStuff(8);
        printFieldHeader();
        printFieldBody();
        printFieldFooter();
        clearConsoleStuff(7);
    }

    private void printFieldHeader() {
        System.out.println("Difficulty: " + Difficulty);
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

    private void clearConsoleStuff(int x) {
        for (int i = 0; i < x; i++)
            System.out.println(" ");
    }

    private void printFieldFooter() {
        System.out.println("-----------------");
        System.out.print("Score:   ");
        System.out.println(field.getScore());
        System.out.print("Time:    ");
        System.out.println(field.getPlayingTime());
        System.out.println("-----------------");
    }

    private void mainMenu() {
        if (inMenu = true) {
            clearConsoleStuff(6);
            System.out.println(ANSI_RED + "TAPTILES by SMODESIGN" + ANSI_RESET);
            System.out.println(ANSI_BLUE + "------------------------------------" + ANSI_RESET);
            System.out.println(" ");
            System.out.println(ANSI_BLUE + "MAIN MENU" + ANSI_RESET);
            System.out.println(" ");
            System.out.println(ANSI_REDB + "1. " + ANSI_RESET + "NEW GAME");
            System.out.println(ANSI_REDB + "2. " + ANSI_RESET + "HALL OF FAME");
            System.out.println(ANSI_REDB + "3. " + ANSI_RESET + "COMMENTS");
            System.out.println(ANSI_REDB + "4. " + ANSI_RESET + "RATE");
            System.out.println(ANSI_REDB + "5. " + ANSI_RESET + "CHANGE NAME");
            System.out.println(ANSI_REDB + "6. " + ANSI_RESET + "QUIT");

            clearConsoleStuff(4);
            String menuHandle = new Scanner(System.in).nextLine().trim();

            while ((!(menuHandle == "6")) && (inMenu == true)) {
                switch (menuHandle) {
                    case "1":
                        inMenu = false;
                        break;
                    case "2":
                        hofShow();
                        break;
                    case "3":
                        commentShow();
                        break;
                    case "4":
                        ratingAsk();
                        break;
                    case "5":
                        clearConsoleStuff(8);
                        System.out.print("Nickname:  ");
                        playerName = new Scanner(System.in).nextLine().trim().toUpperCase();
                        System.out.println("Name changed to:  " + playerName);
                        break;
                    case "6":
                        System.exit(0);
                    default:
                        mainMenu();
                }
                if (inMenu) {
                    clearConsoleStuff(2);
                    System.out.println("Press (" + ANSI_CYAN + "Enter" + ANSI_RESET + ") to go back to menu");
                    menuHandle = new Scanner(System.in).nextLine();
                }
            }
        } else {
            return;
        }
        clearConsoleStuff(4);
    }


    private void printBeforeGameScreen() {
        clearConsoleStuff(8);
        if (playerName == null) {
            clearConsoleStuff(4);
            System.out.println(ANSI_RED + "WELCOME TO TAPTILES" + ANSI_RESET);
            System.out.println("My name is " + ANSI_BLUE + "Smoothie" + ANSI_RESET + ". And I will be your guide today.\n\nWhat is your name?");
            clearConsoleStuff(8);
            System.out.print("Nickname:  ");
            playerName = new Scanner(System.in).nextLine().trim().toUpperCase();
        }
        clearConsoleStuff(12);
        System.out.println("Pleased to meet you " + playerName + "!");
        mainMenu();
    }

    private void printAfterGameScreen() {
        System.out.println("You WON !!");
        scoreShow();
        System.out.println("Press (" + ANSI_CYAN + "Enter" + ANSI_RESET + ") to go back to menu");
        String handle = new Scanner(System.in).nextLine().trim();
        hofShow();
        replayGame();
        commentShow();
        commentAsk();
        ratingShow();
        ratingAsk();
    }

    private void commentAsk() {
        clearConsoleStuff(8);
        System.out.println("What would you say about our game ?");
        System.out.print("Your comment : ");
        String comment = new Scanner(System.in).nextLine().trim();
        try {
            commentService.addComment(new Comment(playerName, GAME_NAME, comment, new Date()));
        } catch (CommentException e) {
            System.out.println("Something went terribly wrong");
            e.printStackTrace();
            commentAsk();
        }
        clearConsoleStuff(4);
    }

    private void commentShow() {
        clearConsoleStuff(8);
        System.out.println("People said about us : ");
        System.out.println("---------------------------------------------------------------------------------------------------");
        try {
            commentService.getComments(GAME_NAME)
                    .forEach(s -> System.out.println("[" + ANSI_CYAN + s.getCommentedon() + ANSI_RESET + "] " + ANSI_PURPLE + s.getPlayer() + ANSI_RESET + " said \'" + s.getComment() + "\'"));
        } catch (CommentException e) {
            System.out.println("Comments could not have been gotten.");
            e.printStackTrace();
        }
        System.out.println("---------------------------------------------------------------------------------------------------");
        clearConsoleStuff(6);
    }

    private void ratingAsk() {
        clearConsoleStuff(4);
        System.out.println("--------------------------------------------------------------");
        System.out.println("Please, take a second to rate our game (1-5)");
        System.out.println("--------------------------------------------------------------");
        clearConsoleStuff(9);
        System.out.print("Your rating : ");
        int rating = new Scanner(System.in).nextInt();
        if (rating >= 1 && rating <= 5) {
            try {
                ratingService.setRating(new Rating(playerName, GAME_NAME, rating, new Date()));
            } catch (RatingException e) {
                System.out.println("Wrong input!");
                e.printStackTrace();
                ratingAsk();
            }
        }
    }

    private void ratingShow() {
        clearConsoleStuff(8);
        System.out.println("--------------------------------------------------------------");
        System.out.print("People gave us an average rating of: ");
        try {
            System.out.println(ratingService.getAverageRating(GAME_NAME) + ".");
        } catch (RatingException e) {
            System.out.println("Average rating has not been found. Somebody probably sucks at programming.");
            e.printStackTrace();
        }
        System.out.println("--------------------------------------------------------------");
        clearConsoleStuff(4);
    }

    private void scoreShow() {
        clearConsoleStuff(4);
        System.out.print("Your score is: ");
        System.out.println(ANSI_YELLOW + field.getScore() + ANSI_RESET);
        try {
            scoreService.addScore(new Score(GAME_NAME, playerName, field.getScore(), new Date()));
        } catch (ScoreException e) {
            System.out.println("Could not post score to database.");
            e.printStackTrace();
        }
        clearConsoleStuff(4);
    }

    private void hofShow() {
        clearConsoleStuff(8);
        System.out.println("--------------------------------------------------------------");
        System.out.println("Hall of fame: ");
        try {
            scoreService.getBestScores(GAME_NAME)
                    .forEach(c -> System.out.println("[" + ANSI_CYAN + c.getPlayedon() + ANSI_RESET + "] " + ANSI_REDB + c.getPlayer() + ANSI_RESET + " : " + c.getPoints()));
        } catch (ScoreException e) {
            System.out.println("Could not load Hall of Fame");
            e.printStackTrace();
        }
        System.out.println("--------------------------------------------------------------");
        clearConsoleStuff(6);
    }

    private void replayGame() {
        clearConsoleStuff(8);
        System.out.println("Try again ? (" + ANSI_GREEN + "Y" + ANSI_RESET + ")es(" + ANSI_RED + "N" + ANSI_RESET + ")o");
        if (field.getGameState() == GameState.WON) {
            String input = new Scanner(System.in).nextLine().trim().toUpperCase();
            if ("Y".equals(input)) {
                field = new Field(4, 4);
                ConsoleUI ui = new ConsoleUI(field);
                play();

                return;

            } else if ("N".equals(input)) {
                return;
            } else {
                System.out.println("Wrong input!");
                replayGame();
            }
        }
    }


}