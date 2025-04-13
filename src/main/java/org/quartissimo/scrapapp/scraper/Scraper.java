package org.quartissimo.scrapapp.scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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

    public Scraper(){}

    public Scraper(WebDriver driver){
        this.driver = driver;
    }

    protected void waiting(int min, int max) throws InterruptedException {
        Random rand = new Random();
        Thread.sleep(min + rand.nextInt(max - min));
    }

    protected void conditionalWaiting(int timeoutInMs, By selectorA, By selectorB) throws NoSuchElementException {
        int waited = 0;
        final int timeToWait = 200;
        while(waited < timeoutInMs){
            boolean aPresent = !this.driver.findElements(selectorA).isEmpty();
            boolean bPresent = !this.driver.findElements(selectorB).isEmpty();

            if(aPresent || bPresent) return;

            try{
                Thread.sleep(timeToWait);
                waited += timeToWait;
            } catch (InterruptedException _) {}
        }
    }

    protected void loadPage(String url, By elementToWaitFor){
        this.driver.get(url);
        WebDriverWait wait = new WebDriverWait(this.driver, Duration.ofSeconds(10)); //timeout = 10s
        wait.until(ExpectedConditions.presenceOfElementLocated(elementToWaitFor));
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    // These methods allow us to look for an element that could be absent without crashing
    protected Optional<WebElement> safeFindElement(By by){
        List<WebElement> found = this.driver.findElements(by);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.getFirst());
    }

    protected Optional<WebElement> safeFindElementInElement(WebElement sourceElement, By by){
        List<WebElement> found = sourceElement.findElements(by);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.getFirst());
    }

    protected void closeTab(){
        Object[] windowHandles = this.driver.getWindowHandles().toArray();
        this.driver.close();
        this.driver.switchTo().window((String) windowHandles[0]);
    }

    public abstract void scrape() throws Exception;
}
