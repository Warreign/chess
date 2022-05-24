module cz.cvut.fel.pjv.shubevik {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;

    opens cz.cvut.fel.pjv.shubevik.GUI to javafx.fxml;
    exports cz.cvut.fel.pjv.shubevik.GUI;
    exports cz.cvut.fel.pjv.shubevik.game.pieces;
    exports cz.cvut.fel.pjv.shubevik.game;
    exports cz.cvut.fel.pjv.shubevik.game.players;
    exports cz.cvut.fel.pjv.shubevik.game.moves;
    exports cz.cvut.fel.pjv.shubevik.PGN;
}