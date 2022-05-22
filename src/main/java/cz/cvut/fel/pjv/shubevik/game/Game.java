package cz.cvut.fel.pjv.shubevik.game;

import cz.cvut.fel.pjv.shubevik.board.Board;
import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.moves.Move;
import cz.cvut.fel.pjv.shubevik.moves.MoveType;
import cz.cvut.fel.pjv.shubevik.pieces.*;
import cz.cvut.fel.pjv.shubevik.players.Player;
import cz.cvut.fel.pjv.shubevik.players.RandomPlayer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.logging.*;

public class Game {

    static Logger logger = Logger.getLogger(Game.class.getName());

    private Board board;
    private Map<PColor, Player> players;
    private Player currentPlayer;
    private Result result;
    private final boolean clock;
    private ObservableList<Piece> takenPieces;
    private ObservableList<Move> previousMoves;

    private int fullMoves;
    private int halfMoves;
    private BooleanProperty gameOver;
    private ObjectProperty<MoveType> specialMove;

    public Game(Player p1, Player p2, boolean clock) {
        takenPieces = FXCollections.observableList(new ArrayList<>());
        previousMoves = FXCollections.observableList(new ArrayList<>());

        players = new HashMap<>();
        players.put(p1.getColor(), p1);
        players.put(p2.getColor(), p2);

        result = Result.IN_PROCESS;
        board = new Board();
        this.clock = clock;
        currentPlayer = players.get(PColor.WHITE);
        if (clock) currentPlayer.startTimer();

        fullMoves = 0;
        halfMoves = 0;

        gameOver = new SimpleBooleanProperty(false);
        specialMove = new SimpleObjectProperty<>();
    }

    public boolean takeMove(Move move) {
        if (isMoveValid(move)) {
            logger.log(Level.INFO, "Valid move");
            makeMove(move);
            board.setLastMove(move);
            isOver();
            switchPlayers();
            return true;
        }
        logger.log(Level.INFO, "Invalid move");
        return false;
    }

    public void makeMove(Move move) {
        if (move.endOccupied()) takenPieces.add(move.getEnd().getPiece());
        move.getStart().getPiece().setWasMoved(true);
        movePieces(move);
        // Castling
        previousMoves.add(move);
        if (move.getPiece() instanceof King && move.getPiece().yDiff(move) == 2) {
            doCastling(move);
            specialMove.set(MoveType.CASTLING);
        }
        // En Passant
        else if (move.getPiece() instanceof Pawn && ((Pawn) move.getPiece()).isEnPassant(board,move)) {
            doEnPassant(move);
            specialMove.set(MoveType.EN_PASSANT);
        }
        // Promote
        else if (move.getPiece() instanceof Pawn && ((move.getColor() == PColor.WHITE && move.getEnd().x == 7) || (move.getColor() == PColor.BLACK && move.getEnd().x == 0))) {
            specialMove.set(MoveType.PROMOTION);
        }
        else {
            specialMove.set(MoveType.NORMAL);
        }
    }
    public List<Tile> findPotentialMoves(PColor color) {
        List<Tile> potentialMoves = new ArrayList<>();
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                Tile t =  board.getTile(x, y);
                if (!t.isOccupied() || t.getPieceColor() == (color == PColor.WHITE ? PColor.BLACK : PColor.WHITE)) {
                    potentialMoves.add(t);
                }
            }
        }
        return potentialMoves;
    }
    public List<Move> findMovesPiece(Tile tile) {
        List<Move> possibleMoves = new ArrayList<>();
        Move m = new Move(tile, null);
        for (Tile t : findPotentialMoves(m.getColor())) {
            m.setEnd(t);
            if (
                    tile.isOccupied() &&
                    tile.getPiece().isValid(this, m) &&
                    pathClear(m) &&
                    !checkAfterMove(m)
            ) {
                possibleMoves.add(m.copyMove());
            }
        }
        return possibleMoves;
    }

    public boolean canMoveOpponent() {
        PColor opponent = currentPlayer.getColor() == PColor.WHITE ? PColor.BLACK : PColor.WHITE;
        for (Tile t : findPiecesColor(opponent)) {
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
        PColor attacker = tile.getPieceColor() == PColor.WHITE ? PColor.BLACK : PColor.WHITE;
        for (Tile s : findPiecesColor(attacker)) {
            m.setStart(s);
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

    public boolean checkOpponent() {
        return tileUnderAttack(findKings().get(currentPlayer.getColor() == PColor.WHITE ? PColor.BLACK : PColor.WHITE));
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

    public boolean isMoveValid(Move move) {
        return move.checkMove() &&
                currentPlayer == players.get(move.getColor()) &&
                move.getPiece().isValid(this, move) &&
                pathClear(move) &&
                !checkAfterMove(move);
    }

    // Do special moves
    public void doCastling(Move move) {
        System.out.println("Doing castling");
        Move m;
        if (move.getColor() == PColor.WHITE) {
            m = move.getPiece().isRight(move) ?
                    new Move(getTile(0,7), getTile(0,5)) :
                    new Move(getTile(0,0), getTile(0,3));
            movePieces(m);
        }
        else { // BLACK
            m = move.getPiece().isRight(move) ?
                    new Move(getTile(7,7), getTile(7,5)) :
                    new Move(getTile(7,0), getTile(7,3));
            movePieces(m);
        }
        halfMoves++;
    }

    public void doEnPassant(Move move) {
        if (move.getColor() == PColor.WHITE) {
            Tile enemyTile = getTile(move.getEnd().x-1, move.getEnd().y);
            takenPieces.add(enemyTile.getPiece());
            enemyTile.setPiece(null);
        }
    }

    public void doPromotion(Piece piece) {
        board.getLastMove().getEnd().setPiece(piece);
        isOver();
    }


    // Evaluate game

    public boolean isCheckmate() {
        boolean opponentCheckmate = !canMoveOpponent() && checkOpponent();
        if (opponentCheckmate) result = currentPlayer.getColor() == PColor.WHITE ? Result.WHITE_WIN : Result.BLACK_WIN;
        return opponentCheckmate;
    }

    public boolean isStalemate() {
        boolean stalemate = !canMoveOpponent() && !checkOpponent();
        if (stalemate) result = Result.DRAW;
        return stalemate;
    }

    public boolean isInsufficientMaterial() {
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

    public boolean isFiftyMoves() {
        boolean fiftyMoves = fullMoves >= 50;
        if (fiftyMoves) result = Result.DRAW;
        return fiftyMoves;
    }

    public boolean isThreeFoldRep() {
        return false;
    }

    public boolean isOver() {
        boolean ended = isCheckmate() || isStalemate() || isInsufficientMaterial() || isFiftyMoves() || isThreeFoldRep();
        gameOver.set(ended);
        return ended;
    }

    public void movePieces(Move move) {
        move.getStart().setPiece(null);
        move.getEnd().setPiece(move.getPiece());
    }

    public void moveIncrement(Move move) {
        if (move.getPiece() instanceof Pawn || move.endOccupied()) {
            halfMoves = 0;
            fullMoves = 0;
        }
        else {
            halfMoves++;
        }
        if (move.getColor() == PColor.BLACK) fullMoves++;
    }

    public void switchPlayers() {
        if (!gameOver.get()) {
            currentPlayer.stopTimer();
            currentPlayer = players.values().stream().filter(player -> player != currentPlayer).collect(Collectors.toList()).get(0);
            currentPlayer.startTimer();

            // Move randomly
            if (currentPlayer instanceof RandomPlayer) {
                takeMove(randomMove());
            }
        }
    }

    private Move randomMove() {
        List<Tile> pieces = findPiecesColor(currentPlayer.getColor() == PColor.WHITE ? PColor.BLACK : PColor.WHITE);
        pieces = pieces.stream().filter(tile -> !findMovesPiece(tile).isEmpty()).collect(Collectors.toList());
        List<Move> moves = findMovesPiece(pieces.get(ThreadLocalRandom.current().nextInt(pieces.size())));
        return moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
    }

    public void stopTimers() {
        players.get(PColor.WHITE).stopTimer();
        players.get(PColor.BLACK).stopTimer();
    }

    public String generatePGN() {
        if (previousMoves.size() == 0) {
            return null;
        }
        return "1";
    }

    public Board getBoard() {
        return board;
    }

    public Tile getTile(int x, int y) {
        return board.getTile(x, y);
    }

    public Map<PColor, Player> getPlayers() {
        return players;
    }

    public Player getCurrent() {
        return currentPlayer;
    }

    public ObservableList<Move> getLastMoves() {
        return previousMoves;
    }

    public Move getLastMove() {
        return previousMoves.get(previousMoves.size()-1);
    }

    public ObjectProperty<MoveType> getSpecialMove() {
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
}

