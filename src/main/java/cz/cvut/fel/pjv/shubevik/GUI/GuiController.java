package cz.cvut.fel.pjv.shubevik.GUI;

import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.pieces.*;
import cz.cvut.fel.pjv.shubevik.players.Player;
import cz.cvut.fel.pjv.shubevik.players.PlayerType;
import javafx.application.Application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GuiController extends Application {

    public static final Image GAME_ICON = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/pieces/white_king.png"));
    public static final Image BACKGROUND_IMAGE = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/menu.jpg"));

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
    public static final Map<Class<?>, Map<PColor, Image>> PIECE_MAP = new HashMap<>();

    public static final Background BLACK_TILE = new Background(new BackgroundFill(Color.rgb(118, 150, 86), CornerRadii.EMPTY, Insets.EMPTY));
    public static final Background WHITE_TILE = new Background(new BackgroundFill(Color.rgb(238, 238, 210), CornerRadii.EMPTY, Insets.EMPTY));
    public static final Background HIGHLIGHTED_TILE = new Background(new BackgroundFill(Color.rgb(70, 87, 64), CornerRadii.EMPTY, Insets.EMPTY));
    public static final List<String> MARKERS_NUM = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
    public static final List<String> MARKERS_CHAR = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");

    static {
        Map<PColor, Image> rook = new HashMap<>();
        rook.put(PColor.WHITE, ROOK_WHITE);
        rook.put(PColor.BLACK, ROOK_BLACK);
        PIECE_MAP.put(Rook.class, rook);

        Map<PColor, Image> knight = new HashMap<>();
        knight.put(PColor.WHITE, KNIGHT_WHITE);
        knight.put(PColor.BLACK, KNIGHT_BLACK);
        PIECE_MAP.put(Knight.class, knight);

        Map<PColor, Image> bishop = new HashMap<>();
        bishop.put(PColor.WHITE, BISHOP_WHITE);
        bishop.put(PColor.BLACK, BISHOP_BLACK);
        PIECE_MAP.put(Bishop.class, bishop);

        Map<PColor, Image> queen = new HashMap<>();
        queen.put(PColor.WHITE, QUEEN_WHITE);
        queen.put(PColor.BLACK, QUEEN_BLACK);
        PIECE_MAP.put(Queen.class, queen);

        Map<PColor, Image> king = new HashMap<>();
        king.put(PColor.WHITE, KING_WHITE);
        king.put(PColor.BLACK, KING_BLACK);
        PIECE_MAP.put(King.class, king);

        Map<PColor, Image> pawn = new HashMap<>();
        pawn.put(PColor.WHITE, PAWN_WHITE);
        pawn.put(PColor.BLACK, PAWN_BLACK);
        PIECE_MAP.put(Pawn.class, pawn);
    }

    private Stage stage;
    private Scene menuScene;
    private Scene gameScene;
    private Game game;
    private boolean freeEdit;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        menuScene = new MenuScene(this,900, 600);
        freeEdit = true;

        stage.setTitle("Chess");
        stage.getIcons().add(GAME_ICON);

        openMenu();

        stage.setMaxHeight(Screen.getPrimary().getBounds().getHeight());
        stage.minWidthProperty().bind(stage.heightProperty().multiply(1.5));
//        stage.minHeightProperty().bind(menuScene.widthProperty().divide(1.6));
        stage.setMinHeight(600);

        stage.show();
        startGame("James", "Connor", PColor.WHITE, PColor.BLACK, true, true, 0);
    }

    public static void main(String[] args) {
        launch();
    }

    public void openMenu() {
        if (!(stage.getScene() instanceof MenuScene)) {
            stage.setScene(menuScene);
        }
    }

    public void startGame(String name1, String name2, PColor color1, PColor color2, boolean ai1, boolean ai2, int time) {
        Player p1 = new Player(name1, color1, time != 0 ? new Timer(time) : null, ai1 ? PlayerType.RANDOM : PlayerType.HUMAN);
        Player p2 = new Player(name2, color2, time != 0 ? new Timer(time) : null, ai2 ? PlayerType.RANDOM : PlayerType.HUMAN);

        game = new Game(p1, p2, time != 0);
        gameScene = new GameScene(this);
        stage.setScene(gameScene);
    }

    public Game getGame() {
        return game;
    }

    public Scene getMenu() {
        return menuScene;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isFreeEdit() {
        return freeEdit;
    }

    public void setFreeEdit(boolean val) {
        freeEdit = val;
    }
}