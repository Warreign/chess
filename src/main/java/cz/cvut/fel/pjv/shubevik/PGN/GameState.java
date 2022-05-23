package cz.cvut.fel.pjv.shubevik.PGN;

import cz.cvut.fel.pjv.shubevik.board.Board;
import cz.cvut.fel.pjv.shubevik.game.Result;
import cz.cvut.fel.pjv.shubevik.moves.Move;
import cz.cvut.fel.pjv.shubevik.moves.MoveType;
import cz.cvut.fel.pjv.shubevik.pieces.Piece;

import java.util.List;

public class GameState {

    private Board board;
    private List<Piece> takenPieces;
    private Move lastMove;
    private MoveType type;
    private boolean check;
    private boolean checkmate;

    public GameState(Board board, List<Piece> taken, Move lastMove, boolean check, boolean checkmate) {
        this.board = board;
        this.lastMove = lastMove;
        this.type = lastMove == null ? null : lastMove.getType();
        this.takenPieces = taken;
        this.check = check;
        this.checkmate = checkmate;
    }

    public Board getBoard() {
        return  board;
    }

    public Move getMove() {
        return lastMove;
    }

    public MoveType getType() {
        return type;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckmate() {
        return checkmate;
    }
}
