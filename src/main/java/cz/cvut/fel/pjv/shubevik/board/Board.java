package cz.cvut.fel.pjv.shubevik.board;


import cz.cvut.fel.pjv.shubevik.game.Color;
import cz.cvut.fel.pjv.shubevik.pieces.*;

public class Board {

    private final int sizeX;
    private final int sizeY;
    private Tile[][] grid;
    private PieceSet set1;
    private PieceSet set2;

    public Board() {
        this.sizeX = 8;
        this.sizeY = 8;
    }

    public Tile getTile(int x, int y) {
        return this.grid[x][y];
    }

    public PieceSet initSet(Color color) {

    }
}
