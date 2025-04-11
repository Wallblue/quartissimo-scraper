package org.quartissimo.scrapapp;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}