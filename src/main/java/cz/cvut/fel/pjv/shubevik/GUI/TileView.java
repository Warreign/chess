package cz.cvut.fel.pjv.shubevik.GUI;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class TileView extends Pane {

    public enum MPos {
        LEFT_UP,
        RIGHT_DOWN
    }

    private final ImageView pieceView;

    public TileView() {
        pieceView = new ImageView();
        pieceView.fitWidthProperty().bind(widthProperty());
        pieceView.fitHeightProperty().bind(heightProperty());
        getChildren().add(pieceView);
    }

    public Image getPiece() {
        return pieceView.getImage();
    }

    public void setPiece(Image pieceImage) {
        pieceView.setImage(pieceImage);
    }

    public void removePiece() {
        pieceView.setImage(null);
    }

    public boolean isPresent() {
        return pieceView.getImage() != null;
    }

    public void putMarker(String marker, MPos pos) {
        Label mLabel = new Label(marker);
        if (pos == MPos.RIGHT_DOWN) {
            mLabel.layoutXProperty().bind(widthProperty().subtract(mLabel.widthProperty()).subtract(2));
            mLabel.layoutYProperty().bind(heightProperty().subtract(mLabel.heightProperty()).add(2));
        }
        else {
            mLabel.setLayoutX(2);
        }
        getChildren().add(mLabel);
    }
}
