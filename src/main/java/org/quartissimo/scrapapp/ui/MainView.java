package org.quartissimo.scrapapp.ui;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.quartissimo.scrapapp.scraper.ScraperLauncher;
import org.quartissimo.scrapapp.scraper.VisitParisRegionScraper;

public class MainView extends VBox {
    public MainView(){
        Button btn = new Button("Scrape");
        btn.setOnMouseReleased(_ -> {
            ScraperLauncher scraperLauncher = new ScraperLauncher(new VisitParisRegionScraper());
            scraperLauncher.launchScrapers();
        });
        this.getChildren().addAll(btn);
    }
}
