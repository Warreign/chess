package cz.cvut.fel.pjv.shubevik.pieces;

import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.game.Color;
import cz.cvut.fel.pjv.shubevik.moves.Move;

public class Rook extends Piece {

    public Rook(Color color, Tile tile) { super(color, tile); }

    @Override
    public Move[] validMoves() {
        return super.validMoves();
    }
}
