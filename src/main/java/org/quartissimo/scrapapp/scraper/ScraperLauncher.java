package org.quartissimo.scrapapp.scraper;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ScraperLauncher {
    private final WebDriver driver;
    private final Scraper[] scrapers;

    public ScraperLauncher(Scraper... scrapers) {
        this.scrapers = scrapers;
        this.driver = initChromeDriver();

        for(Scraper scraper : scrapers){
            scraper.setDriver(driver);
        }
    }

    private WebDriver initChromeDriver(){
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0",
                //"--headless=new",
                "--disable-notifications"
        );
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
