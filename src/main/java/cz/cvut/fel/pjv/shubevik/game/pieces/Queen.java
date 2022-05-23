package cz.cvut.fel.pjv.shubevik.game.pieces;

import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.game.moves.Move;

public class Queen extends Piece {

    public Queen(PColor color) { super(color); }

    public boolean isValid(Game game, Move move) {
        return (!move.getEnd().isOccupied() || move.getEnd().getPieceColor() != getColor()) &&
                (isDiagonal(move) || isStraight(move));
    }

    @Override
    public String toString() {
        return "Q";
    }
}
