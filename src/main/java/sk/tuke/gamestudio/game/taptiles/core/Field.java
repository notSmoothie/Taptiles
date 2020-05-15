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
        bonusMulti = 1;
        grid = new Tile[sizeX][sizeY];
        random = new Random();
        generateField();
        history = new Stack<Point>();

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

        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                if (grid[i][j] == null)
                    grid[i][j] = new Tile(currSymbol);

    }

    private boolean pointIsOutside(Point p) {
        if(p.getX() < 0 || p.getX() > sizeX || p.getY() < 0 || p.getY() > sizeY){
            return true;
        }
       return false;
    }

    private boolean clearX(int x1, int x2, int y) {
        for (int x = Math.min(x1, x2) + 1; x < Math.max(x1,x2); x++) {
            if (grid[x][y].getState() != TileState.REMOVED) {
                return false;
            }
        }
        return true;
    }

    private boolean clearY(int y1, int y2, int x) {
        for (int y = Math.min(y1, y2) + 1; y < Math.max(y1,y2); y++) {
            if (grid[x][y].getState() != TileState.REMOVED) {
                return false;
            }
        }
        return true;
    }


    /**
     * BLOCK OF LOGICK INCOMING
     */

    public boolean checkConnect(Point p1, Point p2) {

        if (p1.getX() == p2.getX() && p1.getY() == p2.getY()) return false;

        if (pointIsOutside(p1) || pointIsOutside(p2)){
            throw new ArrayIndexOutOfBoundsException();}

        Tile t1 = getTile(p1);
        Tile t2 = getTile(p2);

        if (t1.getSymbol() != t2.getSymbol() || t1.getState() == TileState.REMOVED) return false;

        int minPos = Math.min(p1.getY(), p2.getY());
        int maxPos = Math.max(p1.getY(), p2.getY());
        int y;

        if (p1.getX() != p2.getX()) {
            for (y = maxPos; y >= 0; y--) {
                if (grid[p1.getX()][y].getSymbol() != t1.getSymbol() && grid[p1.getX()][y].getState() != TileState.REMOVED)
                {
                if (grid[p2.getX()][y].getSymbol() != t2.getSymbol() && grid[p2.getX()][y].getState() != TileState.REMOVED)
                {break;}}
                if (clearX(p1.getX(), p2.getX(), y)) {
                    if (y <= minPos) return true;
                    if (clearY(minPos, maxPos, p1.getY() < p2.getY() ? p1.getX() : p2.getX())) return true;
                }
                if (y <= 0) {return true; }
            }
            if (clearY(maxPos, minPos, p1.getY() < p2.getY() ? p1.getX() : p2.getX())) {
                for (y = maxPos + 1; y < sizeY; y++) {
                    if (grid[p1.getX()][y].getState() != TileState.REMOVED) {break;}
                    if (grid[p2.getX()][y].getState() != TileState.REMOVED) {break;}
                    if (clearX(p1.getX(), p2.getX(), y)) return true;

                }
                if (y == sizeY) return true;
            }
        }

        int x;
        minPos = Math.min(p1.getX(), p2.getX());
        maxPos = Math.max(p1.getX(), p2.getX());

        for (x = maxPos; x >= 0; x--) {
            if (grid[x][p1.getY()].getSymbol() != t1.getSymbol() && grid[x][p1.getY()].getState() != TileState.REMOVED)
            {
            if (grid[x][p2.getY()].getSymbol() != t2.getSymbol() && grid[x][p2.getY()].getState() != TileState.REMOVED)
            { break;}}
            if (clearY(p1.getY(), p2.getY(), x)) {
                if (x <= minPos) return true;
                if (clearX(minPos, maxPos, p1.getX() < p2.getX() ? p1.getY() : p2.getY())) return true;
            }
        }

        if (clearX(maxPos, minPos, p1.getX() < p2.getX() ? p1.getY() : p2.getY())) {
            for (x = maxPos + 1; x < sizeX; x++) {
                if (grid[x][p1.getY()].getState() != TileState.REMOVED) {break;}
                if (grid[x][p2.getY()].getState() != TileState.REMOVED) {break;}
                if (clearY(p1.getY(), p2.getY(), x)) return true;
                if (x <= 0)  { return true; }
            }
            if (x == sizeX) return true;
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

    public void setStartMillis(){
        startMillis =  System.currentTimeMillis();
    }

    public void setScore(int score){
        this.score = score;
    }


    public void addScore(){
        int popDiff = (((int) System.currentTimeMillis()) / 1000) - popTime;
        popTime = ((int) System.currentTimeMillis()) / 1000;

        if (!conStarted){
            conStarted = true;
            popDiff = 6;
        }

        System.out.println(popDiff);

        popTime = ((int) System.currentTimeMillis()) / 1000;

        if (popDiff < 7){
            bonusMulti = bonusMulti + 1;
        } else {
            bonusMulti = 1;
        }

        score = score + (bonusMulti*5);
        scoreHistory.push(score);
    }

    public int getLastScore(){
        return scoreHistory.pop();
    }

}
