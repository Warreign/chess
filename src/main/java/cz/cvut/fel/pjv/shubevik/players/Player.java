package cz.cvut.fel.pjv.shubevik.players;

import cz.cvut.fel.pjv.shubevik.GUI.Timer;
import cz.cvut.fel.pjv.shubevik.game.PColor;

public abstract class Player {

    private final PColor pieceColor;
    private final String name;
    private final Timer timer;

    public Player(String name, PColor color, Timer timer) {
        this.name = name;
        pieceColor = color;
        this.timer = timer;
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

    public void startTimer() {
        timer.start();
    }

    public void stopTimer() {
        timer.stop();
    }
}
