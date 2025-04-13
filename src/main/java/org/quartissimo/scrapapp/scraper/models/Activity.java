package org.quartissimo.scrapapp.scraper.models;

import java.util.ArrayList;

public class Activity {
    private String title = "";
    private ArrayList<String> categories = new ArrayList<>();
    private String shortDescription = "";
    private String description = "";
    private String address = "";
    private String zipcode = "";
    private String city = "";
    private String phoneNumber = "";
    private String webSite = "";
    private String availabilities = "";
    private String prices = "";

    public Activity(){ }

    public Activity(
        String title, ArrayList<String> categories,
        String shortDescription, String description,
        String address, String zipcode, String city,
        String phoneNumber, String webSite,
        String availabilities, String prices
    ) {
        this.title = title;
        this.categories = categories;
        this.shortDescription = shortDescription;
        this.description = description;
        this.address = address;
        this.zipcode = zipcode;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.webSite = webSite;
        this.availabilities = availabilities;
        this.prices = prices;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "title='" + title + '\'' +
                ", categories=" + categories +
                ", shortDescription='" + shortDescription + '\'' +
                ", description='" + description + '\'' +
                ", address='" + address + '\'' +
                ", zipcode='" + zipcode + '\'' +
                ", city='" + city + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", webSite='" + webSite + '\'' +
                ", availabilities='" + availabilities + '\'' +
                ", prices='" + prices + '\'' +
                '}';
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public void pushCategory(String category) {
        this.categories.addLast(category);
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    public void setAvailabilities(String availabilities) {
        this.availabilities = availabilities;
    }

    public void setPrices(String prices) {
        this.prices = prices;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getZipcode() {
        return zipcode;
    }

    public String getCity() {
        return city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getWebSite() {
        return webSite;
    }

    public String getAvailabilities() {
        return availabilities;
    }

    public String getPrices() {
        return prices;
    }
}
