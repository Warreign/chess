package cz.cvut.fel.pjv.shubevik.GUI;

import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.HIGHLIGHTED_TILE;
import static cz.cvut.fel.pjv.shubevik.GUI.GuiController.PIECE_MAP;

import cz.cvut.fel.pjv.shubevik.board.Board;
import cz.cvut.fel.pjv.shubevik.board.Tile;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.moves.Move;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.util.Map;

public class BoardController {

    private GuiController controller;
    private Game game;
    private TileView[][] tiles;
    private Map<TileView, Tile> tileMap;

    private Background old;

    private ObjectProperty<Move> moveProperty;
    private BooleanProperty moveValid;

    public BoardController(GuiController controller, TileView[][] tiles, Map<TileView, Tile> tileMap) {
        this.controller = controller;
        this.game = controller.getGame();
        this.tiles = tiles;
        this.tileMap = tileMap;

        moveProperty = new SimpleObjectProperty<>();
        moveValid = new SimpleBooleanProperty();

        addListeners();
    }

    public void updateBoard(Board board) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; ++y) {
                Tile t;
                if (board == null) {
                    t = game.getTile(x, y);
                }
                else {
                    t = board.getTile(x, y);
                }
                if (t.isOccupied()) {
                    tiles[x][y].setPiece(PIECE_MAP.get(t.getPiece().getClass()).get(t.getPieceColor()));
                }
                else {
                    tiles[x][y].setPiece(null);
                }
            }
        }
    }

    private EventHandler<MouseEvent> onDragDetected = e -> {
        TileView source = (TileView) e.getSource();

        if (source.isPresent() &&
                (controller.isFreeEdit() ||
                        (game.hasStarted() &&
                         game.getCurrentColor() ==  tileMap.get(source).getPieceColor()))) {
            Dragboard db = source.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putImage(source.getPiece());
            source.removePiece();
            db.setContent(content);

            old = source.getBackground();
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
            if (controller.isFreeEdit()) {
                game.movePieces(constructMove(e), true);
                updateBoard(null);
                e.setDropCompleted(true);
            }
            else {
                moveProperty.set(constructMove(e));
                if (moveValid.get()) {
                    updateBoard(null);
                }
                e.setDropCompleted(moveValid.get());
            }
        }
        e.consume();
    };

    private EventHandler<DragEvent> onDragDone = e -> {
        if (e.getTransferMode() != TransferMode.MOVE) {
            ((TileView) e.getGestureSource()).setPiece(e.getDragboard().getImage());
        }
        ((TileView)e.getGestureSource()).setBackground(old);
        e.consume();
    };

    private Move constructMove(DragEvent e) {
        Tile start = tileMap.get((TileView) e.getGestureSource());
        Tile end = tileMap.get((TileView) e.getGestureTarget());
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
                tiles[x][y].setOnDragDetected(null);
                tiles[x][y].setOnDragOver(null);
                tiles[x][y].setOnDragDropped(null);
                tiles[x][y].setOnDragDone(null);
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
