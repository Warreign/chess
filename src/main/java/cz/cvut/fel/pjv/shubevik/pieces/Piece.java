package cz.cvut.fel.pjv.shubevik.pieces;

import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.game.Color;
import cz.cvut.fel.pjv.shubevik.moves.Move;

public abstract class Piece {

    boolean wasMoved;
    final Color color;
    boolean captured;
    Tile position;

    public Piece(Color color, Tile tile) {
        this.color = color;
        this.position = tile;
    }

    public Move[] validMoves() {

    }
}
