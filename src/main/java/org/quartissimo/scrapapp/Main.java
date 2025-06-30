package org.quartissimo.scrapapp;

import java.io.IOException;

import org.quartissimo.scrapapp.ui.MainView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        MainView mainView = new MainView();
        Scene scene = new Scene(mainView);
        mainView.setScene(scene);
        stage.setScene(scene);
        stage.setHeight(720);
        stage.setWidth(1080);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
