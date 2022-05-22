package cz.cvut.fel.pjv.shubevik.GUI;

import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class Timer {
    private long currentStartTime;
    private int time;
//    private IntegerProperty remainingTime;
//    private BooleanProperty timeOut;
    private long offset;
    private boolean running;
//    private Task<Void> listenerTask;

    Thread listener;

    public Timer(int seconds) {
        currentStartTime = 0;
        offset = 0;
        running = false;
        time = seconds;
    }

    public void start() {
        System.out.println("Timer start");
        if (!running) {
            currentStartTime = System.currentTimeMillis() - offset;
            service.restart();
        }
        running = true;
    }

    public void stop() {
        System.out.println("Timer stop");
        if (running) {
            offset = System.currentTimeMillis() - currentStartTime;
        }
        running = false;
    }

    private Service<Void> service = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    while (running) {
                        if (System.currentTimeMillis() - currentStartTime >= 1000) {
                            time--;
                            currentStartTime += 1000;
                            updateMessage(TimeListener.convert(time));
                            if (time == 0) {
                                stop();
                            }
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                }
            };
        }
    };
    public StringProperty getProperty() {
        return (StringProperty) service.messageProperty();
    }

    public int getTime() {
        return time;
    }
}
