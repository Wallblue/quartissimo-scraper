package org.quartissimo.scrapapp.scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.quartissimo.scrapapp.scraper.models.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VisitParisRegionScraper extends Scraper {
    public VisitParisRegionScraper(){
        this.domainUrl = "https://www.visitparisregion.com/fr";
    }

    @Override
    public void scrape() throws Exception {
        this.loadPage(this.domainUrl, By.className("crtTeaserSmall"));
        this.waiting(1000, 1500);
        this.handleCookiesPopup();
        this.waiting(500, 1000);

        // The first page of the website is a "mosaic" type of page
        this.scrapeMosaicTypePage();
        /*
        List<String> pageLinks = new ArrayList<>();
        for(WebElement a_tag : this.driver.findElements(By.className("crtTeaserSmall"))){
            pageLinks.addLast(a_tag.getAttribute("href"));
        }

        // Pour les musées déjà
        this.loadPage(pageLinks.getFirst(), By.className("crtTeaser-content"));
        // We get the total number of pages there are
        int pageTotal = Integer.parseInt(driver.findElements(By.cssSelector("ol.crtPagination-list button")).getLast().getText());

        // We scrape each page
        for(int currentPage = 1; currentPage <= pageTotal; currentPage++) {
            if(currentPage > 1) {
                this.loadPage(pageLinks.getFirst() + "?page=" + currentPage, By.className("crtTeaser-content"));
            }
            this.waiting(1000, 1500);

            scrapeListTypePage();
        }/

        for(String link : pageLinks){
            this.driver.get(link);
            this.conditionalWaiting(10000, By.className("crtTeaser-content"), By.className("crtTeaserSmall"));

            if(this.driver.findElements(By.className("crtTeaserSmall")).isEmpty()){ // If it's an activity list page
                // We get the total number of pages there are
                int pageTotal = Integer.parseInt(driver.findElements(By.cssSelector("ol.crtPagination-list button")).getLast().getText());

                // We scrape each page
                for(int currentPage = 1; currentPage <= pageTotal; currentPage++) {
                    if(currentPage > 1) {
                        this.loadPage(pageLinks.getFirst() + "?page=" + currentPage, By.className("crtTeaser-content"));
                    }
                    this.waiting(1000, 1500);

                    scrapeListTypePage();
                }
            } else { // If it's a mosaic page
                scrapeMosaicTypePage();
            }
        }*/
    }

    private void handleCookiesPopup(){
        List<WebElement> cookies = this.driver.findElements(By.cssSelector("#cookieScreen .cn-decline"));
        if(!cookies.isEmpty()){
            cookies.getFirst().click();
        }
    }

    private void scrapeMosaicTypePage() throws InterruptedException {
        List<String> pageLinks = new ArrayList<>();
        for(WebElement a_tag : this.driver.findElements(By.className("crtTeaserSmall"))){
            pageLinks.addLast(a_tag.getAttribute("href"));
        }

        for(String link : pageLinks){
            this.driver.get(link);
            this.conditionalWaiting(10000, By.className("crtTeaser-content"), By.className("crtTeaserSmall"));

            if(!this.driver.findElements(By.className("crtTeaser-content")).isEmpty()){ // If it's an activity list page
                // We get the total number of pages there are
                List<WebElement> paginatorElement = driver.findElements(By.cssSelector("ol.crtPagination-list button"));
                int pageTotal;
                if(paginatorElement.isEmpty())
                    pageTotal = 1; // The paginator does not exist if there's only 1 page
                else
                    pageTotal = Integer.parseInt(paginatorElement.getLast().getText());

                // We scrape each page
                for(int currentPage = 1; currentPage <= pageTotal && currentPage <= 2; currentPage++) {
                    if(currentPage > 1) {
                        this.loadPage(link + "?page=" + currentPage, By.className("crtTeaser-content"));
                    }
                    this.waiting(1000, 1500);

                    scrapeListTypePage();
                }
            } else { // If it's a mosaic page
                //scrapeMosaicTypePage();
                System.out.println("Mosaic");
            }
        }
    }

    private void scrapeListTypePage() throws InterruptedException {
        ArrayList<Activity> activities = new ArrayList<>();

        int i = 0;
        List<WebElement> activityArticles = this.driver.findElements(By.className("crtTeaser-content"));
        Activity newActivity; // This will store the newActivity in the loop

        for (WebElement article : activityArticles) {
            newActivity = new Activity();

            // Get the activity name
            WebElement titleLink = article.findElement(By.className("crtTeaser-title"));
            newActivity.setTitle(titleLink.getText());
            String activityPageLink = titleLink.getAttribute("href");


            /*/ Get the activity teaser
            Optional<WebElement> shortDesc = this.safeFindElementInElement(article, By.className("crtTeaser-desc"));
            if (shortDesc.isPresent()) {
                String teaser = shortDesc.get().getAttribute("innerText");
                newActivity.setShortDescription(teaser != null ? teaser.trim() : "");
            }*/

            // Get the activity categories
            List<WebElement> categorySpans = article.findElements(By.className("crtTeaser-segment"));
            for (WebElement span : categorySpans)
                newActivity.pushCategory(span.getText());

            // Open a new tab to keep the articles list page alive
            this.driver.switchTo().newWindow(WindowType.TAB);

            // Load the activity page
            this.loadPage(activityPageLink, By.id("informations"));
            this.waiting(1000, 1500);

            /*
            // Get description
            Optional<WebElement> description = this.safeFindElement(By.cssSelector("#description .is-contrib"));
            if(description.isPresent()){
                newActivity.setDescription(description.get().getText());
            }
            */

            // TODO Get the exception bc without this it doesnt work
            WebElement infos = driver.findElement(By.id("informations"));

            // Get website
            Optional<WebElement> websiteContainer = this.safeFindElementInElement(infos, By.className("crtProductContact-link"));

            if (websiteContainer.isPresent()) {
                String link = websiteContainer.get().getAttribute("href");
                if (link != null) {
                    if (link.contains("play.google.com")) {
                        closeTab();
                        continue;
                    } else newActivity.setWebSite(link);
                }
            }

            // Get the address
            Optional<WebElement> addressFirstLineContainer = this.safeFindElementInElement(infos, By.className("crtProductContact-address-1"));
            if (addressFirstLineContainer.isPresent()) {
                newActivity.setAddress(addressFirstLineContainer.get().getText());
            }

            Optional<WebElement> addressSecondLineContainer = this.safeFindElementInElement(infos, By.className("crtProductContact-address-city"));
            if (addressSecondLineContainer.isPresent()) {
                String addressSecondLine = addressSecondLineContainer.get().getText();
                if(!addressSecondLine.isEmpty()) {
                    String[] zipcodeCityArray = addressSecondLine.split(" ");
                    newActivity.setZipcode(zipcodeCityArray[0]);
                    if (zipcodeCityArray.length > 1)
                        newActivity.setCity(zipcodeCityArray[1]);
                }
            }

            // Get phone number
            Optional<WebElement> phoneNumberContainer = this.safeFindElementInElement(infos, By.cssSelector(".crtProductContact-phone a"));
            if (phoneNumberContainer.isPresent()) {
                newActivity.setPhoneNumber(phoneNumberContainer.get().getText());
            }

            /*/ Get opening hours
            Optional<WebElement> hoursContainer = this.safeFindElementInElement(infos, By.cssSelector("div.crtProductOpeningDays div.decorated"));
            if (hoursContainer.isPresent()) {
                newActivity.setAvailabilities(hoursContainer.get().getText());
            }

            // Get prices
            Optional<WebElement> pricesContainer = this.safeFindElementInElement(infos, By.cssSelector("div.crtProductPrices div.decorated"));
            if (pricesContainer.isPresent()) {
                newActivity.setPrices(pricesContainer.get().getText());
            }*/

            // Closing tab and going back to the first one
            this.closeTab();

            activities.addLast(newActivity);
            System.out.println(newActivity);
            if (++i == 1) return;
        }
    }
}
