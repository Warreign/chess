package cz.cvut.fel.pjv.shubevik.GUI;

import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.game.Result;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;

public class TimeListener implements ChangeListener<String> {

    private GuiController controller;
    private StringProperty property;
    private Label tileLabel;

    public TimeListener(GuiController controller, Label timeLabel, StringProperty stringProperty) {
        this.controller = controller;
        this.tileLabel = timeLabel;
        property = stringProperty;

        property.addListener(this);
    }

    public static String convert(long seconds) {
        return String.format("%02d:%02d:%02d",seconds / 3600, (seconds / 60) % 60, seconds % 60);
    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String number, String t1) {
        if (!t1.equals("")) {
            long time = Long.parseLong(t1);
            if (time == 0) {
                controller.getGame().endGameWithResult(controller.getGame().getCurrentColor() == PColor.WHITE ? Result.BLACK_WIN : Result.WHITE_WIN);
            }
            tileLabel.setText(convert(time));
        }
    }
    public void remove() {
        property.removeListener(this);
    }
}
