package cz.cvut.fel.pjv.shubevik.GUI;

import cz.cvut.fel.pjv.shubevik.PGN.GameState;
import cz.cvut.fel.pjv.shubevik.PGN.PGNBuilder;
import cz.cvut.fel.pjv.shubevik.game.Tile;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.game.Result;
import cz.cvut.fel.pjv.shubevik.game.moves.Move;
import cz.cvut.fel.pjv.shubevik.game.moves.SpecialMove;
import cz.cvut.fel.pjv.shubevik.game.pieces.*;
import cz.cvut.fel.pjv.shubevik.game.players.Player;
import cz.cvut.fel.pjv.shubevik.game.players.PlayerType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.*;

public class GameScene extends Scene {

    private GuiController controller;
    private Game game;

    private Pane root;
    private BoardController boardController;

    private ChangeListener<Move> userMoveListener;
    private ListChangeListener<Piece> takenPiecesListener;
    private ChangeListener<SpecialMove> specialMoveListener;
    private ChangeListener<Boolean> gameOverListener;
    private ChangeListener<Player> currentPlayerListener;

    private Stage promStage;
    private Scene whiteProm;
    private Scene blackProm;

    private GridPane board;
    private TileView[][] tiles;
    private Map<TileView, Tile> tileMap;


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
    private Label resultText;
    private Button startButton;
    private Button resignButton;

    private boolean viewMode;
    private int historyPosition;
    private Button backButton;
    private Button forwardButton;
    private Button toPgnButton;

    public GameScene(GuiController controller) {
        super(new Pane(), controller.getStage().getWidth(), controller.getStage().getHeight());
        root = (Pane) this.getRoot();
        this.controller = controller;
        this.game = controller.getGame();

        tileMap = new HashMap<>();
        tiles = new TileView[8][8];
        board = new GridPane();

        initBoard();
        initPanel();
        initListeners();
        initPromStage();
        boardController = new BoardController(controller, tiles, tileMap);
        boardController.updateBoard(null);
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
                if (x == 0) tile.putMarker(MARKERS_CHAR.get(y), TileView.MPos.RIGHT_DOWN);
                if (y == 0) tile.putMarker(MARKERS_NUM.get(x), TileView.MPos.LEFT_UP);

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
        time1.setText(p1.getTimer() != null ? TimeListener.convert(p1.getTimer().getTime()) : "" );
        time2.setText(p2.getTimer() != null ? TimeListener.convert(p2.getTimer().getTime()) : "" );
        if (p1.getTimer() != null) new TimeListener(time1, p1.getTimer().getProperty());
        if (p2.getTimer() != null) new TimeListener(time2, p2.getTimer().getProperty());

        // Name labels
        name1 = new Label(p1.getName());
        name2 = new Label(p2.getName());
        name1.setAlignment(Pos.CENTER);
        name2.setAlignment(Pos.CENTER);
        taken1 = new FlowPane(Orientation.HORIZONTAL);
        taken2 = new FlowPane(Orientation.HORIZONTAL);
        taken1.prefWidthProperty().bind(sidePanel.widthProperty());
        taken1.prefHeightProperty().bind(sidePanel.heightProperty().divide(10));
        taken2.prefWidthProperty().bind(sidePanel.widthProperty());
        taken2.prefHeightProperty().bind(sidePanel.heightProperty().divide(10));
        VBox playerTop = new VBox(10, name2, time2, taken2);
        VBox playerBottom = new VBox(10, taken1, time1, name1);
        playerTop.spacingProperty().bind(heightProperty().divide(100));
        playerBottom.spacingProperty().bind(heightProperty().divide(100));

        sidePanel.setTop(playerTop);
        sidePanel.setBottom(playerBottom);

        message = new Label();
        message.setWrapText(true);

        resultText = new Label();
        resultText.setWrapText(true);

        // Font autoresize
        IntegerProperty fontSize = new SimpleIntegerProperty();
        fontSize.bind(heightProperty().divide(25));
        sidePanel.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));

        heightProperty().addListener((observableValue, number, t1) -> {
            sidePanel.setPadding(new Insets(t1.doubleValue() / 40));
        });

        backButton = new Button("<-");
        backButton.setDisable(true);
        backButton.setOnAction(e -> {
            if (historyPosition - 1 < 0) message.setText("First move");
            else historyPosition--;
            displayState(game.getHistory().get(historyPosition));
        });

        toPgnButton = new Button("To PGN");
        toPgnButton.setOnAction(e -> {
//            System.out.println(PGNBuilder.gameToPGN(game));
            TextArea pgn = new TextArea();
            pgn.setWrapText(true);
            pgn.setText(PGNBuilder.gameToPGN(game));
            pgn.setEditable(false);
            BorderPane bp = new BorderPane();
            bp.setPadding(new Insets(5));
            bp.setCenter(pgn);
            Stage stage = new Stage();
            stage.initOwner(controller.getStage());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(bp, 500, 300));
            stage.show();
        });

        forwardButton = new Button("->");
        forwardButton.setDisable(true);
        forwardButton.setOnAction(e -> {
            if (historyPosition + 1 == game.getHistory().size()) message.setText("Last move");
            else historyPosition++;
            displayState(game.getHistory().get(historyPosition));
        });
        HBox buttonsRow1 = new HBox(10, backButton, toPgnButton, forwardButton);
        buttonsRow1.setAlignment(Pos.CENTER);

        startButton = new Button("Start");
        startButton.setOnAction(e -> {
            if (game.impossiblePosition()) {
                message.setText("Impossible position");
            }
            else {
                message.setText("Game in process");
                resignButton.setDisable(false);
                addListeners();
                controller.setFreeEdit(false);
                game.begin();
                startButton.setDisable(true);
            }
        });

        resignButton = new Button("Resign");
        resignButton.setDisable(true);
        resignButton.setOnAction(e -> {
            PColor winner = Game.opposite(game.getCurrentColor());
            game.endGameWithResult(winner == PColor.WHITE ? Result.WHITE_WIN : Result.BLACK_WIN);
        });
        HBox buttonsRow2 = new HBox(10, startButton, resignButton);
        buttonsRow2.setAlignment(Pos.CENTER);

        VBox middlePanel = new VBox(10, resultText,message, buttonsRow1, buttonsRow2);
        middlePanel.setAlignment(Pos.CENTER);

        sidePanel.setCenter(middlePanel);
//
//        Button b = new Button("Kings");
//        b.setOnAction(e -> {
//            for (Tile t : game.findPiecesColor(PColor.WHITE)) {
//                System.out.printf("White: %s %s\n", t.getPiece(), t);
//            }
//            for (Tile t : game.findPiecesColor(PColor.BLACK)) {
//                System.out.printf("Black: %s %s\n", t.getPiece(), t);
//            }
//            System.out.printf("Last move: %s\n", game.getLastMove());
//            System.out.printf("Current: %s\n", game.getCurrentColor());
//            System.out.println(game.getBoard());
//        });
//        sidePanel.setRight(b);

        root.getChildren().add(sidePanel);
    }

    private void initListeners() {
        userMoveListener = (observableValue, move, t1) -> boardController.getMoveValid().set(game.takeMove(t1));

        specialMoveListener = (observableValue, type, t1) -> {
            if (t1 == SpecialMove.CASTLING || t1 == SpecialMove.EN_PASSANT) {
                boardController.updateBoard(null);
                game.getSpecialMove().set(SpecialMove.NONE);
            }
            if (t1 == SpecialMove.PROMOTION) {
                if (game.getCurrent().get().getType() == PlayerType.HUMAN) {
                    openPromStage(game.getCurrentColor());
                }
                else {
                    setPromPiece(new Queen(game.getCurrentColor()), false);
                }
            }
        };

        takenPiecesListener = change -> {
            if (change.next() && change.wasAdded()) {
                Piece p = change.getAddedSubList().get(0);
                if (p.getColor() == PColor.BLACK) {
                    ImageView container = new ImageView(PIECE_MAP.get(p.getClass()).get(p.getColor()));
                    container.fitHeightProperty().bind(sidePanel.heightProperty().divide(20));
                    container.fitWidthProperty().bind(sidePanel.heightProperty().divide(20));
                    taken1.getChildren().add(container);
                }
                else { // WHITE
                    ImageView container = new ImageView(PIECE_MAP.get(p.getClass()).get(p.getColor()));
                    container.fitHeightProperty().bind(sidePanel.heightProperty().divide(20));
                    container.fitWidthProperty().bind(sidePanel.heightProperty().divide(20));
                    taken2.getChildren().add(container);
                }
            }
        };

        gameOverListener = (observableValue, aBoolean, t1) -> {
            if (t1) {
                game.stopTimers();
                switch (game.getResult()) {
                    case DRAW:
                        resultText.setText("Draw!");
                        break;
                    case WHITE_WIN:
                        resultText.setText("White won!");
                        break;
                    case BLACK_WIN:
                        resultText.setText("Black won!");
                        break;
                    default:
                        message.setText("");
                }
                game.appendState();
                boardController.removeListeners();
                enterViewMode();
            }
        };

        currentPlayerListener = (observableValue, player, t1) -> {
            if (t1.getType() == PlayerType.RANDOM) {
                game.randomMoveCurrent();
                boardController.updateBoard(null);
            }
        };
    }

    private void initPromStage() {
        promStage = new Stage();
        promStage.getIcons().add(GuiController.GAME_ICON);
        promStage.setTitle("Promotion");
        promStage.setResizable(false);
        promStage.initOwner(controller.getStage());
        promStage.initModality(Modality.WINDOW_MODAL);
        HBox whiteChoice = new HBox();
        HBox blackChoice = new HBox();
        whiteChoice.setAlignment(Pos.CENTER);
        blackChoice.setAlignment(Pos.CENTER);

        for (Class<?> c : Arrays.asList(Queen.class, Rook.class, Knight.class, Bishop.class)) {
            ImageView view = new ImageView(PIECE_MAP.get(c).get(PColor.WHITE));
            view.setFitWidth(promStage.getHeight()/2);
            view.setFitHeight(promStage.getHeight()/2);
            whiteChoice.getChildren().add(view);
            if (c == Queen.class) view.setOnMouseClicked(e -> setPromPiece(new Queen(PColor.WHITE), true));
            if (c == Rook.class) view.setOnMouseClicked(e -> setPromPiece(new Rook(PColor.WHITE), true));
            if (c == Knight.class) view.setOnMouseClicked(e -> setPromPiece(new Knight(PColor.WHITE), true));
            if (c == Bishop.class) view.setOnMouseClicked(e -> setPromPiece(new Bishop(PColor.WHITE), true));
        }
        for (Class<?> c : Arrays.asList(Queen.class, Rook.class, Knight.class, Bishop.class)) {
            ImageView view = new ImageView(PIECE_MAP.get(c).get(PColor.BLACK));
            view.setFitWidth(promStage.getHeight()/2);
            view.setFitHeight(promStage.getHeight()/2);
            blackChoice.getChildren().add(view);
            if (c == Queen.class) view.setOnMouseClicked(e -> setPromPiece(new Queen(PColor.BLACK), true));
            if (c == Rook.class) view.setOnMouseClicked(e -> setPromPiece(new Rook(PColor.BLACK),true));
            if (c == Knight.class) view.setOnMouseClicked(e -> setPromPiece(new Knight(PColor.BLACK),true));
            if (c == Bishop.class) view.setOnMouseClicked(e -> setPromPiece(new Bishop(PColor.BLACK), true));
        }
        whiteProm = new Scene(whiteChoice, 400, 150);
        blackProm = new Scene(blackChoice, 400, 150);

        promStage.setOnCloseRequest(e -> setPromPiece(new Queen(game.getCurrentColor()), true));
    }

    private void setPromPiece(Piece p, boolean user) {
        game.setPromPiece(p);
        boardController.updateBoard(null);
        game.getSpecialMove().set(SpecialMove.NONE);
        if (user) {
            game.evaluateAndSwitch();
        }
        promStage.close();
    }

    private void openPromStage(PColor color) {
        promStage.setScene(color == PColor.WHITE ? whiteProm : blackProm);
        promStage.show();
    }

    private void enterViewMode() {
        viewMode = true;
        resignButton.setDisable(true);
        backButton.setDisable(false);
        forwardButton.setDisable(false);
        historyPosition = game.getHistory().size()-1;
    }

    private void displayState(GameState state) {
        boardController.updateBoard(state.getBoard());
    }

    private void addListeners() {
        boardController.getMoveProperty().addListener(userMoveListener);
        game.getSpecialMove().addListener(specialMoveListener);
        game.getGameOver().addListener(gameOverListener);
        game.getTaken().addListener(takenPiecesListener);
        game.getCurrent().addListener(currentPlayerListener);
    }

    private void removeListeners() {
        boardController.getMoveProperty().removeListener(userMoveListener);
        game.getSpecialMove().removeListener(specialMoveListener);
        game.getGameOver().removeListener(gameOverListener);
        game.getTaken().removeListener(takenPiecesListener);
        game.getCurrent().removeListener(currentPlayerListener);
    }

}
