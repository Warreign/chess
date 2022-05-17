package cz.cvut.fel.pjv.shubevik.board;

import cz.cvut.fel.pjv.shubevik.game.Color;
import cz.cvut.fel.pjv.shubevik.pieces.Piece;

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
    public Color getPieceColor() {
        return isOccupied() ? piece.getColor() : null;
    }
}
