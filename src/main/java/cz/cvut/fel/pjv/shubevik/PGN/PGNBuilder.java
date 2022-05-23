package cz.cvut.fel.pjv.shubevik.PGN;

import cz.cvut.fel.pjv.shubevik.game.Board;
import cz.cvut.fel.pjv.shubevik.game.Tile;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.game.moves.Move;
import cz.cvut.fel.pjv.shubevik.game.pieces.Pawn;

import java.time.LocalDate;
import java.util.List;

public class PGNBuilder {
    public static String boardToString(Board board) {
        return "";
    }

    public static Board stringToBoard(String str) {
        return new Board(true);
    }

    public static String gameToPGN(Game game) {
        StringBuilder s = new StringBuilder();
        String result = "";
        switch (game.getResult()) {
            case DRAW:
                result = "1/2-1/2";
                break;
            case WHITE_WIN:
                result = "1-0";
                break;
            case BLACK_WIN:
                result = "0-1";
                break;
            case IN_PROCESS:
                result = "*";
                break;
        }
        // Add tags
        s.append(String.format("[Date \"%s\"]\n", LocalDate.now()))
                .append(String.format("[White \"%s\"]\n", game.getPlayers().get(PColor.WHITE).getName()))
                .append(String.format("[Black \"%s\"]\n", game.getPlayers().get(PColor.BLACK).getName()))
                .append(String.format("[Result \"%s\"]\n", result));

        // Add moves
        int counter = 1;
        for (GameState state : game.getHistory()) {
            Move m = state.getMove();
            if (m == null) continue;
            if (m.getColor() == PColor.WHITE) s.append(String.format("%d. ",counter++));
            switch (state.getType()) {
                case CASTLING_KINGSIDE:
                    s.append("O-O");
                    break;
                case CASTLING_QUEENSIDE:
                    s.append("O-O-O");
                    break;
                case PROMOTION_QUEEN:
                    if (m.isCapture()) s.append(m.getStart().toString().toCharArray()[0]).append("x");
                    s.append(m.getEnd()).append("=Q");
                    break;
                case PROMOTION_BISHOP:
                    if (m.isCapture()) s.append(m.getStart().toString().toCharArray()[0]).append("x");
                    s.append(m.getEnd()).append("=B");
                    break;
                case PROMOTION_KNIGHT:
                    if (m.isCapture()) s.append(m.getStart().toString().toCharArray()[0]).append("x");
                    s.append(m.getEnd()).append("=N");
                    break;
                case PROMOTION_ROOK:
                    if (m.isCapture()) s.append(m.getStart().toString().toCharArray()[0]).append("x");
                    s.append(m.getEnd()).append("=R");
                    break;
                default:
                    s.append(m.getPiece());

                    boolean canMoveSame = false;
                    Game testGame = new Game(state.getBoard().getCopy());
                    testGame.getTile(m.getEnd().x, m.getEnd().y).setPiece(null);
                    List<Tile> pieces = testGame.findPiecesColor(m.getColor());
                    pieces.removeIf(t -> t.getPiece().getClass() != m.getPiece().getClass() || t.getPiece() instanceof Pawn);
                    if (pieces.size() != 0) {
                        for (Tile t : pieces) {
                            List<Move> moves = testGame.findMovesPiece(t);
                            for (Move tmp : moves)  {
                                if (tmp.getEnd().equals(m.getEnd())) {
                                    canMoveSame = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (canMoveSame || (m.getPiece() instanceof Pawn && m.isCapture()))
                        s.append(m.getStart().toString().toCharArray()[0]);

                    if (m.isCapture()) s.append("x");
                    s.append(m.getEnd());
            }
            if (state.getCheckmate()) s.append("#");
            else if (state.getCheck()) s.append("+");
            s.append(" ");
        }
        if (!result.equals("*")) s.append(result);
        return s.toString();
    }
}
