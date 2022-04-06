package cz.cvut.fel.pjv.shubevik.players;

import cz.cvut.fel.pjv.shubevik.board.Board;
import cz.cvut.fel.pjv.shubevik.game.Color;
import cz.cvut.fel.pjv.shubevik.moves.Move;

public abstract class Player {

    private Color pieceColor;

    public  abstract Move chooseMove(Board board);
}
