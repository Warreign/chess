package cz.cvut.fel.pjv.shubevik.moves;

import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.pieces.Piece;

public class Move {

    final Tile start;
    final Tile end;
    final Piece piece;
    final Piece toCapture;

    public Move(Tile s, Tile e) {
        this.start = s;
        this.end = e;
        this.piece = s.getPiece();
        this.toCapture = e.getPiece();
    }
}
