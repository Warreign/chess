package cz.cvut.fel.pjv.shubevik.GUI;

import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.pieces.*;
import cz.cvut.fel.pjv.shubevik.players.HumanPlayer;
import cz.cvut.fel.pjv.shubevik.players.Player;
import cz.cvut.fel.pjv.shubevik.players.RandomPlayer;
import javafx.application.Application;

import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;


public class GuiController extends Application {

    public static final Image GAME_ICON = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/white_king.png"));

    public static final Image ROOK_WHITE = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/white_rook.png"));
    public static final Image ROOK_BLACK = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/black_rook.png"));
    public static final Image KNIGHT_WHITE = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/white_knight.png"));
    public static final Image KNIGHT_BLACK = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/black_knight.png"));
    public static final Image BISHOP_WHITE = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/white_bishop.png"));
    public static final Image BISHOP_BLACK = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/black_bishop.png"));
    public static final Image QUEEN_WHITE = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/white_queen.png"));
    public static final Image QUEEN_BLACK = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/black_queen.png"));
    public static final Image KING_WHITE = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/white_king.png"));
    public static final Image KING_BLACK = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/black_king.png"));
    public static final Image PAWN_WHITE = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/white_pawn.png"));
    public static final Image PAWN_BLACK = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/black_pawn.png"));

    public static final Map<Class, Map<PColor, Image>> pieceMap = new HashMap<>();

    public Stage main;
    public Scene menuScene;
    public Game game;
    public GridPane board;

    @Override
    public void start(Stage stage) {
        main = stage;
        menuScene = new MenuScene(this,900, 600);
        initConst();

        stage.setTitle("Chess");
        stage.getIcons().add(GAME_ICON);

        stage.minWidthProperty().bind(stage.heightProperty().multiply(1.5));
        stage.maxWidthProperty().bind(stage.heightProperty().multiply(1.5));
        stage.setMinHeight(600);

        openMenu();
        stage.show();
//        startGame("James", "Connor", PColor.WHITE, PColor.BLACK, false, false, 1200, true);
    }

    public static void main(String[] args) {
        launch();
    }

    private void initConst() {
        Map<PColor, Image> rook = new HashMap<>();
        rook.put(PColor.WHITE, ROOK_WHITE);
        rook.put(PColor.BLACK, ROOK_BLACK);
        pieceMap.put(Rook.class, rook);
        Map<PColor, Image> knight = new HashMap<>();
        knight.put(PColor.WHITE, KNIGHT_WHITE);
        knight.put(PColor.BLACK, KNIGHT_BLACK);
        pieceMap.put(Knight.class, knight);
        Map<PColor, Image> bishop = new HashMap<>();
        bishop.put(PColor.WHITE, BISHOP_WHITE);
        bishop.put(PColor.BLACK, BISHOP_BLACK);
        pieceMap.put(Bishop.class, bishop);
        Map<PColor, Image> queen = new HashMap<>();
        queen.put(PColor.WHITE, QUEEN_WHITE);
        queen.put(PColor.BLACK, QUEEN_BLACK);
        pieceMap.put(Queen.class, queen);
        Map<PColor, Image> king = new HashMap<>();
        king.put(PColor.WHITE, KING_WHITE);
        king.put(PColor.BLACK, KING_BLACK);
        pieceMap.put(King.class, king);
        Map<PColor, Image> pawn = new HashMap<>();
        pawn.put(PColor.WHITE, PAWN_WHITE);
        pawn.put(PColor.BLACK, PAWN_BLACK);
        pieceMap.put(Pawn.class, pawn);
    }

    public void openMenu() {
        if (!(main.getScene() instanceof MenuScene)) {
            main.setScene(menuScene);
        }
    }

    public void startGame(String name1, String name2, PColor color1, PColor color2, boolean ai1, boolean ai2, int time, boolean freeEdit) {
        Player p1 = ai1 ? new RandomPlayer(name1, color1, new Timer(time)) :
                            new HumanPlayer(name1, color1, new Timer(time));
        Player p2 = ai2 ? new RandomPlayer(name2, color2, new Timer(time)) :
                            new HumanPlayer(name2, color2, new Timer(time));
        game = new Game(p1, p2, time != 0);
        main.setScene(new GameScene(this, main.getWidth(), main.getHeight(), game, freeEdit));
    }
}