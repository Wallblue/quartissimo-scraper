package org.quartissimo.scrapapp.scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Random;

public abstract class Scraper {
    protected String domainUrl;
    protected WebDriver driver;
    protected int minWaitingTime = 1000;
    protected int maxWaitingTime = 3000;

    public Scraper(){}

    public Scraper(int minWaitingTime, int maxWaitingTime){
        this.minWaitingTime = minWaitingTime;
        this.maxWaitingTime = maxWaitingTime;
    }

    public Scraper(WebDriver driver){
        this.driver = driver;
    }

    public Scraper(WebDriver driver, int minWaitingTime, int maxWaitingTime){
        this.driver = driver;
        this.minWaitingTime = minWaitingTime;
        this.maxWaitingTime = maxWaitingTime;
    }

    public void waiting() throws InterruptedException {
        Random rand = new Random();
        Thread.sleep(this.minWaitingTime + rand.nextInt(this.maxWaitingTime));
    }

    protected void loadPage(String url, By elementToWaitFor){
        this.driver.get(url);
        WebDriverWait wait = new WebDriverWait(this.driver, Duration.ofSeconds(10)); //timeout = 10s
        wait.until(ExpectedConditions.presenceOfElementLocated(elementToWaitFor));
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public abstract void scrape() throws Exception;
}
