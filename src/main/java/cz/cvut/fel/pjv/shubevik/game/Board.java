package cz.cvut.fel.pjv.shubevik.game;

import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.MARKERS_CHAR;
import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.MARKERS_NUM;

import cz.cvut.fel.pjv.shubevik.GUI.GuiController;
import cz.cvut.fel.pjv.shubevik.game.moves.Move;
import cz.cvut.fel.pjv.shubevik.game.pieces.*;

public class Board {

    private Tile[][] grid;
    private boolean initialized;
    private Move lastMove;

    public static Piece genPiece(int x, int y) {
        if (y == 4 && x == 0) return new King(PColor.WHITE);
        if (y == 3 && x == 0) return new Queen(PColor.WHITE);
        if ((y == 2 || y == 5) && x == 0) return new Bishop(PColor.WHITE);
        if ((y == 1 || y == 6) && x == 0) return new Knight(PColor.WHITE);
        if ((y == 0 || y == 7) && x == 0) return new Rook(PColor.WHITE);
        if (x == 1) return new Pawn(PColor.WHITE);
//
        if (y == 4 && x == 7) return new King(PColor.BLACK);
        if (y == 3 && x == 7) return new Queen(PColor.BLACK);
        if ((y == 2 || y == 5) && x == 7) return new Bishop(PColor.BLACK);
        if ((y == 1 || y == 6) && x == 7) return new Knight(PColor.BLACK);
        if ((y == 0 || y == 7) && x == 7) return new Rook(PColor.BLACK);
        if (x == 6) return new Pawn(PColor.BLACK);
        if (x == 1 && y == 0) return new Pawn(PColor.BLACK);

//        if ( (x == 3 || x == 5) && y == 3) return new Knight(PColor.WHITE);



        return null;
    }

    public Board(boolean clear) {
        grid = new Tile[8][8];
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                if (!clear) grid[x][y] = new Tile(x, y, genPiece(x, y));
                else grid[x][y] = new Tile(x, y, null);
            }
        }
    }

    public void clear() {
        for (Tile[] r : grid) {
            for (Tile t : r) {
                if (t.isOccupied()) {
                    t.setPiece(null);
                }
            }
        }
    }
    public Tile getTile(int x, int y) {
        return grid[x][y];
    }

    public Tile getTile(String notation) {
        int x = MARKERS_NUM.indexOf(String.valueOf(notation.toCharArray()[1]));
        int y = MARKERS_CHAR.indexOf(String.valueOf(notation.toCharArray()[0]));
        return getTile(x, y);
    }

    public Piece getPiece(int x, int y) { return grid[x][y].getPiece(); }

    public void setPiece(int x, int y, Piece piece) { grid[x][y].setPiece(piece); }

    public void setPiece(String notation, Piece piece) {
        getTile(notation).setPiece(piece);
    }

    public Tile[][] getGrid() {
        return grid;
    }

    public Board getCopy() {
        Board b = new Board(true);
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                if (getTile(x, y).isOccupied())
                    b.setPiece(x, y, getPiece(x, y).getCopy());
            }
        }
        return b;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("\n");
        for (int x = 7; x >= 0; --x) {
            s.append(GuiController.MARKERS_NUM.get(x)).append(" ");
            for (int y = 0; y < 8; ++y) {
                if (!getTile(x,y).isOccupied()) s.append("*  ");
                else s.append(getTile(x, y).getPiece().getColor().toString().toLowerCase().toCharArray()[0]).append(getTile(x, y).getPiece().toString()).append(" ");
            }
            s.append("\n");
        }
        s.append("  ");
        for (int i = 0; i < 8; ++i) {
            s.append(GuiController.MARKERS_CHAR.get(i)).append("  ");
        }
        return s.append("\n").toString();
    }
}
