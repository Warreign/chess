package cz.cvut.fel.pjv.shubevik.pieces;

import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.moves.Move;

public abstract class Piece {

    private boolean moved;
    private final PColor color;
    private boolean captured;
    public Piece(PColor color) {
        this.color = color;
    }

    public PColor getColor() {
        return this.color;
    }

    public abstract boolean isValid(Game game, Move move);

    public boolean wasMoved() {
        return moved;
    }

    public void setWasMoved(boolean wasMoved) { moved = wasMoved; }

    public int xDiff(Move move) {
        return Math.abs(move.getEnd().x-move.getStart().x);
    }

    public int yDiff(Move move) {
        return Math.abs(move.getEnd().y-move.getStart().y);
    }

    public boolean isJump(Move move) {
        return (xDiff(move) == 2 && yDiff(move) == 1) || (xDiff(move) == 1 && yDiff(move) == 2);
    }

    public boolean isDiagonal(Move move) {
        return xDiff(move) == yDiff(move);
    }

    public boolean isStraight(Move move) {
        return xDiff(move) == 0 || yDiff(move) == 0;
    }

    public boolean isLeft(Move move) {
        return move.getEnd().y - move.getStart().y < 0;
    }

    public boolean isRight(Move move) {
        return move.getEnd().y - move.getStart().y > 0;
    }

    public boolean isUp(Move move) {
        return move.getEnd().x - move.getStart().x > 0;
    }

    public boolean isDown(Move move) {
        return move.getEnd().x - move.getStart().x < 0;
    }
}
