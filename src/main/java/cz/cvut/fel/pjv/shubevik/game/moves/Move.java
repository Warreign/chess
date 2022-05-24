package cz.cvut.fel.pjv.shubevik.game.moves;

import cz.cvut.fel.pjv.shubevik.game.Tile;
import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.game.pieces.Piece;

public class Move {

    private Tile start, end;
    private Piece piece;
    private Piece capture;
    private MoveType type;
    private Class<? extends Piece> promotingTo;

    public Move(Tile s, Tile e) {
        this.start = s;
        this.end = e;
        this.piece = s != null ? s.getPiece() : null;
        this.capture = e != null ? e.getPiece() : null;
    }

    public Move(Tile s, Tile e, Piece p, Piece c, MoveType t, Class<? extends Piece> pr) {
        start = s;
        end = e;
        piece = p;
        capture = c;
        type = t;
        promotingTo = pr;
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

    public Move getCopy() {
        return new Move(start, end, piece, capture, type, promotingTo);
    }

    public Piece getPiece() {
        return piece;
    }

    public Piece getCapture() {
        return capture;
    }

    public Class<? extends Piece> getPromotion() {
        return promotingTo;
    }

    public void setPromoting(Class<? extends Piece> c) {
        promotingTo = c;
    }

    public boolean isCapture() {
        return capture != null;
    }

    public MoveType getType() {
        return type;
    }

    public void setType(MoveType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", start, end);
    }

    public boolean equals(Move other) {
        return this.start.equals(other.start) && this.end.equals(other.end);
    }
}
