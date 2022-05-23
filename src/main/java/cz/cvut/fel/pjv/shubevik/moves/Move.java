package cz.cvut.fel.pjv.shubevik.moves;

import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.pieces.Piece;

public class Move {

    private Tile start, end;
    private Piece piece;
    private Piece capture;

    public Move(Tile s, Tile e) {
        this.start = s;
        this.end = e;
        this.piece = s != null ? s.getPiece() : null;
        this.capture = e != null ? e.getPiece() : null;
    }

    public boolean checkMove() {
        return (start != end && piece != null && start != null && end != null);
    }

    public void update() {
        piece = start != null ? start.getPiece() : null;
        capture =  end != null ? end.getPiece() : null;
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

    public boolean isEndOccupied() {
        return getCapture() != null;
    }

    public Move copyMove() {
        return new Move(start, end);
    }

    public Piece getPiece() {
        return piece;
    }

    public Piece getCapture() {
        return capture;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s %s %s", start, end, piece, getColor());
    }
}
