package cz.cvut.fel.pjv.shubevik.game.pieces;

import cz.cvut.fel.pjv.shubevik.game.Board;
import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.game.moves.Move;

public class Pawn extends Piece {

    public Pawn(PColor color) { super(color); }

    public boolean isValid(Game game, Move move) {
        return (!move.getEnd().isOccupied() || move.getEnd().getPieceColor() != getColor()) &&
                (isNormalMove(move) || isLongMove(game.getBoard(), move) || isCapture(move) || isEnPassant(game, move));
    }

    public boolean isNormalMove(Move move) {
        if (move.getColor() == PColor.WHITE) {
            return isUp(move) && !move.isEndOccupied() && xDiff(move) == 1 && yDiff(move) == 0;
        }
        else {
            return isDown(move) && !move.isEndOccupied() && xDiff(move) == 1 && yDiff(move) == 0;
        }
    }

    public boolean isLongMove(Board board, Move move) {
        if (move.getColor() == PColor.WHITE) {
            return isUp(move) && !move.isEndOccupied() && xDiff(move) == 2 && yDiff(move) == 0 && !wasMoved() &&
                    !board.getTile(move.getStart().x+1, move.getStart().y).isOccupied();
        }
        else {
            return isDown(move) && !move.isEndOccupied() && xDiff(move) == 2 && yDiff(move) == 0 && !wasMoved() &&
                    !board.getTile(move.getStart().x-1, move.getStart().y).isOccupied();
        }
    }

    public boolean isEnPassant(Game game, Move move) {
        Move lm = game.getLastMove();
        if (getColor() == PColor.WHITE) {
            return lm != null &&
                    move.getEnd().x == move.getStart().x + 1 &&
                    lm.getPiece() instanceof Pawn &&
                    lm.getEnd().x - lm.getStart().x == -2 &&
                    lm.getEnd().y == move.getEnd().y &&
                    lm.getEnd().x - move.getEnd().x == -1;
        }
        else {
            return lm != null &&
                    move.getEnd().x == move.getStart().x - 1 &&
                    lm.getPiece() instanceof Pawn &&
                    lm.getEnd().x - lm.getStart().x == 2 &&
                    lm.getEnd().y == move.getEnd().y &&
                    lm.getEnd().x - move.getEnd().x == 1;
        }
    }

    public boolean isCapture(Move move) {
        if (getColor() == PColor.WHITE) {
            return xDiff(move) == 1 && yDiff(move) == 1 &&
                    isUp(move) &&
                    move.getEnd().isOccupied();
        }
        else {
            return xDiff(move) == 1 && yDiff(move) == 1 &&
                    isDown(move) &&
                    move.getEnd().isOccupied();
        }
    }

    @Override
    public String toString() {
        return "";
    }
}
