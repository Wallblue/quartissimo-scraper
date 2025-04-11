package org.quartissimo.scrapapp.scraper;

public abstract class Scraper {
    private String domainUrl;
    public abstract void scrape() throws Exception;
}
