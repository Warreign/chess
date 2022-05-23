package cz.cvut.fel.pjv.shubevik.players;

import cz.cvut.fel.pjv.shubevik.GUI.Timer;
import cz.cvut.fel.pjv.shubevik.game.PColor;

public class Player {

    private final PColor pieceColor;
    private final String name;
    private final Timer timer;
    private final PlayerType type;

    public Player(String name, PColor color, Timer timer, PlayerType type) {
        this.name = name;
        pieceColor = color;
        this.timer = timer;
        this.type = type;
    }

    public void startTimer() {
        if (timer != null) timer.start();
    }

    public void stopTimer() {
        if (timer != null) timer.stop();
    }

    public PColor getColor() {
        return pieceColor;
    }

    public String getName() {
        return name;
    }

    public Timer getTimer() {
        return timer;
    }

    public PlayerType getType() {
        return type;
    }
}
