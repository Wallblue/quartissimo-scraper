package org.quartissimo.scrapapp.scraper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ScraperLauncher {
    private final WebDriver driver;
    private Scraper[] scrapers;

    public ScraperLauncher(Scraper[] scrapers) {
        this.scrapers = scrapers;
        this.driver = initChromeDriver();
    }

    private WebDriver initChromeDriver(){
        ChromeOptions options = new ChromeOptions();

        return new ChromeDriver(options);
    }

    public void launchScrapers(){
        try{
            for(Scraper scraper: this.scrapers){
                scraper.scrape();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            this.driver.quit();
        }
    }
}
