module cz.cvut.fel.pjv.shubevik {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens cz.cvut.fel.pjv.shubevik.GUI to javafx.fxml;
    exports cz.cvut.fel.pjv.shubevik.GUI;
    exports cz.cvut.fel.pjv.shubevik.board;
    exports cz.cvut.fel.pjv.shubevik.pieces;
    exports cz.cvut.fel.pjv.shubevik.game;
    exports cz.cvut.fel.pjv.shubevik.players;
    exports cz.cvut.fel.pjv.shubevik.moves;
    exports cz.cvut.fel.pjv.shubevik.PGN;
}