package cz.cvut.fel.pjv.shubevik.GUI;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;

public class TimeListener implements ChangeListener<String> {

    private StringProperty property;
    private Label time;

    public TimeListener(Label timeLabel, StringProperty stringProperty) {
        time = timeLabel;
        property = stringProperty;
        property.addListener(this);

    }

    public static String convert(int seconds) {
        return String.format("%02d:%02d:%02d",seconds / 3600, (seconds / 60) % 60, seconds % 60);
    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String number, String t1) {
        if (!t1.equals("")) {
            time.setText(t1);
        }
    }

    public void remove() {
        property.removeListener(this);
    }
}
