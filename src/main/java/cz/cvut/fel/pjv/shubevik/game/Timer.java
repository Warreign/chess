package cz.cvut.fel.pjv.shubevik.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Timer {
    private long currentStartTime;
    private int remainingTime;
    private long offset;
    private boolean running;

    Thread listener;

    public Timer(int seconds) {
        currentStartTime = 0;
        offset = 0;
        remainingTime = seconds;
        running = false;
    }

    public void start() {
        if (!running) {
            currentStartTime = System.currentTimeMillis() + offset;
            listener = new Thread(listenerRunnable);
            listener.start();
        }
        running = true;
    }

    public void stop() {
        if (running) {
            listener.interrupt();
            listener = null;
            offset = System.currentTimeMillis() - currentStartTime;
        }
        running = false;
    }

    Runnable listenerRunnable = new Runnable() {
        @Override
        public void run() {
            while(running) {
                if (System.currentTimeMillis() - currentStartTime >= 1000) {
                    remainingTime--;
                    currentStartTime += 1000;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };
}
