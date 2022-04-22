package de.geko.gui;

import javafx.stage.Stage;

/**
 * @author Ruwen Lamm
 * instance of current Stage of main window
 */
public class CurrentWindow {
    private static final CurrentWindow ourInstance = new CurrentWindow();
    private Stage currStage;

    private CurrentWindow() {
    }

    public static CurrentWindow getInstance() {
        return ourInstance;
    }

    public Stage getCurrStage() {
        return currStage;
    }

    public void setCurrStage(Stage currStage) {
        this.currStage = currStage;
    }
}
