package cz.cvut.fel.pjv.shubevik.pieces;

import cz.cvut.fel.pjv.shubevik.board.Board;
import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.game.Color;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.moves.Move;

public class Queen extends Piece {

    public Queen(Color color) { super(color); }

    public boolean isValid(Game game, Move move) {
        return (!move.getEnd().isOccupied() || move.getEnd().getPieceColor() != getColor()) &&
                (isDiagonal(move) || isStraight(move));
    }
}
