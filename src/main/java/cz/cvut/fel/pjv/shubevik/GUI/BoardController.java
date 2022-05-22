package cz.cvut.fel.pjv.shubevik.GUI;

import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.pieceMap;
import static cz.cvut.fel.pjv.shubevik.GUI.GameScene.HIGHLIGHTED_TILE;

import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.moves.Move;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.util.Map;

public class BoardController {

    private GuiController controller;
    private Game game;
    private GridPane board;
    private Pane root;
    private TileView[][] tiles;
    private Map<TileView, Tile> tileMap;

    private Background temp;

    private ObjectProperty<Move> moveProperty;
    private BooleanProperty moveValid;

    public BoardController(GuiController controller, Game game, GridPane board, TileView[][] tiles, Map<TileView, Tile> tileMap) {
        this.controller = controller;
        this.game = game;
        this.board = board;
        this.root = (Pane) board.getParent();
        this.tiles = tiles;
        this.tileMap = tileMap;

        moveProperty = new SimpleObjectProperty<Move>();
        moveValid = new SimpleBooleanProperty();

        addListeners();
    }

    public void updateBoard() {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; ++y) {
                Tile t = game.getTile(x, y);
                if (t.isOccupied()) {
                    tiles[x][y].setPiece(pieceMap.get(t.getPiece().getClass()).get(t.getPieceColor()));
                }
                else {
                    tiles[x][y].setPiece(null);
                }
            }
        }
    }

    private EventHandler<MouseEvent> onDragDetected = e -> {
        TileView source = (TileView) e.getSource();

        if (source.isPresent() && game.getCurrent().getColor() ==  tileMap.get(source).getPieceColor()) {
            Dragboard db = source.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putImage(source.getPiece());
            source.removePiece();
            db.setContent(content);

            temp = source.getBackground();
            source.setBackground(HIGHLIGHTED_TILE);
        }
        e.consume();
    };

    private EventHandler<DragEvent> onDragOver = e -> {
        if (e.getSource() != e.getGestureSource() && e.getDragboard().hasImage()) {
            e.acceptTransferModes(TransferMode.MOVE);
        }
        e.consume();
    };

    private EventHandler<DragEvent> onDragDropped = e -> {
        moveValid.set(false);
        if (e.getDragboard().hasImage()) {
            TileView target = (TileView) e.getGestureTarget();
            moveProperty.set(constructMove(e));
            if (moveValid.get()) {
                target.setPiece(e.getDragboard().getImage());
            }
        }
        e.setDropCompleted(moveValid.get());
        e.consume();
    };

    private EventHandler<DragEvent> onDragDone = e -> {
        if (e.getTransferMode() != TransferMode.MOVE) {
            ((TileView) e.getGestureSource()).setPiece(e.getDragboard().getImage());
        }
        ((TileView)e.getGestureSource()).setBackground(temp);
        e.consume();
    };

    private Move constructMove(DragEvent e) {
        Tile start = tileMap.get((TileView) e.getGestureSource());
        Tile end = tileMap.get((TileView) e.getGestureTarget());
//        System.out.printf("Move: %d, %d -> %d, %d\n",start.x,start.y,end.x, end.y);
        return new Move(start, end);
    }

    public void addListeners() {
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                tiles[x][y].setOnDragDetected(onDragDetected);
                tiles[x][y].setOnDragOver(onDragOver);
                tiles[x][y].setOnDragDropped(onDragDropped);
                tiles[x][y].setOnDragDone(onDragDone);
            }
        }
    }

    public void removeListeners() {
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                tiles[x][y].removeEventHandler(MouseEvent.DRAG_DETECTED, onDragDetected);
                tiles[x][y].setOnDragOver(onDragOver);
                tiles[x][y].setOnDragDropped(onDragDropped);
                tiles[x][y].setOnDragDone(onDragDone);
            }
        }
    }

    public ObjectProperty<Move> getMoveProperty() {
        return moveProperty;
    }

    public BooleanProperty getMoveValid() {
        return moveValid;
    }
}
