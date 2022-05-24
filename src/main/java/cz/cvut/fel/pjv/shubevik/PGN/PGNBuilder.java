package cz.cvut.fel.pjv.shubevik.PGN;

import cz.cvut.fel.pjv.shubevik.GUI.Timer;
import cz.cvut.fel.pjv.shubevik.game.*;
import cz.cvut.fel.pjv.shubevik.game.moves.Move;
import cz.cvut.fel.pjv.shubevik.game.pieces.*;
import cz.cvut.fel.pjv.shubevik.game.players.Player;
import cz.cvut.fel.pjv.shubevik.game.players.PlayerType;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.*;

public class PGNBuilder {

    static Logger logger = Logger.getLogger(PGNBuilder.class.getName());

    public static String boardToString(Board board) {
        StringBuilder s = new StringBuilder();
        Arrays.stream(board.getGrid())
                .flatMap(Arrays::stream)
                .filter(Tile::isOccupied)
                .forEach(t -> s.append(t.getPiece()
                        .getColor().toString().toLowerCase().charAt(0))
                        .append(t.getPiece().toString())
                        .append(t)
                        .append("-"));
        return s.toString();
    }

    public static Board stringToBoard(String str) {
        Board board = new Board(true);
        String[] pieceNotations = str.split("-");

        for (String n : pieceNotations) {
            if (n.length() == 3) {
                board.setPiece(n.substring(1,3), new Pawn(charToColor(n.charAt(0))));
            }
            else if (n.length() == 4) {
                PColor c = charToColor(n.charAt(0));
                try {
                    Piece p = charToClass(n.charAt(1)).getConstructor(PColor.class).newInstance(c);
                    board.setPiece(n.substring(2,4), p);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    logger.warning(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
            else {
                return null;
            }
        }
        return board;
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
            default:
                result = "*";
                break;
        }



        // Add tags
        s.append(String.format("[Date \"%s\"]\n", LocalDate.now()))
                .append(String.format("[White \"%s\"]\n", game.getPlayers().get(PColor.WHITE).getName()))
                .append(String.format("[Black \"%s\"]\n", game.getPlayers().get(PColor.BLACK).getName()))
                .append(String.format("[Result \"%s\"]\n", result))
                .append(String.format("[Board \"%s\"]\n", boardToString(game.getHistory().get(0).getBoard())))
                .append(String.format("[Time1 \"%s\"]\n", game.getTimer1() == null ? "*" : game.getTimer1().getTime()))
                .append(String.format("[Time2 \"%s\"]\n", game.getTimer2() == null ? "*" : game.getTimer2().getTime()))
                .append(String.format("[Type1 \"%s\"]\n", game.getPlayers().get(PColor.WHITE).getType().toString()))
                .append(String.format("[Type2 \"%s\"]\n", game.getPlayers().get(PColor.BLACK).getType().toString()));

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

                    Game testGame = new Game(state.getBoard().getCopy());
                    testGame.getTile(m.getEnd().x, m.getEnd().y).setPiece(null);
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
        Matcher m = Pattern.compile("\\[.*]\n").matcher(pgn);
        List<String> tags = m.results().map(MatchResult::group).collect(Collectors.toList());
        if (tags.size() < 3) return null;

        String name1 = null;
        String name2 = null;
        Result result = null;
        Board board = new Board(false);
        PlayerType type1 = PlayerType.HUMAN;
        PlayerType type2 = PlayerType.HUMAN;
        int time1 = 0;
        int time2 = 0;

        for (String tag : tags) {
            pgn = pgn.replaceAll(String.format("\\%s\\]\n",tag.replaceAll("[]\n]", "").replaceAll("\\*", "\\\\*")), "");
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
                case "Time1":
                    try {
                        time1 = Integer.parseUnsignedInt(val);
                    } catch (NumberFormatException ignored) {}
                    break;
                case "Time2":
                    try {
                        time2 = Integer.parseUnsignedInt(val);
                    } catch (NumberFormatException ignored) {}
                    break;
                case "Type1":
                    type1 = stringToType(val);
                    break;
                case "Type2":
                    type2 = stringToType(val);
                    break;
                case "Board":
                    board = stringToBoard(val);
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

        if (type1 == null || type2 == null ||
                (type1 == PlayerType.RANDOM && type2 == PlayerType.RANDOM && (time1 != 0 || time2 != 0))) return null;

        if (board == null) return null;

        Timer timer1 = (time1 > 0 && time2 > 0) ? new Timer(time1) : null;
        Timer timer2 = (time1 > 0 && time2 > 0) ? new Timer(time2) : null;

        // Moves
        Player p1 = new Player(name1, PColor.WHITE, null, type1);
        Player p2 = new Player(name2, PColor.BLACK, null, type2);
        Game game = new Game(p1, p2, board, true);
        game.begin();

        String moveRegex = "\\d+. \\[]";
        pgn = pgn.replaceAll("\\d+\\. ", "");
        List<String> moveNotations = Pattern.compile("[a-zA-Z][a-zA-Z1-9+=#\\-]+\\s").matcher(pgn).results().map(MatchResult::group).map(String::strip).collect(Collectors.toList());

        for (String n : moveNotations) {
            if (n.equals("O-O") || n.equals("0-0")) {
                if (!castling(game, true)) return null;
                continue;
            }
            if (n.equals("O-O-O") || n.equals("0-0-0")) {
                if (!castling(game, false)) return null;
                continue;
            }
            if (n.length() == 2 && wrong2Char(game, n)) return null;
            if (n.length() == 3 && wrong3Char(game, n)) return null;
            if (n.length() == 4 && wrong4Char(game, n)) return null;
            if (n.length() == 5 && wrong5Char(game, n)) return null;
            if (n.length() == 6 && wrong6Char(game, n)) return null;
            if (n.length() == 7 && wrong7Char(game, n)) return null;
        }

        if (result != Result.IN_PROCESS) game.getGameOver().set(true);
        game.setReconstruct(false);
        game.getPlayers().get(PColor.WHITE).setTimer(timer1);
        game.getPlayers().get(PColor.BLACK).setTimer(timer2);
        game.setResult(result);
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

    public static PColor charToColor(char c) {
        switch (c) {
            case 'w':
                return PColor.WHITE;
            case 'b':
                return PColor.BLACK;
            default:
                return null;
        }
    }

    public static PlayerType stringToType(String s) {
        switch (s) {
            case "RANDOM":
                return PlayerType.RANDOM;
            case "COMPUTER":
                return PlayerType.COMPUTER;
            default:
                return PlayerType.HUMAN;
        }
    }

    public static Piece classToInstance(Game game, Class<? extends Piece> c, String tile) {
        try {
            return c.getConstructor(PColor.class).newInstance(game.getLastMove().getColor());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            logger.warning(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static char get(String s, int i) {
        try {
            return s.toCharArray()[i];
        } catch (IndexOutOfBoundsException e) {
            logger.warning(e.getMessage());
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
            int sy = getCharIdx(n, 0);
            return game.takeMove(new Move(game.getTile(ex+off, sy), game.getTile(ex,ey)));
        }
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
            return wrong2Char(game, n.substring(0, 2));
        }
        return !normalMove(game, n, false);
    }
    public static boolean wrong4Char(Game game, String n) {
        if (get(n, 3) == '+' || get(n, 3) == '#') {
            return wrong3Char(game, n.substring(0, 3));
        }
        if (get(n, 1) == 'x' && Character.isLowerCase(get(n, 0))) {
            return !pawnMove(game, n, true);
        }
        if (get(n, 1) == 'x' && Character.isUpperCase(get(n,0))) {
            return wrong3Char(game, n.replaceAll("x", ""));
        }
        if (get(n, 2) == '=') {
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
        return (get(n, 6) == '+' || get(n, 6) == '=') && wrong6Char(game, n.substring(0, 6));
    }



}
