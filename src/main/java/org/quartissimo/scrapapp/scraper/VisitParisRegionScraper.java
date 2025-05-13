package org.quartissimo.scrapapp.scraper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.quartissimo.scrapapp.scraper.models.Activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VisitParisRegionScraper extends Scraper {
    private double i;
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
        ArrayList<Activity> activities = this.scrapeMosaicTypePage();

        // Export all activities in a JSON file
        ObjectMapper mapper = new ObjectMapper();

        try{
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            String filePath = "src/main/resources/export.json";
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), activities);
            System.out.println("Export done.");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void handleCookiesPopup(){
        List<WebElement> cookies = this.driver.findElements(By.cssSelector("#cookieScreen .cn-decline"));
        if(!cookies.isEmpty()){
            cookies.getFirst().click();
        }
    }

    private ArrayList<Activity> scrapeMosaicTypePage() throws InterruptedException {
        List<String> pageLinks = new ArrayList<>();
        WebElement firstContainer = this.driver.findElements(By.className("crtRowLayout")).getFirst();
        //System.out.println(firstContainer.getAttribute("innerHTML"));
        List<WebElement> links = firstContainer.findElements(By.className("crtTeaserSmall"));
        for(WebElement a_tag : links){
            pageLinks.addLast(a_tag.getAttribute("href"));
        }

        // We check for the shopping page because it is the only mosaic page that has 2 mosaic panels, and we only want the first one to avoid a loop
        String currentUrl = driver.getCurrentUrl();
        if(currentUrl != null && currentUrl.equals("https://www.visitparisregion.com/fr/a-voir-a-faire/faire-du-shopping")){
            pageLinks = pageLinks.subList(0, 3);
        }

        System.out.println(pageLinks);
        ArrayList<Activity> activities = new ArrayList<>();
        for(String link : pageLinks){
            this.driver.get(link);
            waiting(500, 700);

            List<WebElement> elements = this.driver.findElements(By.className("crtTeaser-content"));
            if(!elements.isEmpty()){ // If it's an activity list page
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

                    activities.addAll(this.scrapeListTypePage());
                }
            } else { // If it's a mosaic page
                activities.addAll(this.scrapeMosaicTypePage());
            }
            if(pageLinks.size() == 8){
                this.i+=100/8;
                System.out.println(i + "%");
            }
        }
        return activities;
    }

    private ArrayList<Activity> scrapeListTypePage() throws InterruptedException {
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


            // Get the activity teaser
            Optional<WebElement> shortDesc = this.safeFindElementInElement(article, By.className("crtTeaser-desc"));
            if (shortDesc.isPresent()) {
                String teaser = shortDesc.get().getAttribute("innerText");
                newActivity.setShortDescription(teaser != null ? teaser.trim() : "");
            }

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

            // Get opening hours
            Optional<WebElement> hoursContainer = this.safeFindElementInElement(infos, By.cssSelector("div.crtProductOpeningDays div.decorated"));
            if (hoursContainer.isPresent()) {
                newActivity.setAvailabilities(hoursContainer.get().getText());
            }

            // Get prices
            Optional<WebElement> pricesContainer = this.safeFindElementInElement(infos, By.cssSelector("div.crtProductPrices div.decorated"));
            if (pricesContainer.isPresent()) {
                newActivity.setPrices(pricesContainer.get().getText());
            }

            // Closing tab and going back to the first one
            this.closeTab();

            activities.addLast(newActivity);
            if (++i == 1) return activities;
        }
        return activities;
    }
}
