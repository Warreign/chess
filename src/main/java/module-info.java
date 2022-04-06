module cz.cvut.fel.pjv.shubevik.chess {
    requires javafx.controls;
    requires javafx.fxml;


    opens cz.cvut.fel.pjv.shubevik.chess to javafx.fxml;
    exports cz.cvut.fel.pjv.shubevik.chess;
    exports cz.cvut.fel.pjv.shubevik;
    opens cz.cvut.fel.pjv.shubevik to javafx.fxml;
}