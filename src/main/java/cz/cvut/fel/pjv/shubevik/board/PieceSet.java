package cz.cvut.fel.pjv.shubevik.board;

import java.util.ArrayList;
import java.util.List;

import cz.cvut.fel.pjv.shubevik.game.Color;
import cz.cvut.fel.pjv.shubevik.pieces.Piece;

public class PieceSet {
    private List<Piece> pieces;
    private Color color;

    public PieceSet(Color color) {
        this.color = color;
        this.pieces = new ArrayList<>();
    }

    public void add(Piece piece) {
        pieces.add(piece);
    }

    public List<Piece> getSet() {
        return this.pieces;
    }
}
