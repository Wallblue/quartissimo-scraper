package org.quartissimo.scrapapp;

import io.github.bonigarcia.wdm.WebDriverManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.quartissimo.scrapapp.scraper.ScraperLauncher;
import org.quartissimo.scrapapp.scraper.VisitParisRegionScraper;
import org.quartissimo.scrapapp.ui.MainView;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(new MainView());
        stage.setScene(scene);
        stage.setHeight(720);
        stage.setWidth(1080);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}