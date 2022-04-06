package cz.cvut.fel.pjv.shubevik.game;

import cz.cvut.fel.pjv.shubevik.board.Board;
import cz.cvut.fel.pjv.shubevik.moves.Move;

import java.util.List;

public class Game {

    private Board board;
    private List<Move> moves;
    private Color currentTurn;
    private Player player1;
    private Player player2;
    private Result result;

    public void startGame() {
    }

    public void appendMove(Move move) {
        moves.add(move);
    }

    public void switchTurns() {

    }

    public boolean isInCheck(Color color) {

    }

    public boolean isInCheckmate(Color color) {

    }

    public void makeMove(Move move) {

    }
}
