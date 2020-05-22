package sk.tuke.gamestudio.game.taptiles.core;

import java.util.Random;
import java.util.Stack;

public class Field {
    private Tile[][] grid;
    private Random random;
    private int sizeX, sizeY;
    private int tileCount;
    private Stack<Point> history;
    static Stack<Integer> scoreHistory = new Stack<Integer>();
    private GameState gameState;
    private int popTime;
    private long startMillis;
    private boolean conStarted;
    private int bonusMulti;
    private int score;


    public Field(int sizeX, int sizeY) {
        if (sizeX < 1 || sizeY < 1 || ((sizeX * sizeY) % 2 == 1))
            throw new ArrayIndexOutOfBoundsException();

        this.sizeX = sizeX;
        this.sizeY = sizeY;
        tileCount = sizeX * sizeY;
        gameState = GameState.PLAYING;
        conStarted = false;
        bonusMulti = 0;
        grid = new Tile[sizeX][sizeY];
        random = new Random();
        generateField();
        history = new Stack<Point>();

    }

    public void checkLost() {
        boolean lost = true;
        for (int y = 0; y < this.getSizeY(); y++) {
            for (int x = 0; x < this.getSizeX(); x++) {
                if (this.checkConnects(x, y, false)) {
                    lost = false;
                }
            }
        }
        if (lost == true && this.gameState != GameState.WON) {
            this.gameState = GameState.LOST;
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public void setBonusMulti(int i) {
        this.bonusMulti = i;
    }

    public Tile getTile(int x, int y) {
        return grid[x][y];
    }

    public Tile getTile(Point p) {
        return grid[p.getX()][p.getY()];
    }

    private void generateField() {
        Point point = new Point();
        int insertedTiles = 0;

        char currSymbol = 'a';
        do {
            for (int i = 0; i < 4; i++) {
                do {
                    point.setX(random.nextInt(sizeX));
                    point.setY(random.nextInt(sizeY));
                } while (getTile(point) != null);
                grid[point.getX()][point.getY()] = new Tile(currSymbol);
                insertedTiles++;
            }
            currSymbol++;
        } while (insertedTiles < tileCount - 4);

        for (int i = -1; i < sizeX + 1; i++)
            for (int j = 0 - 1; j < sizeY + 1; j++)
                if (i > -1 && i < sizeX && j > -1 && j < sizeX)
                    if (grid[i][j] == null)
                        grid[i][j] = new Tile(currSymbol);

    }


    //Returns TRUE if the axis bethween two points is clear
    private boolean twoPointAxisIsClear(Point p1, Point p2, Boolean p1IsTile, Boolean p2IsTile) {
        int min = 0;
        int max = 0;
        int axis = 0;

        //Ask which axis
        //Ask about states of middle points to make sure they are REMOVED

        if (p1.getX() == p2.getX()) {
            min = Math.min(p1.getY(), p2.getY());
            max = Math.max(p1.getY(), p2.getY());
            axis = p1.getX();

            for (int mover = min; mover <= max; mover++) {
                if (!((axis == p1.getX() && mover == p1.getY() && p1IsTile) || (axis == p2.getX() && mover == p2.getY() && p2IsTile))) {
                    if (this.getTile(axis, mover).getState() != TileState.REMOVED) {
                        return false;
                    }
                }
            }
        } else if (p1.getY() == p2.getY()) {
            min = Math.min(p1.getX(), p2.getX());
            max = Math.max(p1.getX(), p2.getX());
            axis = p1.getY();

            for (int mover = min; mover <= max; mover++) {
                if (!((mover == p1.getX() && axis == p1.getY() && p1IsTile) || (mover == p2.getX() && axis == p2.getY() && p2IsTile))) {
                    if (this.getTile(mover, axis).getState() != TileState.REMOVED) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean areNear(Point p1, Point p2) {
        if (p1.getY() == p2.getY()) {
            if (p1.getX() + 1 == p2.getX() || p1.getX() - 1 == p2.getX()) {
                return true;
            }
        }
        if (p1.getX() == p2.getX()) {
            return p1.getY() + 1 == p2.getY() || p1.getY() - 1 == p2.getY();
        }
        return false;
    }

    private boolean areSame(Point p1, Point p2) {
        return p1.getY() == p2.getY() && p1.getX() == p2.getX();
    }

    private boolean checkOuterX(Point p1, Point p2) {
        Point up1 = new Point(0, p1.getY());
        Point up2 = new Point(0, p2.getY());
        Point down1 = new Point(getSizeX() - 1, p1.getY());
        Point down2 = new Point(getSizeX() - 1, p2.getY());

        if (twoPointAxisIsClear(p1, up1, true, false)) {
            if (twoPointAxisIsClear(p2, up2, true, false)) {
                return true;
            }
        }

        if (twoPointAxisIsClear(p1, down1, true, false)) {
            return twoPointAxisIsClear(p2, down2, true, false);
        }
        return false;
    }

    private boolean checkOuterY(Point p1, Point p2) {
        Point left1 = new Point(p1.getX(), 0);
        Point left2 = new Point(p2.getX(), 0);
        Point right1 = new Point(p1.getX(), getSizeX() - 1);
        Point right2 = new Point(p2.getX(), getSizeX() - 1);

        if (twoPointAxisIsClear(p1, left1, true, false)) {
            if (twoPointAxisIsClear(p2, left2, true, false)) {
                return true;
            }
        }

        if (twoPointAxisIsClear(p1, right1, true, false)) {
            return twoPointAxisIsClear(p2, right2, true, false);
        }

        return false;
    }


    /**
     * BLOCK OF LOGICK INCOMING
     */


    //Checks whether the lines bethween all the points are clear
    public boolean checkConnect(Point p1, Point p2) {

        Tile t1 = getTile(p1);
        Tile t2 = getTile(p2);

        if (t1.getState() == TileState.REMOVED || t2.getState() == TileState.REMOVED) {
            return false;
        }

        if (t2.getSymbol() != t1.getSymbol()) {
            return false;
        }

        if (areSame(p1, p2)) {
            return false;
        }

        if (areNear(p1, p2)) {
            return true;
        }

        if (checkOuterX(p1, p2)) {
            return true;
        }
        if (checkOuterY(p2, p1)) {
            return true;
        }

        Point midP1 = new Point();
        Point midP2 = new Point();


        for (int i = 0; i < 2; i++) {
            for (int row = 0; row < getSizeX(); row++) {
                if (i == 0) {
                    midP1 = new Point(row, p1.getY());
                    midP2 = new Point(row, p2.getY());
                    if (checkIterate(p1, p2, midP1, midP2))
                        return true;
                } else if (i == 1) {
                    midP1 = new Point(p1.getX(), row);
                    midP2 = new Point(p2.getX(), row);
                    if (checkIterate(p1, p2, midP1, midP2))
                    return true;
                }
            }
        }
        return false;
    }


    private boolean checkIterate(Point p1, Point p2, Point midP1, Point midP2) {
        if (twoPointAxisIsClear(midP1, midP2, areSame(p1, midP1), areSame(p2, midP2))) {
            if (twoPointAxisIsClear(p1, midP1, true, areSame(p1, midP1))) {
                return twoPointAxisIsClear(p2, midP2, true, areSame(p2, midP2));
            }
        }
        return false;
    }


    public boolean connect(Point p1, Point p2) {

        if (checkConnect(p1, p2)) {
            history.push(p1);
            history.push(p2);

            grid[p1.getX()][p1.getY()].setState(TileState.REMOVED);
            grid[p2.getX()][p2.getY()].setState(TileState.REMOVED);

            if (history.size() == tileCount) gameState = GameState.WON;

            addScore();

            return true;
        }
        return false;
    }

    public boolean undo() {
        if (history.isEmpty()) return false;
        getTile(history.pop()).setState(TileState.GROUND);
        getTile(history.pop()).setState(TileState.GROUND);
        return true;
    }

    public void reset() {
        while (undo()) ;
    }

    public int getPlayingTime() {
        return ((int) (System.currentTimeMillis() - startMillis)) / 1000;
    }

    public int getScore() {
        return score;
    }

    public void setStartMillis() {
        startMillis = System.currentTimeMillis();
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean checkConnects(int x1, int y1, boolean hint) {
        Point p1;
        Point p2;
        boolean found = false;
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                p1 = new Point(x, y);
                p2 = new Point(x1, y1);
                if (checkConnect(p1, p2) && (p1 != p2)) {
                    if (hint == true) {
                        this.getTile(x, y).setState(TileState.HINTED);
                    }
                    found = true;
                }
            }
        }
        return found;
    }

    public void unHint() {
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                if (this.getTile(x, y).getState() == TileState.HINTED)
                    this.getTile(x, y).setState(TileState.GROUND);
            }
        }
    }


    public void addScore() {
        int popDiff = (((int) System.currentTimeMillis()) / 1000) - popTime;
        popTime = ((int) System.currentTimeMillis()) / 1000;

        if (!conStarted) {
            conStarted = true;
            popDiff = 6;
        }

        popTime = ((int) System.currentTimeMillis()) / 1000;

        if (popDiff < 7) {
            bonusMulti = bonusMulti + 1;
        } else {
            bonusMulti = 1;
        }
        scoreHistory.push(bonusMulti * 5);
        score = score + (bonusMulti * 5);
    }

    public int getLastScore() {
        return scoreHistory.pop();
    }

}
