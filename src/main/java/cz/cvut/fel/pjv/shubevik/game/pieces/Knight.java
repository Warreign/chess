package cz.cvut.fel.pjv.shubevik.game.pieces;

import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.game.moves.Move;

public class Knight extends Piece {

    public Knight(PColor color) { super(color); }

    public boolean isValid(Game game, Move move) {
        return (!move.getEnd().isOccupied() || move.getEnd().getPieceColor() != getColor()) &&
                isJump(move);
    }

    @Override
    public String toString() {
        return "N";
    }
}
