package org.quartissimo.scrapapp.scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
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

    public void waiting(int min, int max) throws InterruptedException {
        Random rand = new Random();
        Thread.sleep(min + rand.nextInt(max - min));
    }

    protected void loadPage(String url, By elementToWaitFor){
        this.driver.get(url);
        WebDriverWait wait = new WebDriverWait(this.driver, Duration.ofSeconds(10)); //timeout = 10s
        wait.until(ExpectedConditions.presenceOfElementLocated(elementToWaitFor));
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    // This methods allow us to look for an element that could be absent without crashing
    protected Optional<WebElement> safeFindElement(By by){
        List<WebElement> found = this.driver.findElements(by);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.getFirst());
    }

    protected Optional<WebElement> safeFindElementInElement(WebElement sourceElement, By by){
        List<WebElement> found = this.driver.findElements(by);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.getFirst());
    }

    protected void closeTab(){
        Object[] windowHandles = this.driver.getWindowHandles().toArray();
        this.driver.close();
        this.driver.switchTo().window((String) windowHandles[0]);
    }

    public abstract void scrape() throws Exception;
}
