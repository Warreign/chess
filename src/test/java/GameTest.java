import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.sun.source.tree.WhileLoopTree;
import cz.cvut.fel.pjv.shubevik.game.moves.Move;
import cz.cvut.fel.pjv.shubevik.game.pieces.Queen;
import cz.cvut.fel.pjv.shubevik.game.players.Player;
import cz.cvut.fel.pjv.shubevik.game.players.PlayerType;
import org.hamcrest.MatcherAssert.*;

import cz.cvut.fel.pjv.shubevik.game.Board;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.game.Tile;
import cz.cvut.fel.pjv.shubevik.game.pieces.King;
import cz.cvut.fel.pjv.shubevik.game.pieces.Pawn;
import cz.cvut.fel.pjv.shubevik.game.pieces.Rook;
import org.junit.jupiter.api.*;


import java.util.List;
import java.util.Map;

class GameTest {

    Game game;
    Board board;

    @BeforeEach
    void init() {
        board = new Board(true);
        game = new Game(new Player("", PColor.WHITE, null, PlayerType.HUMAN), new Player("", PColor.BLACK, null, PlayerType.HUMAN), board, true);
    }

    @Test
    void testFindPiecesColorNoPieces() {
        Assertions.assertTrue(game.findPiecesColor(PColor.WHITE).isEmpty());
    }

    @Test
    void testFindPiecesColorOnlyOpposite() {
        board.setPiece(1, 1, new Rook(PColor.BLACK));
        board.setPiece(1, 2, new Rook(PColor.BLACK));
        Assertions.assertTrue(game.findPiecesColor(PColor.WHITE).isEmpty());
    }

    @Test
    void testFindPiecesColorPresent() {
        board.setPiece(3, 3, new Pawn(PColor.WHITE));
        board.setPiece(4, 4, new Pawn(PColor.WHITE));
        Assertions.assertEquals(game.findPiecesColor(PColor.WHITE).size(), 2);
    }

    @Test
    void testFindKingsEmpty() {
        Assertions.assertTrue(game.findKings().isEmpty());
    }

    @Test
    void testForKingsPresent() {
        board.setPiece(3, 3, new King(PColor.WHITE));
        board.setPiece(5, 5, new King(PColor.BLACK));
        Map<PColor, Tile> kings = game.findKings();

        Assertions.assertEquals(kings.get(PColor.WHITE), board.getTile(3, 3));
        Assertions.assertEquals(kings.get(PColor.BLACK), board.getTile(5, 5));
    }

    @Test
    void testNoCheck() {
        board.setPiece(0, 0, new King(PColor.WHITE));
        board.setPiece(7, 0, new King(PColor.BLACK));
        Assertions.assertFalse(game.checkOpponent(PColor.WHITE));
    }

    @Test
    void testCheck() {
        board.setPiece(0, 0, new King(PColor.WHITE));
        board.setPiece(7, 0, new King(PColor.BLACK));
        board.setPiece(7, 7, new Queen(PColor.WHITE));
        Assertions.assertTrue(game.checkOpponent(PColor.WHITE));
    }

    @Test
    void testCheckAfterMove() {
        board.setPiece(0, 0, new King(PColor.WHITE));
        board.setPiece(7, 7, new King(PColor.BLACK));
        board.setPiece(7, 0, new Queen(PColor.BLACK));
        board.setPiece(1, 0, new Rook(PColor.WHITE));
        Move m = new Move(board.getTile(1, 0), board.getTile(1, 1));
        Assertions.assertTrue(game.checkAfterMove(m));
    }

    @Test
    void testCastlingLeft() {
        board.setPiece(0, 4, new King(PColor.WHITE));
        board.setPiece(7, 4, new King(PColor.BLACK));
        board.setPiece(0, 0, new Rook(PColor.WHITE));
        Move m = new Move(board.getTile(0, 4), board.getTile(0, 2));
        Assertions.assertTrue(game.takeMove(m));
        Assertions.assertTrue(board.getPiece(0, 2) instanceof King);
        Assertions.assertTrue(board.getPiece(0, 3) instanceof Rook);
    }

    @Test
    void testCastlingRight() {
        board.setPiece(0, 4, new King(PColor.WHITE));
        board.setPiece(7, 4, new King(PColor.BLACK));
        board.setPiece(0, 7, new Rook(PColor.WHITE));
        Move m = new Move(board.getTile(0, 4), board.getTile(0, 6));
        Assertions.assertTrue(game.takeMove(m));
        Assertions.assertTrue(board.getPiece(0, 6) instanceof King);
        Assertions.assertTrue(board.getPiece(0, 5) instanceof Rook);
    }
}
