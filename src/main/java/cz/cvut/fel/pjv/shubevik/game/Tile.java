package cz.cvut.fel.pjv.shubevik.game;

import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.MARKERS_CHAR;
import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.MARKERS_NUM;

import cz.cvut.fel.pjv.shubevik.game.pieces.Piece;

public class Tile {
    public final int x;
    public final int y;
    private Piece piece;

    public Tile(int x, int y, Piece piece) {
        this.x = x;
        this.y = y;
        this.piece = piece;
    }

    public Piece getPiece() {
        return this.piece;
    }
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public boolean isOccupied() {
        return piece != null;
    }
    public PColor getPieceColor() {
        return isOccupied() ? piece.getColor() : null;
    }

    @Override
    public String toString() {
        return String.format(MARKERS_CHAR.get(y) + MARKERS_NUM.get(x));
    }

    public boolean equals(Tile other) {
        return this.x == other.x &&
                this.y == other.y;
    }
}
