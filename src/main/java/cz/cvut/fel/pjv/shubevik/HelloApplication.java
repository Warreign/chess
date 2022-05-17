package cz.cvut.fel.pjv.shubevik;

import cz.cvut.fel.pjv.shubevik.game.Timer;
import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        stage.setTitle("Hello!");
        stage.show();

        Timer tm = new Timer(30);
        tm.start();
        Thread.sleep(10000);
        tm.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}