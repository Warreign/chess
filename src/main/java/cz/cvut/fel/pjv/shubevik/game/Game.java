package cz.cvut.fel.pjv.shubevik.game;

import cz.cvut.fel.pjv.shubevik.PGN.GameState;
import cz.cvut.fel.pjv.shubevik.board.Board;
import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.moves.Move;
import cz.cvut.fel.pjv.shubevik.moves.MoveType;
import cz.cvut.fel.pjv.shubevik.moves.SpecialMove;
import cz.cvut.fel.pjv.shubevik.pieces.*;
import cz.cvut.fel.pjv.shubevik.players.Player;
import cz.cvut.fel.pjv.shubevik.players.PlayerType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.*;

public class Game {

    static Logger logger = Logger.getLogger(Game.class.getName());

    private Board board;
    private Map<PColor, Player> players;
    private ObjectProperty<Player> currentPlayer;
    private Result result;
    private boolean started;
    private ObservableList<Piece> takenPieces;
    private ObservableList<GameState> history;
    private Move lastMove;

    private int fullMoves;
    private int halfMoves;
    private BooleanProperty gameOver;
    private ObjectProperty<SpecialMove> specialMove;

    private int counter;

    public Game(Player p1, Player p2, boolean clock) {
        takenPieces = FXCollections.observableList(new ArrayList<>());
        history = FXCollections.observableList(new ArrayList<>());

        players = new HashMap<>();
        players.put(p1.getColor(), p1);
        players.put(p2.getColor(), p2);

        result = Result.IN_PROCESS;
        board = new Board(false);
        started = false;
        lastMove = null;
        currentPlayer = new SimpleObjectProperty<>();

        fullMoves = 0;
        halfMoves = 0;

        gameOver = new SimpleBooleanProperty(false);
        specialMove = new SimpleObjectProperty<>();
    }
    // Game variation for move testing
    public Game(Board board) {
        this.board = board;
    }

    public static PColor opposite(PColor color) {
        return color == PColor.WHITE ? PColor.BLACK : PColor.WHITE;
    }

    public void begin() {
        if (!started) {
            history.add(new GameState(board.getCopy(), new ArrayList<>(), null, false, false));
            initialEvaluate();
            currentPlayer.set(players.get(PColor.WHITE));
            started = true;
            getCurrentPlayer().startTimer();
            if (getCurrentPlayer().getType() == PlayerType.RANDOM) randomMoveCurrent();
        }
    }

    public boolean takeMove(Move move) {
        if (isMoveValid(move)) {
            makeMove(move);
            lastMove = move;
//            logger.log(Level.INFO, "Valid move");
//            logger.log(Level.INFO, getCurrentColor() + board.toString());
            evaluateAndSwitch();
            return true;
        }
        return false;
    }

    private void makeMove(Move move) {
        movePieces(move, false);
        if (move.isEndOccupied()) {
            takenPieces.add(move.getCapture());
        }
        // Castling
        if (move.getPiece() instanceof King &&
                move.getPiece().yDiff(move) == 2) {
            doCastling(move);
            if (move.getPiece().isRight(move)) {
                move.setType(MoveType.CASTLING_KINGSIDE);
            } else { // Left
                move.setType(MoveType.CASTLING_QUEENSIDE);
            }
            specialMove.set(SpecialMove.CASTLING);
        }
        // En Passant
        else if (move.getPiece() instanceof Pawn &&
                ((Pawn) move.getPiece()).isEnPassant(this,move)) {
            doEnPassant(move);
            move.setType(MoveType.NORMAL);
            specialMove.set(SpecialMove.EN_PASSANT);
        }
        // Promote
        else if (move.getPiece() instanceof Pawn &&
                ((move.getColor() == PColor.WHITE && move.getEnd().x == 7) ||
                        (move.getColor() == PColor.BLACK && move.getEnd().x == 0))) {
            specialMove.set(SpecialMove.PROMOTION);
        }
        else {
            move.setType(MoveType.NORMAL);
            specialMove.set(SpecialMove.NONE);
        }
    }
    public List<Tile> findPotentialMoves(PColor color) {
        List<Tile> potentialMoves = new ArrayList<>();
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                Tile t =  board.getTile(x, y);
                if (!t.isOccupied() || t.getPieceColor() == opposite(color)) {
                    potentialMoves.add(t);
                }
            }
        }
        return potentialMoves;
    }
    public List<Move> findMovesPiece(Tile tile) {
        List<Move> possibleMoves = new ArrayList<>();
        if (!tile.isOccupied()) return possibleMoves;
        Move m = new Move(tile, null);
        for (Tile t : findPotentialMoves(m.getColor())) {
            m.setEnd(t);
            m.update();
            if (m.checkMove() &&
                    tile.getPiece().isValid(this, m) &&
                    pathClear(m) &&
                    !checkAfterMove(m)) {
                possibleMoves.add(m.getCopy());
            }
        }
        return possibleMoves;
    }

    public boolean canMoveOpponent(PColor color) {
        for (Tile t : findPiecesColor(opposite(color))) {
            if (!findMovesPiece(t).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean pathClear(Move move) {
        if (move.getPiece() instanceof Pawn || move.getPiece() instanceof Knight) {
            return true;
        }

        int xShift = move.getEnd().x - move.getStart().x;
        if (xShift != 0) { xShift = xShift/Math.abs(xShift); }
        int yShift = move.getEnd().y - move.getStart().y;
        if (yShift != 0) { yShift = yShift/Math.abs(yShift); }

        Tile t = board.getTile(move.getStart().x+xShift, move.getStart().y+yShift);

        while(t != move.getEnd()) {
            if (t.isOccupied()) {
                return false;
            }
            t = board.getTile(t.x + xShift, t.y + yShift);
        }
        return true;
    }

    public boolean tileUnderAttack(Tile tile) {
        Move m = new Move(null, tile);
        PColor attacker = opposite(tile.getPieceColor());
        for (Tile s : findPiecesColor(attacker)) {
            m.setStart(s);
            m.update();
            if (m.getPiece().isValid(this, m) && pathClear(m)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkAfterMove(Move move) {
        Piece taken = move.getEnd().getPiece();
        move.getStart().setPiece(null);
        move.getEnd().setPiece(move.getPiece());
        Tile king = findKings().get(move.getColor());
        boolean check = tileUnderAttack(king);
        move.getStart().setPiece(move.getPiece());
        move.getEnd().setPiece(taken);
        return check;
    }

    public boolean checkOpponent(PColor color) {
        PColor opponent = opposite(color);
        return tileUnderAttack(findKings().get(opponent));
    }

    public Map<PColor, Tile> findKings() {
        Map<PColor, Tile> kings = new HashMap<>();
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                Tile t = board.getTile(x, y);
                if (t.isOccupied() && t.getPiece() instanceof King) {
                    kings.put(t.getPieceColor(), t);
                }
            }
        }
        return kings;
    }

    public List<Tile> findPiecesColor(PColor color) {
        List<Tile> pieces = new ArrayList<>();
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                Tile t = board.getTile(x, y);
                if (t.isOccupied() && t.getPieceColor() == color) pieces.add(t);
            }
        }
        return pieces;
    }

    private boolean isMoveValid(Move move) {
        return move.checkMove() &&
                currentPlayer.get() == players.get(move.getColor()) &&
                move.getPiece().isValid(this, move) &&
                pathClear(move) &&
                !checkAfterMove(move);
    }

    // Do special moves
    private void doCastling(Move move) {
        Move m;
        if (move.getColor() == PColor.WHITE) {
            m = move.getPiece().isRight(move) ?
                    new Move(getTile(0,7), getTile(0,5)) :
                    new Move(getTile(0,0), getTile(0,3));
            movePieces(m, false);
        }
        else { // BLACK
            m = move.getPiece().isRight(move) ?
                    new Move(getTile(7,7), getTile(7,5)) :
                    new Move(getTile(7,0), getTile(7,3));
            movePieces(m, false);
        }
        halfMoves++;
    }

    private void doEnPassant(Move move) {
        if (move.getColor() == PColor.WHITE) {
            Tile enemyTile = getTile(move.getEnd().x-1, move.getEnd().y);
            takenPieces.add(enemyTile.getPiece());
            enemyTile.setPiece(null);
        }
    }

    public void setPromPiece(Piece piece) {
        getLastMove().setType(MoveType.values()[Arrays.asList(Queen.class, Rook.class, Knight.class, Bishop.class).indexOf(piece.getClass())]);
        getLastMove().getEnd().setPiece(piece);
    }

    // Game evaluation
    private boolean isCheckmateOpponent(PColor color) {;
        boolean opponentCheckmate = !canMoveOpponent(color) && checkOpponent(color);
        if (opponentCheckmate) result = color == PColor.WHITE ? Result.WHITE_WIN : Result.BLACK_WIN;
        return opponentCheckmate;
    }

    private boolean isStalemateOpponent(PColor color) {
        boolean stalemate = !canMoveOpponent(color) && !checkOpponent(color);
        if (stalemate) result = Result.DRAW;
        return stalemate;
    }

    private boolean isInsufficientMaterial() {
        List<Tile> pieceTiles = findPiecesColor(PColor.WHITE);
        List<Piece> whitePieces = new ArrayList<>();
        for (Tile t : pieceTiles) whitePieces.add(t.getPiece());
        boolean insufficientWhite = whitePieces.size() == 1 ||
                (whitePieces.size() == 2 &&
                        (whitePieces.get(0) instanceof Bishop || whitePieces.get(0) instanceof Knight ||
                                whitePieces.get(1) instanceof Bishop || whitePieces.get(1) instanceof Knight));

        pieceTiles = findPiecesColor(PColor.BLACK);
        List<Piece> blackPieces = new ArrayList<>();
        for (Tile t : pieceTiles) blackPieces.add(t.getPiece());
        boolean insufficientBlack = blackPieces.size() == 1 ||
                (blackPieces.size() == 2 &&
                        (blackPieces.get(0) instanceof Bishop || blackPieces.get(0) instanceof Knight ||
                                blackPieces.get(1) instanceof Bishop || blackPieces.get(1) instanceof Knight));

        if (insufficientWhite && insufficientBlack) result = Result.DRAW;
        return insufficientWhite && insufficientBlack;
    }

    private boolean isFiftyMoves() {
        boolean fiftyMoves = fullMoves >= 50;
        if (fiftyMoves) result = Result.DRAW;
        return fiftyMoves;
    }

    private boolean isThreeFoldRep() {
        return false;
    }

    private void evaluateForColor(PColor color) {
        boolean ended = isCheckmateOpponent(color) || isStalemateOpponent(color) || isInsufficientMaterial() || isFiftyMoves() || isThreeFoldRep();
        gameOver.set(ended);
    }

    public void initialEvaluate() {
        evaluateForColor(PColor.WHITE);
        if (!gameOver.get()) {
            evaluateForColor(PColor.BLACK);
        }
    }

    public void evaluateAndSwitch() {
        evaluateForColor(getCurrentColor());
        if (getSpecialMove().get() != SpecialMove.PROMOTION && getResult() == Result.IN_PROCESS) {
            switchPlayers();
        }
    }

    public boolean impossiblePosition() {
        return checkOpponent(PColor.WHITE);
    }

    public void movePieces(Move move, boolean swap) {
        move.getPiece().setWasMoved(true);
        if (swap) {
            move.getStart().setPiece(move.getCapture());
            move.getEnd().setPiece(move.getPiece());
        }
        else {
            move.getStart().setPiece(null);
            move.getEnd().setPiece(move.getPiece());
        }
    }

    private void moveIncrement(Move move) {
        if (move.getPiece() instanceof Pawn || move.isEndOccupied()) {
            halfMoves = 0;
            fullMoves = 0;
        }
        else {
            halfMoves++;
        }
        if (move.getColor() == PColor.BLACK) fullMoves++;
    }

    private void switchPlayers() {
        appendState();
        if (!gameOver.get()) {
            currentPlayer.get().stopTimer();
            currentPlayer.set(getCurrentColor() == PColor.WHITE ? players.get(PColor.BLACK) : players.get(PColor.WHITE));
            if (!gameOver.get()) currentPlayer.get().startTimer();
        }
    }

    public void randomMoveCurrent() {
        // Find all pieces of current player
        List<Tile> pieces = findPiecesColor(getCurrentColor());
        // Filter out pieces with no moves
        pieces.removeIf(t -> findMovesPiece(t).isEmpty());
        // Choose random piece;
        List<Move> moves = findMovesPiece(pieces.get(ThreadLocalRandom.current().nextInt(pieces.size())));
        // Choose random move
        takeMove(moves.get(ThreadLocalRandom.current().nextInt(moves.size())));
    }

    public void stopTimers() {
        players.get(PColor.WHITE).stopTimer();
        players.get(PColor.BLACK).stopTimer();
    }

    public void appendState() {
        System.out.println(counter++);
        history.add(new GameState(board.getCopy(),
                copyTakenPieces(),
                getLastMove() != null ? getLastMove().getCopy() : null,
                checkOpponent(getCurrentColor()),
                isCheckmateOpponent(getCurrentColor())));
    }

    private List<Piece> copyTakenPieces() {
        List<Piece> taken = new ArrayList<>();
        for (Piece p : takenPieces) taken.add(p.getCopy());
        return taken;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Tile getTile(int x, int y) {
        return board.getTile(x, y);
    }

    public Map<PColor, Player> getPlayers() {
        return players;
    }

    public ObservableList<GameState> getHistory() {
        return history;
    }

    public Move getLastMove() {
        return lastMove;
    }

    public ObjectProperty<SpecialMove> getSpecialMove() {
        return specialMove;
    }

    public BooleanProperty getGameOver() {
        return gameOver;
    }

    public Result getResult() {
        return result;
    }

    public ObservableList<Piece> getTaken() {
        return takenPieces;
    }

    public ObjectProperty<Player> getCurrent() {
        return currentPlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer.get();
    }

    public PColor getCurrentColor() {
        return currentPlayer.get().getColor();
    }

    public boolean hasStarted() {
        return started;
    }
}

