package cz.cvut.fel.pjv.shubevik.GUI;

import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.KNIGHT_BLACK;
import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.pieceMap;

import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.moves.Move;
import cz.cvut.fel.pjv.shubevik.moves.MoveType;
import cz.cvut.fel.pjv.shubevik.pieces.*;
import cz.cvut.fel.pjv.shubevik.players.Player;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    private Stage promStage;
    private Scene whiteProm;
    private Scene blackProm;

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
    private Label message;

    private boolean freeEdit;

    public GameScene(GuiController controller, double width, double height, Game game, boolean freeEdit) {
        super(new Pane(), width, height);
        root = (Pane) this.getRoot();
        this.controller = controller;
        this.game = game;
        this.freeEdit = freeEdit;

        tileMap = new HashMap<>();
        tiles = new TileView[8][8];
        board = new GridPane();

        markersLeft = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
        markersBottom = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");

        initBoard();
        initPanel();
        initPromStage();
        boardController = new BoardController(controller, game, board, tiles, tileMap, freeEdit);
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
        taken1 = new FlowPane(Orientation.HORIZONTAL);
        taken2 = new FlowPane(Orientation.HORIZONTAL);
        taken1.prefWidthProperty().bind(sidePanel.widthProperty());
        taken1.prefHeightProperty().bind(sidePanel.heightProperty().divide(8));
        taken2.prefWidthProperty().bind(sidePanel.widthProperty());
        taken2.prefHeightProperty().bind(sidePanel.heightProperty().divide(8));
        taken1.setPadding(new Insets(10));
        taken2.setPadding(new Insets(10));
        VBox playerTop = new VBox(10, name2, time2, taken2);
        VBox playerBottom = new VBox(10, taken1, time1, name1);
        playerTop.spacingProperty().bind(heightProperty().divide(100));
        playerBottom.spacingProperty().bind(heightProperty().divide(100));

        sidePanel.setTop(playerTop);
        sidePanel.setBottom(playerBottom);

        message = new Label();
        sidePanel.setCenter(message);

        // Font autoresize
        IntegerProperty fontSize = new SimpleIntegerProperty();
        fontSize.bind(heightProperty().divide(25));
        sidePanel.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));

        heightProperty().addListener((observableValue, number, t1) -> {
            sidePanel.setPadding(new Insets(t1.doubleValue() / 40));
        });

        root.getChildren().add(sidePanel);
    }

    private void initPromStage() {
        promStage = new Stage();
        promStage.getIcons().add(GuiController.GAME_ICON);
        promStage.setTitle("Promotion");
        promStage.setResizable(false);
        promStage.initOwner(controller.main);
        promStage.initModality(Modality.WINDOW_MODAL);
        HBox whiteChoice = new HBox();
        HBox blackChoice = new HBox();
        whiteChoice.setAlignment(Pos.CENTER);
        blackChoice.setAlignment(Pos.CENTER);

        for (Class c : Arrays.asList(Queen.class, Rook.class, Knight.class, Bishop.class)) {
            ImageView view = new ImageView(pieceMap.get(c).get(PColor.WHITE));
            view.setFitWidth(promStage.getHeight()/2);
            view.setFitHeight(promStage.getHeight()/2);
            whiteChoice.getChildren().add(view);
            if (c == Queen.class) view.setOnMouseClicked(e -> {
                game.doPromotion(new Queen(PColor.WHITE));
                boardController.updateBoard();
                promStage.close();
            });
            if (c == Rook.class) view.setOnMouseClicked(e -> {
                game.doPromotion(new Rook(PColor.WHITE));
                boardController.updateBoard();
                promStage.close();
            });
            if (c == Knight.class) view.setOnMouseClicked(e -> {
                game.doPromotion(new Knight(PColor.WHITE));
                boardController.updateBoard();
                promStage.close();
            });
            if (c == Bishop.class) view.setOnMouseClicked(e -> {
                game.doPromotion(new Bishop(PColor.WHITE));
                boardController.updateBoard();
                promStage.close();
            });
        }
        for (Class c : Arrays.asList(Queen.class, Rook.class, Knight.class, Bishop.class)) {
            ImageView view = new ImageView(pieceMap.get(c).get(PColor.BLACK));
            view.setFitWidth(promStage.getHeight()/2);
            view.setFitHeight(promStage.getHeight()/2);
            blackChoice.getChildren().add(view);
            if (c == Queen.class) view.setOnMouseClicked(e -> {
                game.doPromotion(new Queen(PColor.BLACK));
                boardController.updateBoard();
                promStage.close();
            });
            if (c == Rook.class) view.setOnMouseClicked(e -> {
                game.doPromotion(new Rook(PColor.BLACK));
                boardController.updateBoard();
                promStage.close();
            });
            if (c == Knight.class) view.setOnMouseClicked(e -> {
                game.doPromotion(new Knight(PColor.BLACK));
                boardController.updateBoard();
                promStage.close();
            });
            if (c == Bishop.class) view.setOnMouseClicked(e -> {
                game.doPromotion(new Bishop(PColor.BLACK));
                boardController.updateBoard();
                promStage.close();
            });
        }
        whiteProm = new Scene(whiteChoice, 400, 150);
        blackProm = new Scene(blackChoice, 400, 150);

        promStage.setOnCloseRequest(e -> {
            game.doPromotion(new Queen(game.getLastMove().getColor()));
        });
    }

    private void openPromStage(PColor color) {
        promStage.setScene(color == PColor.WHITE ? whiteProm : blackProm);
        promStage.show();
    }

    private void initListeners() {
        // Listener for move from user
        boardController.getMoveProperty().addListener((observableValue, move, t1) -> {
            boolean valid = game.takeMove(t1);
            boardController.getMoveValid().set(valid);
        });

        // Listener for special moves
        game.getSpecialMove().addListener((observableValue, moveType, t1) -> {
            if (t1 == MoveType.CASTLING || t1 == MoveType.EN_PASSANT) {
                boardController.updateBoard();
            }
            if (t1 == MoveType.PROMOTION) {
                openPromStage(game.getLastMove().getColor());
            }
        });

        // Listener for game over
        game.getGameOver().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                game.stopTimers();
                switch (game.getResult()) {
                    case DRAW:
                        message.setText("Draw!");
                        break;
                    case WHITE_WIN:
                        message.setText("White won!");
                        break;
                    case BLACK_WIN:
                        message.setText("Black won!");
                        break;
                }
                boardController.removeListeners();
            }
        });

        game.getTaken().addListener(new ListChangeListener<Piece>() {
            @Override
            public void onChanged(Change<? extends Piece> change) {
                if (change.next() && change.wasAdded()) {
                    Piece p = change.getAddedSubList().get(0);
                    if (p.getColor() == PColor.WHITE) {
                        ImageView container = new ImageView(pieceMap.get(p.getClass()).get(p.getColor()));
                        container.fitHeightProperty().bind(taken1.widthProperty().divide(10));
                        container.fitWidthProperty().bind(taken1.widthProperty().divide(10));
                        taken1.getChildren().add(container);
                    }
                    else { // BLACK
                        ImageView container = new ImageView(pieceMap.get(p.getClass()).get(p.getColor()));
                        container.fitHeightProperty().bind(taken2.widthProperty().divide(10));
                        container.fitWidthProperty().bind(taken2.widthProperty().divide(10));
                        taken2.getChildren().add(container);
                    }
                }
            }
        });
    }

}
