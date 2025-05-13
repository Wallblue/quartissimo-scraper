package org.quartissimo.scrapapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.quartissimo.scrapapp.ui.MainView;

import java.io.IOException;

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
