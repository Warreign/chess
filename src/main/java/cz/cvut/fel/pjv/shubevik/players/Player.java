package cz.cvut.fel.pjv.shubevik.players;

import cz.cvut.fel.pjv.shubevik.board.Board;
import cz.cvut.fel.pjv.shubevik.game.Color;
import cz.cvut.fel.pjv.shubevik.moves.Move;

public abstract class Player {

    private final Color pieceColor;
    private final String name;

    public Player(String name, Color color) {
        this.name = name;
        pieceColor = color;
    }

    public Color getColor() {
        return pieceColor;
    }

    public String getName() {
        return name;
    }
}
