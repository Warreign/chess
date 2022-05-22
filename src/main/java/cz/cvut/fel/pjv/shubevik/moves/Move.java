package cz.cvut.fel.pjv.shubevik.moves;

import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.pieces.Piece;

public class Move {

    private Tile start, end;
    private Piece piece;

    public Move(Tile s, Tile e) {
        this.start = s;
        this.end = e;
        this.piece = s != null ? s.getPiece() : null;
    }

    public boolean checkMove() {
        return (start != end && piece != null && start != null && end != null);
    }

    public Tile getStart() {
        return start;
    }

    public void setStart(Tile start) {
        this.start = start;
        piece = start.getPiece();
    }

    public Tile getEnd() {
        return end;
    }

    public void setEnd(Tile end) {
        this.end = end;
    }

    public PColor getColor() {
        return piece != null ? piece.getColor() : null;
    }

    public boolean endOccupied() {
        return end.isOccupied();
    }

    public Move copyMove() {
        return new Move(start, end);
    }

    public Piece getPiece() {
        return piece;
    }


}
