package cz.cvut.fel.pjv.shubevik.PGN;

import cz.cvut.fel.pjv.shubevik.game.*;
import cz.cvut.fel.pjv.shubevik.game.moves.Move;
import cz.cvut.fel.pjv.shubevik.game.pieces.*;
import cz.cvut.fel.pjv.shubevik.game.players.Player;
import cz.cvut.fel.pjv.shubevik.game.players.PlayerType;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
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

                                    int i = m.getStart().y == tmp.getStart().y ? 1 : 0;
                                    s.append(m.getStart().toString().toCharArray()[i]);
                                    break;
                                }
                            }
                        }
                    }

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

    public static String s1 = "4124[12]412412";

    public static String s = "[Date \"2022-05-23\"]\n" +
            "[White \"1\"]\n" +
            "[Black \"2\"]\n" +
            "[Result \"1/2-1/2\"]\n" +
            "1. Nf3 d6 2. h4 Na6 3. Rh3 c5 4. h5 b6 5. h6 f6 6. g4 d5 7. c4 Kf7 8. Ng1 Kg6 9. Bg2 Bd7 10. hxg7 Nb8 11. Rh2 b5 12. cxd5 Be8 13. Bh1 Qc7 14. Kf1 Nh6 15. a3 b4 16. f4 c4 17. e3 Qe5 18. e4 Kf7 19. d4 Qxf4+ 20. Nf3 Nd7 21. Rh4 f5 22. Rh2 Kg6 23. d6 Bxg7 24. dxe7 Rd8 25. Qe1 Rc8 26. Rh5 b3 27. gxf5+ Kf6 28. e5+ Nxe5 29. Qd2 Nxf5 30. Nc3 Bf7 31. Rb1 h6 32. Qe2 Rhg8 33. a4 Bh8 34. dxe5+ Kg6 35. Bg2 Bxe5 36. e8=Q Bxc3 37. a5 Rb8 38. Qxg8+ Ng7 39. Ra1 Kxh5 40. a6 Bf6 41. Bh1 Kg4 42. Bd2 h5 43. Kg2 c3 44. Ne5+ Kf5 45. bxc3 Bd8 46. c4 Ne8 47. Nd3 Rb7 48. Qe6+ Bxe6 49. Rb1 Bb6 50. Be3 Bf7 51. axb7 Ke4 52. Rc1 Qf6 53. Nb2 a6 54. Kg3+ Qf3+ 55. Kh2 Ba7 56. Bd4 Kxd4 57. Kg1 Qf6 58. Qf8 Qh4 59. Qb4 Nd6 60. c5+ Nc4 61. Ra1 Qh3 62. Kf2 Be6 63. Bg2 Bg8 64. Ra2 h4 65. Ke1 Bxc5 66. Bf3 Bf7 67. b8=Q Qe6+ 68. Be4 bxa2 69. Qbb5 Bh5 70. Kf2 Qf7+ 71. Bf3 Qh7 72. Qd7+ Bd6 73. Bd5 h3 74. Bf3 h2 75. Qc3+ Kxc3 76. Nd3 Bf7 77. Bg4 Kb3 78. Qe7 Qh6 79. Ke2 a1=Q 80. Nb2 Bb8 81. Nxc4 Qf4 82. Kd3 Qa4 83. Qxf7 Qb4 84. Ke2 h1=Q 85. Kd3 Qf2 86. Qd5 a5 87. Qxa5 Qa3 88. Bh3 Bc7 89. Bg4 Qaf8 90. Bh5 Qff7 91. Nb6 Bb8 92. Bf3 Qfh5 93. Qa7 Qff1+ 94. Ke3 Qe2+ 95. Kd4 Qed1+ 96. Bxd1+ Kb4 97. Ke3 Qhh2 98. Kd3 Qf7 99. Na4 Qc2+ 100. Ke3 Kb5 101. Nb6 Kc6 102. Qa6 Qf1 103. Be2 Kc5 104. Nc4 Bc7 105. Bd3 Bg3 106. Ke4 Qh3 107. Ke3 Bh4+ 108. Kf4 Kd5 109. Qh6 Qe3+ 110. Nxe3+ Kd4 111. Qd6+ Kc3 112. Kg4 Kd2 113. Nc4+ Qxc4+ 114. Kf3 Ke1 115. Be2 Bg3 116. Qxg3+ Kd2 117. Bf1 Kd1 118. Bh3 Qa4 119. Qg7 Qc6+ 120. Ke3 Qc8 121. Qa1+ Kc2 122. Bd7 Kb3 123. Ke4 Kc4 124. Bxc8 Kb4 125. Qb2+ Ka5 126. Bh3 Ka6 127. Kf4 Ka5 128. Bd7 Ka6 129. Kf5 Ka7 130. Bc8 Ka8 131. Qf6 Kb8 132. Ba6 Kc7 133. Kg4 Kb8 134. Qb2+ Ka8 135. Qf6 Ka7 136. Qg7+ Kb6 137. Be2 Kc6 138. Qd7+ Kc5 139. Qg7 Kb6 140. Kg5 Kc5 141. Qh6 Kd4 142. Bf1 Ke4 143. Bb5 Ke5 144. Kg6 Ke4 145. Qh7 Kd4 146. Bc4 Ke3 147. Qa7+ Ke4 148. Ba2 Kd3 149. Bg8 Kd2 150. Kf7 Kc2 151. Qa2+ Kd3 152. Kf8 Ke3 153. Kf7 Kf4 154. Kf6 Ke3 155. Bf7 Kd4 156. Qa7+ Ke4 157. Qa3 Kf4 158. Kg6 Kg4 159. Ba2 Kh4 160. Bb1 Kg4 161. Qb3 Kf4 162. Be4 Kxe4 163. Kh7 Ke5 164. Kh6 Kd6 165. Kg6 Kc6 166. Qc3+ Kb5 167. Qc8 Ka5 168. Kf5 Kb4 169. Qa8 Kc4 170. Kg5 Kc5 171. Qc8+ Kb5 172. Qd8 Ka6 173. Qd7 Ka5 174. Qb7 Ka4 175. Qf7 Kb5 176. Kg4 Ka4 177. Kg3 Ka3 178. Kh3 Kb2 179. Kg4 Kc2 180. Qe7 Kb2 181. Qg7+ Kc2 182. Kh5 Kb3 183. Qb2+ Ka4 184. Kg4 Ka5 185. Kf4 Ka4 186. Qb5+ Kxb5 1/2-1/2";

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

        for (String n : moveNotations) {
            if (n.equals("O-O") || n.equals("0-0")) {
                if (!castling(game, true)) return null;
                continue;
            }
            if (n.equals("O-O-O") || n.equals("0-0-0")) {
                if (!castling(game, false)) return null;
                continue;
            }
            if (n.length() == 2 && wrong2Char(game, n)) {
                System.out.println(n);
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
            System.out.println(game.getBoard());
            System.out.println(n);
            System.out.println(game.getCurrentColor());
            System.out.println("\n");
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
        System.out.println(tile);
        System.out.println(c);
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
        if (!pawnMove(game, n, false)) return true;
        return false;
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
            System.out.println("check");
            return wrong5Char(game, n.substring(0, 5));
        }
        if ((get(n, 4) == '=') && wrong4Char(game, n.substring(0, 3))) return true;
        game.setPromPiece(classToInstance(game, charToClass(get(n, 5)), n.substring(2, 4)));
        game.evaluateAndSwitch();
        return false;
    }

    public static boolean wrong7Char(Game game, String n) {
        return (get(n, 6) == '+' || get(n, 6) == '=') && wrong6Char(game, n.substring(0, 5));
    }



}
