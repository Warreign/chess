package cz.cvut.fel.pjv.shubevik.PGN;

import cz.cvut.fel.pjv.shubevik.game.*;
import cz.cvut.fel.pjv.shubevik.game.moves.Move;
import cz.cvut.fel.pjv.shubevik.game.pieces.*;
import cz.cvut.fel.pjv.shubevik.game.players.Player;
import cz.cvut.fel.pjv.shubevik.game.players.PlayerType;
import javafx.application.Platform;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.*;

public class PGNBuilder {

    static Logger logger = Logger.getLogger(PGNBuilder.class.getName());

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

//                    boolean canMoveSame = false;
                    Game testGame = new Game(state.getBoard().getCopy());
                    testGame.getTile(m.getEnd().x, m.getEnd().y).setPiece(null);
//                    List<Tile> pieces = testGame.findPiecesColor(m.getColor());
//                    pieces.removeIf(t -> t.getPiece().getClass() != m.getPiece().getClass() || t.getPiece() instanceof Pawn);
//                    if (pieces.size() != 0) {
//                        for (Tile t : pieces) {
//                            List<Move> moves = testGame.findMovesPiece(t);
//                            for (Move tmp : moves)  {
//                                if (tmp.getEnd().equals(m.getEnd())) {
//
//                                    int i = m.getStart().y == tmp.getStart().y ? 1 : 0;
//                                    s.append(m.getStart().toString().toCharArray()[i]);
//                                    break;
//                                }
//                            }
//                        }
//                    }

                    testGame.findPiecesColor(m.getColor())
                            .stream()
                            .filter(t -> t.getPiece().getClass() == m.getPiece().getClass() && !(t.getPiece() instanceof Pawn))
                            .map(testGame::findMovesPiece)
                            .flatMap(Collection::stream)
                            .filter(move -> move.getEnd().equals(m.getEnd()))
                            .findFirst()
                            .ifPresent(move -> s.append(get(m.getStart().toString(), m.getStart().y == move.getStart().y ? 1 : 0)));

                    if (m.getPiece() instanceof Pawn && m.isCapture())
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

    public static Game PGNtoGame(String pgn) {
        // Tags
        Matcher m = Pattern.compile("\\[.*\\]\n").matcher(pgn);
        List<String> tags = m.results().map(MatchResult::group).collect(Collectors.toList());
        if (tags.size() < 3) return null;

        String name1 = null;
        String name2 = null;
        Result result = null;

        for (String tag : tags) {
            pgn = pgn.replaceAll(String.format("\\%s\\]\n",tag.replaceAll("[]\n]", "")), "");
            tag = tag.replaceAll("[\\[\\]\n]", "");
            String type = tag.split(" ")[0];
            String val = tag.split(" ")[1].replaceAll("\"", "");
            switch (type) {
                case "White":
                    name1 = val;
                    break;
                case "Black":
                    name2 = val;
                    break;
                case "Result":
                    switch (val) {
                        case "1-0":
                            result = Result.WHITE_WIN;
                            break;
                        case "0-1":
                            result = Result.BLACK_WIN;
                            break;
                        case "1/2-1/2":
                            result = Result.DRAW;
                            break;
                        case "*":
                            result = Result.IN_PROCESS;
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

        if (name1 == null || name2 == null || result == null) return null;

        // Moves
        Player p1 = new Player(name1, PColor.WHITE, null, PlayerType.HUMAN);
        Player p2 = new Player(name2, PColor.BLACK, null, PlayerType.HUMAN);
        Game game = new Game(p1, p2, true);
        game.begin();

        String moveRegex = "\\d+. \\[]";
        pgn = pgn.replaceAll("\\d+\\. ", "");
        List<String> moveNotations = Pattern.compile("[a-zA-Z1-9+=\\-]+\\s").matcher(pgn).results().map(MatchResult::group).map(String::strip).collect(Collectors.toList());

        System.out.println(moveNotations);
        for (String n : moveNotations) {
            System.out.println(n);
            if (n.equals("O-O") || n.equals("0-0")) {
                if (!castling(game, true)) return null;
                continue;
            }
            if (n.equals("O-O-O") || n.equals("0-0-0")) {
                if (!castling(game, false)) return null;
                continue;
            }
            if (n.length() == 2 && wrong2Char(game,n)) {
                System.out.println(n);
                System.out.println(game.getBoard());
                System.out.println(game.getCurrentColor());
                System.out.println("Wrong 2");
                return null;
            }
            if (n.length() == 3 && wrong3Char(game, n)) {
                System.out.println(n);
                System.out.println(game.getCurrentColor());
                System.out.println("Wrong 3");
                return null;
            }
            if (n.length() == 4 && wrong4Char(game, n)) {
                System.out.println(n);
                System.out.println(game.getCurrentColor());
                System.out.println("Wrong 4");
                return null;
            }
            if (n.length() == 5 && wrong5Char(game, n)) {
                System.out.println(n);
                System.out.println(game.getCurrentColor());
                System.out.println("Wrong 5");
                return null;
            }
            if (n.length() == 6 && wrong6Char(game, n)) {
                System.out.println(n);
                System.out.println(game.getCurrentColor());
                System.out.println("Wrong 6");
                return null;
            }
            if (n.length() == 7 && wrong7Char(game, n)) {
                System.out.println(n);
                System.out.println(game.getCurrentColor());
                System.out.println("Wrong 7");
                return null;
            }
//            System.out.println(game.getBoard());
//            System.out.println(n);
//            System.out.println(game.getCurrentColor());
//            System.out.println("\n");
        }
        return game;
    }
    public static Class<? extends Piece> charToClass(char c) {
        switch (c) {
            case 'N':
                return Knight.class;
            case 'Q':
                return Queen.class;
            case 'R':
                return Rook.class;
            case 'K':
                return King.class;
            case 'B':
                return Bishop.class;
            default:
                return Piece.class;
        }
    }

    public static Piece classToInstance(Game game, Class<? extends Piece> c, String tile) {
        try {
            return c.getConstructor(PColor.class).newInstance(game.getLastMove().getColor());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static char get(String s, int i) {
        try {
            return s.toCharArray()[i];
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return ' ';
        }
    }

    public static int getCharIdx(String s, int i) {
        return MARKERS_CHAR.indexOf(String.valueOf(get(s,i)));
    }

    public static int getNumIdx(String s, int i) {
        return MARKERS_NUM.indexOf(String.valueOf(get(s, i)));
    }

    public static boolean castling(Game game, boolean kingSide) {
        int x = game.getCurrentColor() == PColor.WHITE ? 0 : 7;
        return game.takeMove(new Move(game.getTile(x, 4), game.getTile(x, kingSide ? 6 : 2)));
    }

    public static boolean pawnMove(Game game, String n, boolean capture) {
        int ex = getNumIdx(n, capture ? 3 : 1);
        int ey = getCharIdx(n, capture ? 2 : 0);
        int off = game.getCurrentColor() == PColor.WHITE ? -1 : 1;
        if (capture) {
            System.out.println(n);
            System.out.println("pawn capture");
            int sy = getCharIdx(n, 0);
            return game.takeMove(new Move(game.getTile(ex+off, sy), game.getTile(ex,ey)));
        }
//        System.out.println(new Move(game.getTile(ex+off, ey), game.getTile(ex,ey)));
//        System.out.println(new Move(game.getTile(ex+2*off, ey), game.getTile(ex, ey)));
//        boolean a1 = game.takeMove(new Move(game.getTile(ex+off, ey), game.getTile(ex,ey)));
//        System.out.println(a1);
//        if (!a1) {
//            boolean a2 = game.takeMove(new Move(game.getTile(ex+2*off, ey), game.getTile(ex, ey)));
//            System.out.println(a2);
//            System.out.println(game.getBoard());
//            return a2;
//        }
//        return a1;
//        System.out.println(n);
//        System.out.println("" + ex+off + " " + ey);
//        if (game.takeMove(new Move(game.getTile(ex+off, ey), game.getTile(ex,ey)))) {
////            System.out.println("valid 1\n");
//            return true;
//        }
//        else if (game.takeMove(new Move(game.getTile(ex+2*off, ey), game.getTile(ex, ey)))) {
//            return true;
//        }
        return game.takeMove(new Move(game.getTile(ex+off, ey), game.getTile(ex,ey))) ||
                game.takeMove(new Move(game.getTile(ex+2*off, ey), game.getTile(ex, ey)));
    }

    public static boolean normalMove(Game game, String n, boolean overlap) {
        Class<? extends Piece> pieceClass = charToClass(get(n, 0));
        if (overlap) {
            Tile end = game.getTile(n.substring(2));
            if (Character.isDigit(get(n,1))) {
                int sx = getNumIdx(n, 1);
                return game.findPiecesColor(game.getCurrentColor())
                        .stream()
                        .filter(t -> t.getPiece().getClass() == pieceClass)
                        .filter(t -> t.x == sx)
                        .anyMatch(t -> game.takeMove(new Move(t, end)));
            } else if (Character.isLetter(get(n, 1))) {
                int sy = getCharIdx(n, 1);
                return game.findPiecesColor(game.getCurrentColor())
                        .stream()
                        .filter(t -> t.getPiece().getClass() == pieceClass)
                        .filter(t -> t.y == sy)
                        .anyMatch(t -> game.takeMove(new Move(t, end)));
            }
        }
        Tile end = game.getTile(n.substring(1));
        return game.findPiecesColor(game.getCurrentColor())
                .stream()
                .filter(t -> t.getPiece().getClass() == pieceClass)
                .anyMatch(t -> game.takeMove(new Move(t, end)));
    }

    public static boolean wrong2Char(Game game, String n) {
        return !pawnMove(game, n, false);
    }
    public static boolean wrong3Char(Game game, String n) {
        if (get(n, 2) == '+' || get(n, 2) == '#') {
            System.out.println("pawn check");
            return wrong2Char(game, n.substring(0, 2));
        }
        return !normalMove(game, n, false);
    }
    public static boolean wrong4Char(Game game, String n) {
        if (get(n, 3) == '+' || get(n, 3) == '#') {
            return wrong3Char(game, n.substring(0, 3));
        }
        if (get(n, 1) == 'x' && Character.isLowerCase(get(n, 0))) {
            System.out.println(n + " pawn en passant");
            return !pawnMove(game, n, true);
        }
        if (get(n, 1) == 'x' && Character.isUpperCase(get(n,0))) {
            return wrong3Char(game, n.replaceAll("x", ""));
        }
        if (get(n, 2) == '=') {
            System.out.println(n);
            if (wrong2Char(game, n.substring(0,2))) return true;
            game.setPromPiece(classToInstance(game, charToClass(get(n, 3)), n.substring(0, 2)));
            game.evaluateAndSwitch();
            return false;
        }
        if (get(n, 1) != 'x' && Character.isUpperCase(get(n,0))) {
            return !normalMove(game, n, true);
        }
        return true;
    }
    public static boolean wrong5Char(Game game, String n) {
        if (get(n, 4) == '+' || get(n, 4) == '#') {
            return wrong4Char(game, n.substring(0,4));
        }
        if ((get(n, 2) == 'x')) {
            return wrong4Char(game, n.replaceAll("x", ""));
        }
        return true;
    }

    public static boolean wrong6Char(Game game, String n) {
        if (get(n, 5) == '+' || get(n, 5) == '#') {
            return wrong5Char(game, n.substring(0, 5));
        }
        if ((get(n, 4) == '=') && wrong4Char(game, n.substring(0, 4))) return true;
        game.setPromPiece(classToInstance(game, charToClass(get(n, 5)), n.substring(2, 4)));
        game.evaluateAndSwitch();
        return false;
    }

    public static boolean wrong7Char(Game game, String n) {
        System.out.println(n.substring(0,6));
        return (get(n, 6) == '+' || get(n, 6) == '=') && wrong6Char(game, n.substring(0, 6));
    }



}
