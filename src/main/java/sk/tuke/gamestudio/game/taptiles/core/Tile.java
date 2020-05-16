package sk.tuke.gamestudio.game.taptiles.core;

public class Tile {
    private char symbol;

    public Tile(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    private TileState state = TileState.GROUND;

    public TileState getState() {
        return state;
    }

    public void setState(TileState state) {
        this.state = state;
    }
}

