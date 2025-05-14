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
import java.util.stream.Collectors;

public class VisitParisRegionScraper extends Scraper {
    private double i;
    private List<String> categoriesToScrape = new ArrayList<>();

    // Define a functional interface for the callback
    @FunctionalInterface
    public interface SiteScrapedCallback {
        void onSiteScraping(String siteName);
    }

    // Field to store the callback
    private SiteScrapedCallback siteScrapedCallback;

    public VisitParisRegionScraper(){
        this.domainUrl = "https://www.visitparisregion.com/fr";
    }

    public void setCategoriesToScrape(List<String> categories) {
        this.categoriesToScrape = categories;
    }

    // Method to set the callback
    public void setOnSiteScrapedCallback(SiteScrapedCallback callback) {
        this.siteScrapedCallback = callback;
    }

    // Method to call the callback
    private void notifySiteScraped(String siteName) {
        if (this.siteScrapedCallback != null) {
            this.siteScrapedCallback.onSiteScraping(siteName);
        }
    }

    @Override
    public void scrape() throws Exception {
        this.loadPage(this.domainUrl, By.className("crtTeaserSmall"));
        this.waiting(50, 100);
        this.handleCookiesPopup();
        this.waiting(30, 60);

        ArrayList<Activity> activities = this.scrapeMosaicTypePage();

        if (!categoriesToScrape.isEmpty()) {
            activities = activities.stream()
                    .filter(activity -> {
                        if (activity.getCategories().isEmpty()) return false;
                        for (String category : activity.getCategories()) {
                            if (categoriesToScrape.contains(category)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        }

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
        List<WebElement> links = firstContainer.findElements(By.className("crtTeaserSmall"));
        for(WebElement a_tag : links){
            pageLinks.addLast(a_tag.getAttribute("href"));
        }

        String currentUrl = driver.getCurrentUrl();
        if(currentUrl != null && currentUrl.equals("https://www.visitparisregion.com/fr/a-voir-a-faire/faire-du-shopping")){
            pageLinks = pageLinks.subList(0, Math.min(3, pageLinks.size()));
        }

        System.out.println(pageLinks);
        ArrayList<Activity> activities = new ArrayList<>();
        for(String link : pageLinks){
            // Notify UI of current site being scraped
            notifySiteScraped(link);

            this.driver.get(link);
            waiting(50, 100);

            List<WebElement> elements = this.driver.findElements(By.className("crtTeaser-content"));
            if(!elements.isEmpty()){
                List<WebElement> paginatorElement = driver.findElements(By.cssSelector("ol.crtPagination-list button"));
                int pageTotal;
                if(paginatorElement.isEmpty())
                    pageTotal = 1;
                else
                    pageTotal = Integer.parseInt(paginatorElement.getLast().getText());

                for(int currentPage = 1; currentPage <= pageTotal && currentPage <= 5; currentPage++) {
                    if(currentPage > 1) {
                        this.loadPage(link + "?page=" + currentPage, By.className("crtTeaser-content"));
                    }
                    this.waiting(50, 100);

                    activities.addAll(this.scrapeListTypePage());
                }
            } else {
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
        Activity newActivity;

        for (WebElement article : activityArticles) {
            newActivity = new Activity();

            WebElement titleLink = article.findElement(By.className("crtTeaser-title"));
            newActivity.setTitle(titleLink.getText());
            String activityPageLink = titleLink.getAttribute("href");

            // Notify UI of current activity being scraped
            notifySiteScraped(newActivity.getTitle());

            Optional<WebElement> shortDesc = this.safeFindElementInElement(article, By.className("crtTeaser-desc"));
            if (shortDesc.isPresent()) {
                String teaser = shortDesc.get().getAttribute("innerText");
                newActivity.setShortDescription(teaser != null ? teaser.trim() : "");
            }

            List<WebElement> categorySpans = article.findElements(By.className("crtTeaser-segment"));
            for (WebElement span : categorySpans)
                newActivity.pushCategory(span.getText());

            this.driver.switchTo().newWindow(WindowType.TAB);

            this.loadPage(activityPageLink, By.id("informations"));
            this.waiting(50, 100);
            WebElement infos = driver.findElement(By.id("informations"));

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

            Optional<WebElement> phoneNumberContainer = this.safeFindElementInElement(infos, By.cssSelector(".crtProductContact-phone a"));
            if (phoneNumberContainer.isPresent()) {
                newActivity.setPhoneNumber(phoneNumberContainer.get().getText());
            }

            Optional<WebElement> hoursContainer = this.safeFindElementInElement(infos, By.cssSelector("div.crtProductOpeningDays div.decorated"));
            if (hoursContainer.isPresent()) {
                newActivity.setAvailabilities(hoursContainer.get().getText());
            }

            Optional<WebElement> pricesContainer = this.safeFindElementInElement(infos, By.cssSelector("div.crtProductPrices div.decorated"));
            if (pricesContainer.isPresent()) {
                newActivity.setPrices(pricesContainer.get().getText());
            }

            this.closeTab();

            activities.addLast(newActivity);
        }
        return activities;
    }
}
