package cz.cvut.fel.pjv.shubevik.GUI;

import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.moves.Move;
import cz.cvut.fel.pjv.shubevik.moves.MoveType;
import cz.cvut.fel.pjv.shubevik.players.Player;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GameScene extends Scene {

    public static final Background BLACK_TILE = new Background(new BackgroundFill(Color.rgb(118, 150, 86), CornerRadii.EMPTY, Insets.EMPTY));
    public static final Background WHITE_TILE = new Background(new BackgroundFill(Color.rgb(238, 238, 210), CornerRadii.EMPTY, Insets.EMPTY));
    public static final Background HIGHLIGHTED_TILE = new Background(new BackgroundFill(Color.rgb(207, 141, 21), CornerRadii.EMPTY, Insets.EMPTY));

    private GuiController controller;
    private Game game;

    private Pane root;
    private BoardController boardController;

    private GridPane board;
    private TileView[][] tiles;
    private Map<TileView, Tile> tileMap;
    private List<String> markersLeft;
    private List<String> markersBottom;

    private BorderPane sidePanel;
    private Label name1;
    private Label name2;
    private Label time1;
    private Label time2;
    private FlowPane taken1;
    private FlowPane taken2;
    private TimeListener timeListener1;
    private TimeListener timeListener2;

    public GameScene(GuiController controller, double width, double height, Game game) {
        super(new Pane(), width, height);
        root = (Pane) this.getRoot();
        this.controller = controller;
        this.game = game;

        tileMap = new HashMap<>();
        tiles = new TileView[8][8];
        board = new GridPane();

        markersLeft = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
        markersBottom = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");

        initBoard();
        initPanel();
        boardController = new BoardController(controller, game, board, tiles, tileMap);
        boardController.updateBoard();
        initListeners();
    }

    private void initBoard() {
        board.prefHeightProperty().bind(heightProperty());
        board.prefWidthProperty().bind(heightProperty());
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                TileView tile = new TileView();
                board.add(tile, y, 7-x);
                tileMap.put(tile, game.getTile(x, y));
                // Tile position and dimensions
                tile.prefHeightProperty().bind(heightProperty().divide(8));
                tile.prefWidthProperty().bind(widthProperty().divide(8));

                // Color
                tile.setBackground((x + y) % 2 == 0 ? BLACK_TILE : WHITE_TILE);

                // Labels
                if (x == 0) tile.putMarker(markersBottom.get(y), TileView.MPos.RIGHT_DOWN);
                if (y == 0) tile.putMarker(markersLeft.get(x), TileView.MPos.LEFT_UP);

                // Piece image
                tiles[x][y] = tile;
            }
        }

        IntegerProperty fontSize = new SimpleIntegerProperty();
        fontSize.bind(heightProperty().divide(40));
        board.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";",
                                                        "-fx-text-fill: red;",
                                                        "-fx-font-family: Impact;"));

        root.getChildren().add(board);
    }

    private void initPanel() {
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        sidePanel = new BorderPane();
        // Set dimensions
        sidePanel.layoutXProperty().bind(board.widthProperty());
        sidePanel.prefWidthProperty().bind(widthProperty().subtract(board.widthProperty()));
        sidePanel.prefHeightProperty().bind(heightProperty());

        Player p1 = game.getPlayers().get(PColor.WHITE);
        Player p2 = game.getPlayers().get(PColor.BLACK);

        // Time labels
        time1 = new Label();
        time2 = new Label();
        time1.setText(TimeListener.convert(p1.getTimer().getTime()));
        time2.setText(TimeListener.convert(p2.getTimer().getTime()));
        new TimeListener(time1, p1.getTimer().getProperty());
        new TimeListener(time2, p2.getTimer().getProperty());

        // Name labels
        name1 = new Label(p1.getName());
        name2 = new Label(p2.getName());
        name1.setAlignment(Pos.CENTER);
        name2.setAlignment(Pos.CENTER);
        VBox playerTop = new VBox(10, name2, time2);
        VBox playerBottom = new VBox(10, name1, time1);
        playerTop.spacingProperty().bind(heightProperty().divide(100));
        playerBottom.spacingProperty().bind(heightProperty().divide(100));

        sidePanel.setTop(playerTop);
        sidePanel.setBottom(playerBottom);

        // Font autoresize
        IntegerProperty fontSize = new SimpleIntegerProperty();
        fontSize.bind(heightProperty().divide(25));
        sidePanel.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));

        heightProperty().addListener((observableValue, number, t1) -> {
            sidePanel.setPadding(new Insets(t1.doubleValue() / 40));
        });

        root.getChildren().add(sidePanel);
    }

    private void openPromStage() {

    }

    private void initListeners() {
        // Listener for move from user
        boardController.getMoveProperty().addListener((observableValue, move, t1) -> {
            System.out.println("Move spotted");
            boolean valid = game.takeMove(t1);
            boardController.getMoveValid().set(valid);
        });

        // Listener for special moves
        game.getSpecialMove().addListener((observableValue, moveType, t1) -> {
            if (t1 == MoveType.CASTLING || t1 == MoveType.EN_PASSANT) {
                boardController.updateBoard();
            }
            if (t1 == MoveType.PROMOTION) {
                openPromStage();
            }
        });
    }

}
