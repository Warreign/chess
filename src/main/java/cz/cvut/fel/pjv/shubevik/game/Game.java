package cz.cvut.fel.pjv.shubevik.game;

import cz.cvut.fel.pjv.shubevik.board.Board;
import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.moves.Move;
import cz.cvut.fel.pjv.shubevik.moves.MoveType;
import cz.cvut.fel.pjv.shubevik.pieces.*;
import cz.cvut.fel.pjv.shubevik.players.HumanPlayer;
import cz.cvut.fel.pjv.shubevik.players.Player;
import cz.cvut.fel.pjv.shubevik.players.RandomPlayer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.*;

public class Game {

    static Logger logger = Logger.getLogger(Game.class.getName());

    private Board board;
    Map<PColor, Player> players;
    private Player currentPlayer;
    private Optional<Result> result;
    private ObservableList<Piece> takenPieces;
    private ObservableMap<Move, MoveType> previousMoves;

    private int fullMoves;
    private int halfMoves;
    private BooleanProperty gameOver;
    private ObjectProperty<MoveType> specialMove;

    public Game(Player p1, Player p2) {
        takenPieces = FXCollections.observableList(new ArrayList<>());
        previousMoves = FXCollections.observableMap(new LinkedHashMap<>());

        players = new HashMap<>();
        players.put(p1.getColor(), p1);
        players.put(p2.getColor(), p2);

        result = Optional.empty();
        board = new Board();
        currentPlayer = players.get(PColor.WHITE);
        currentPlayer.startTimer();

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
            switchPlayers();
            return true;
        }
        logger.log(Level.INFO, "Invalid move");
        return false;
    }

    public void makeMove(Move move) {
        movePieces(move);
        // Castling
        if (move.getPiece() instanceof King && move.getPiece().yDiff(move) == 2) {
            System.out.println("Move is castling");
            doCastling(move);
            previousMoves.put(move, MoveType.CASTLING);
            specialMove.set(MoveType.CASTLING);
        }
        // En Passant
        else if (move.getPiece() instanceof Pawn && ((Pawn) move.getPiece()).isEnPassant(board,move)) {
            doEnPassant(move);
            previousMoves.put(move, MoveType.EN_PASSANT);
            specialMove.set(MoveType.EN_PASSANT);
        }
        // Promote
        else if (move.getPiece() instanceof Pawn && ((move.getColor() == PColor.WHITE && move.getEnd().x == 7) || (move.getColor() == PColor.BLACK && move.getEnd().x == 0))) {
            previousMoves.put(move,MoveType.PROMOTION);
            specialMove.set(MoveType.PROMOTION);
        }
        else {
            previousMoves.put(move, MoveType.NORMAL);
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
        System.out.println("move " + move.checkMove());
        System.out.println("player " + (currentPlayer == players.get(move.getColor())));
        System.out.println("piece " + move.getPiece().isValid(this, move));
        System.out.println("check " + checkAfterMove(move));
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

    public void doPromotion(Move move, Piece piece) {
        move.getEnd().setPiece(piece);
    }


    // Evaluate game

    public boolean isCheckmate() {
        boolean opponentCheckmate = !canMoveOpponent() && checkOpponent();
        if (opponentCheckmate) result = Optional.of(currentPlayer.getColor() == PColor.WHITE ? Result.WHITE_WIN : Result.BLACK_WIN);
        return opponentCheckmate;
    }

    public boolean isStalemate() {
        boolean stalemate = !canMoveOpponent() && checkOpponent();
        if (stalemate) result = Optional.of(Result.DRAW);
        return stalemate;
    }

    public boolean isInsufficientMaterial() {
        List<Tile> pieceTiles = findPiecesColor(currentPlayer.getColor() == PColor.WHITE ? PColor.BLACK : PColor.WHITE);
        List<Piece> pieces = new ArrayList<>();
        for (Tile t : pieceTiles) {
            pieces.add(t.getPiece());
        }
        return pieces.size() == 1 ||
                (pieces.size() == 2 &&
                        (pieces.get(0) instanceof Bishop || pieces.get(0) instanceof Knight ||
                         pieces.get(1) instanceof Bishop || pieces.get(1) instanceof Knight));
    }

    public boolean isFiftyMoves() {
        boolean fiftyMoves = fullMoves >= 50;
        if (fiftyMoves) result = Optional.of(Result.DRAW);
        return fiftyMoves;
    }

    public boolean isThreeFoldRep() {
        return false;
    }

    public boolean evaluateGame() {
        return isCheckmate() || isStalemate() || isInsufficientMaterial() || isFiftyMoves() || isThreeFoldRep();
    }

    public void movePieces(Move move) {
        move.getStart().getPiece().setWasMoved(true);
        move.getStart().setPiece(null);
        if (move.endOccupied()) {
            takenPieces.add(move.getEnd().getPiece());
        }
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
        currentPlayer.stopTimer();
        currentPlayer = players.values().stream().filter(player -> player != currentPlayer).collect(Collectors.toList()).get(0);
        currentPlayer.startTimer();
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

    public ObservableMap<Move, MoveType> getLastMoves() {
        return previousMoves;
    }

    public ObjectProperty<MoveType> getSpecialMove() {
        return specialMove;
    }
}

