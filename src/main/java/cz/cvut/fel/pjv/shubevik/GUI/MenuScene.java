package cz.cvut.fel.pjv.shubevik.GUI;

import cz.cvut.fel.pjv.shubevik.game.PColor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Arrays;

public class MenuScene extends Scene {

    public static final Image BACKGROUND_IMAGE = new Image(GuiController.class.getResourceAsStream("/cz/cvut/fel/pjv/shubevik/images/menu.jpg"));

    private GuiController controller;
    private BorderPane root;
    private Stage settingsStage;

    // Game settings
    private TextField nameP1;
    private TextField nameP2;
    private CheckBox aiP1;
    private CheckBox aiP2;
    private ComboBox<PColor> colorP1;
    private ComboBox<PColor> colorP2;
    private CheckBox timed;
    private TextField timeHours;
    private TextField timeMinutes;


    MenuScene(GuiController controller, double width, double height) {
        super(new BorderPane(), width, height);
        root = (BorderPane) this.getRoot();
        this.controller = controller;
        initialize();
        initializeSettingsStage();
    }

    private void initialize() {
        // Set title
        Label title  = new Label("Chess");
        title.setFont(Font.font("Impact", FontPosture.REGULAR, 100));
        title.setTextAlignment(TextAlignment.CENTER);
        title.setTextFill(Color.WHITE);
        StackPane sp = new StackPane(title);
        sp.setAlignment(Pos.CENTER);
        root.setTop(sp);

        // Set background
        BackgroundImage bgiImage = new BackgroundImage(
                BACKGROUND_IMAGE,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(1.0, 1.0, true, true, true, true));
        Background bg = new Background(bgiImage);
        root.setBackground(bg);

        // Buttons
        VBox buttons = new VBox();
        root.setCenter(buttons);
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(10);
        buttons.maxWidthProperty().bind(root.widthProperty().divide(4));
        buttons.maxHeightProperty().bind(root.heightProperty().divide(3));

            // Play button
        Button playButton = new Button("Play");
        playButton.setOnAction(e -> settingsStage.show());
        playButton.maxWidthProperty().bind(buttons.widthProperty());
        playButton.prefHeightProperty().bind(buttons.heightProperty().divide(3));

            // Editor mode
        Button editButton = new Button("Editor Mode");
        editButton.maxWidthProperty().bind(buttons.widthProperty());
        editButton.prefHeightProperty().bind(buttons.heightProperty().divide(3));

            // Exit button
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> Platform.exit());
        quitButton.maxWidthProperty().bind(buttons.widthProperty());
        quitButton.prefHeightProperty().bind(buttons.heightProperty().divide(3));

        buttons.getChildren().addAll(playButton,editButton,quitButton);
    }

    private EventHandler<ActionEvent> playHandler = new EventHandler<>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            int time = 0;
            if (colorP1.getValue() == null || colorP2.getValue() == null || colorP1.getValue() == colorP2.getValue()) {
                colorP1.requestFocus();
                return;
            }
            if (nameP1.getText().isEmpty() || nameP2.getText().isEmpty()) {
                nameP1.requestFocus();
                return;
            }
            if (timed.isSelected()) {
                try {
                    time = Integer.parseInt(timeHours.getText()) * 3600 + Integer.parseInt(timeMinutes.getText()) * 60;
                } catch (NumberFormatException e) {
                    return;
                }
            }
            controller.startGame(nameP1.getText(), nameP2.getText(), colorP1.getValue(), colorP2.getValue(), aiP1.isSelected(), aiP2.isSelected(), time);
            settingsStage.close();
        }
    };

    private void initializeSettingsStage() {
        VBox settings = new VBox();
        settingsStage = new Stage();
        settingsStage.setScene(new Scene(settings, 300, 180));
        settingsStage.getIcons().add(GuiController.GAME_ICON);
        settingsStage.initOwner(controller.main);
        settingsStage.initModality(Modality.WINDOW_MODAL);

        settings.setSpacing(10);
        settings.setAlignment(Pos.CENTER);
        settings.setPadding(new Insets(10, 20, 15, 20));

        // Settings
            // Player 1
        aiP1 = new CheckBox("AI");
        nameP1 = new TextField();
        nameP1.setPromptText("Player 1");
        nameP1.setMaxWidth(getWidth() * 0.5);
        colorP1 = new ComboBox<>(FXCollections.observableList(Arrays.asList(PColor.WHITE, PColor.BLACK)));
        colorP1.setPromptText("Color");
        HBox p1 = new HBox(10, aiP1, nameP1, colorP1);
        p1.setAlignment(Pos.CENTER);

            // Player 2
        aiP2 = new CheckBox("AI");
        nameP2 = new TextField();
        nameP2.setPromptText("Player 2");
        nameP2.setMaxWidth(getWidth() * 0.5);
        colorP2 = new ComboBox<>(FXCollections.observableList(Arrays.asList(PColor.WHITE, PColor.BLACK)));
        colorP2.setPromptText("Color");
        HBox p2 = new HBox(10, aiP2, nameP2, colorP2);
        p2.setAlignment(Pos.CENTER);

            // Time option
        timed = new CheckBox("Time");
        timeHours = new TextField();
        timeHours.setPromptText("Hours");
        timeHours.setDisable(true);
        timeHours.setMaxWidth(getWidth()/14);
        timeMinutes = new TextField();
        timeMinutes.setPromptText("Minutes");
        timeMinutes.setDisable(true);
        timeMinutes.setMaxWidth(getWidth()/14);

        timed.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                timeHours.setDisable(false);
                timeMinutes.setDisable(false);
            }
            else {
                timeHours.setDisable(true);
                timeMinutes.setDisable(true);
            }
        });

        HBox timeChoice = new HBox(timed, timeHours, timeMinutes);
        timeChoice.setSpacing(20);
        timeChoice.setAlignment(Pos.CENTER);

            // Buttons
        Button playButton = new Button("Play");
        playButton.setOnAction(playHandler);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> settingsStage.close());
        cancelButton.setCancelButton(true);

        BorderPane actionButtons = new BorderPane();
        actionButtons.setLeft(cancelButton);
        actionButtons.setRight(playButton);

            // Title
        Label title = new Label("Game settings");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setAlignment(Pos.CENTER);

        settings.getChildren().addAll(title, p1, p2, timeChoice, actionButtons);
    }
}
