package sk.tuke.gamestudio.game.taptiles.core;

import java.util.Random;
import java.util.Stack;

public class Field {
    private final Tile[][] grid;
    private final Random random;
    private final int sizeX, sizeY;
    private final int tileCount;
    private final Stack<Point> history;
    private GameState gameState;
    private long startMillis;


    public Field(int sizeX, int sizeY) {
        if (sizeX < 1 || sizeY < 1 || ((sizeX * sizeY) % 2 == 1))
            throw new ArrayIndexOutOfBoundsException();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        tileCount = sizeX * sizeY;
        gameState = GameState.PLAYING;

        grid = new Tile[sizeX][sizeY];
        random = new Random();
        GenerateField();
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

    private void GenerateField() {
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

        startMillis = System.currentTimeMillis();

    }

    private boolean PointIsOutside(Point p) {
        return p == null || p.getX() < 0 || p.getX() >= sizeX || p.getY() < 0 || p.getY() >= sizeY;
    }

    private boolean ClearX(int x1, int x2, int y) {
        for (int x = Math.min(x1, x2) + 1; x < x1 && x < x2; x++) {
            if (grid[x][y].getState() != TileState.REMOVED) return false;
        }
        return true;
    }

    private boolean ClearY(int y1, int y2, int x) {
        for (int y = Math.min(y1, y2) + 1; y < y1 && x < y2; y++) {
            if (grid[x][y].getState() != TileState.REMOVED) return false;
        }
        return true;
    }


    /**
     * BLOCK OF LOGICK INCOMING
     */

    public boolean CheckConnect(Point p1, Point p2) {

        if (p1 == p2) return false;

        if (PointIsOutside(p1) || PointIsOutside(p2))
            throw new ArrayIndexOutOfBoundsException();

        Tile t1 = getTile(p1);
        Tile t2 = getTile(p2);

        if (t1.getSymbol() != t2.getSymbol() || t1.getState() == TileState.REMOVED) return false;

        int minPos = Math.min(p1.getY(), p2.getY());
        int maxPos = Math.max(p1.getY(), p2.getY());

        int y;
        if (p1.getX() != p2.getX()) {
            for (y = maxPos; y >= 0; y--) {
                if (grid[p1.getX()][y].getSymbol() != t1.getSymbol() && grid[p1.getX()][y].getState() != TileState.REMOVED)
                    break;
                if (grid[p2.getX()][y].getSymbol() != t2.getSymbol() && grid[p2.getX()][y].getState() != TileState.REMOVED)
                    break;
                if (ClearX(p1.getX(), p2.getX(), y)) {
                    if (y <= minPos) return true;
                    if (ClearY(minPos, y, p1.getY() < p2.getY() ? p1.getX() : p2.getX())) return true;
                }
            }
            if (y == -1) return true;
            if (ClearY(maxPos, minPos, p1.getY() < p2.getY() ? p1.getX() : p2.getX())) {
                for (y = maxPos + 1; y < sizeY; y++) {
                    if (grid[p1.getX()][y].getState() != TileState.REMOVED) break;
                    if (grid[p2.getX()][y].getState() != TileState.REMOVED) break;
                    if (ClearX(p1.getX(), p2.getX(), y)) return true;

                }
                if (y == sizeY) return true;
            }
        }

        int x;
        minPos = Math.min(p1.getX(), p2.getX());
        maxPos = Math.max(p1.getX(), p2.getX());

        for (x = maxPos; x >= 0; x--) {
            if (grid[x][p1.getY()].getSymbol() != t1.getSymbol() && grid[x][p1.getY()].getState() != TileState.REMOVED)
                break;
            if (grid[x][p2.getY()].getSymbol() != t2.getSymbol() && grid[x][p2.getY()].getState() != TileState.REMOVED)
                break;
            if (ClearY(p1.getY(), p2.getY(), x)) {
                if (x <= minPos) return true;
                if (ClearX(minPos, x, p1.getX() < p2.getX() ? p1.getY() : p2.getY())) return true;
            }
        }
        if (x == -1) return true;
        if (ClearX(maxPos, minPos, p1.getX() < p2.getX() ? p1.getY() : p2.getY())) {
            for (x = maxPos + 1; x < sizeX; x++) {
                if (grid[x][p1.getY()].getState() != TileState.REMOVED) break;
                if (grid[x][p2.getY()].getState() != TileState.REMOVED) break;
                if (ClearY(p1.getY(), p2.getY(), x)) return true;

            }
            return x == sizeX;
        }


        return false;
    }

    public boolean Connect(Point p1, Point p2) {
        if (CheckConnect(p1, p2)) {
            history.push(p1);
            history.push(p2);
            grid[p1.getX()][p1.getY()].setState(TileState.REMOVED);
            grid[p2.getX()][p2.getY()].setState(TileState.REMOVED);
            if (history.size() == tileCount) gameState = GameState.WON;
            return true;
        }
        return false;
    }

    public boolean Undo() {
        if (history.isEmpty()) return false;
        getTile(history.pop()).setState(TileState.GROUND);
        getTile(history.pop()).setState(TileState.GROUND);
        return true;
    }

    public void Reset() {
        while (Undo()) ;
    }

    private int getPlayingTime() {
        return ((int) (System.currentTimeMillis() - startMillis)) / 1000;
    }

    public int getScore() {
        return tileCount - getPlayingTime();
    }




}
