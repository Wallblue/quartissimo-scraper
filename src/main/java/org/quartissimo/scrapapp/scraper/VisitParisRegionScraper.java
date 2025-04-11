package org.quartissimo.scrapapp.scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class VisitParisRegionScraper extends Scraper {
    public VisitParisRegionScraper(){
        this.domainUrl = "https://www.visitparisregion.com/fr";
    }

    public VisitParisRegionScraper(int minWaitingTime, int maxWaitingTime){
        super(minWaitingTime, maxWaitingTime);
        this.domainUrl = "https://www.visitparisregion.com/fr";
    }

    @Override
    public void scrape() throws Exception {
        this.loadPage(this.domainUrl, By.className("crtTeaserSmall"));
        this.waiting();
        this.handleCookiesPopup();
        this.waiting();

        List<String> pageLinks = new ArrayList<>();
        for(WebElement a_tag : driver.findElements(By.className("crtTeaserSmall"))){
            pageLinks.addLast(a_tag.getAttribute("href"));
        }

        for(String link : pageLinks){
            // Vérifier la forme de la page
            // Selon, parcourir les informations
            // Pour les pages en colonne:
            //      Ouvrir onglet, prendre infos, fermer onglet
        }
    }

    private void handleCookiesPopup(){
        List<WebElement> cookies = this.driver.findElements(By.cssSelector("#cookieScreen .cn-decline"));
        if(!cookies.isEmpty()){
            cookies.getFirst().click();
        }
    }
}
