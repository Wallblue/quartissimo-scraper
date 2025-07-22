package org.quartissimo.scrapapp.scraper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.quartissimo.scrapapp.scraper.models.Activity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenericScraper extends Scraper {
    private String targetUrl;
    private List<Activity> activities = new ArrayList<>();

    public GenericScraper(String url) {
        this.targetUrl = url;
    }

    @Override
    public void scrape() throws Exception {
        try {
            // Charger la page avec Selenium
            driver.get(targetUrl);
            
            // Attendre que la page se charge
            Thread.sleep(3000);
            
            // Extraire le titre de la page
            String pageTitle = driver.getTitle();
            
            // Créer une activité basique avec les informations disponibles
            Activity activity = new Activity();
            activity.setTitle(pageTitle.isEmpty() ? "Page sans titre" : pageTitle);
            activity.setShortDescription(extractDescription());
            activity.setWebSite(targetUrl);
            
            // Extraire les métadonnées
            extractMetaData(activity);
            
            // Extraire les liens
            extractLinks(activity);
            
            activities.add(activity);
            
            // Sauvegarder les résultats
            saveResults();
            
        } catch (Exception e) {
            throw new Exception("Erreur lors du scraping de " + targetUrl + ": " + e.getMessage());
        }
    }

    private String extractDescription() {
        try {
            // Essayer d'extraire la description depuis les métadonnées
            List<WebElement> metaDesc = driver.findElements(By.cssSelector("meta[name=description]"));
            if (!metaDesc.isEmpty()) {
                return metaDesc.get(0).getAttribute("content");
            }
            
            // Sinon, prendre le premier paragraphe
            List<WebElement> paragraphs = driver.findElements(By.tagName("p"));
            if (!paragraphs.isEmpty()) {
                String text = paragraphs.get(0).getText().trim();
                return text.length() > 200 ? text.substring(0, 200) + "..." : text;
            }
            
            return "Aucune description disponible";
        } catch (Exception e) {
            return "Erreur lors de l'extraction de la description";
        }
    }

    private void extractMetaData(Activity activity) {
        try {
            // Extraire les informations de contact
            List<WebElement> phoneElements = driver.findElements(By.cssSelector("a[href^='tel:'], .phone, .telephone"));
            if (!phoneElements.isEmpty()) {
                activity.setPhoneNumber(phoneElements.get(0).getText().trim());
            }
            
            // Extraire l'adresse
            List<WebElement> addressElements = driver.findElements(By.cssSelector("address, .address"));
            if (!addressElements.isEmpty()) {
                String address = addressElements.get(0).getText().trim();
                activity.setAddress(address);
            }
            
            // Extraire les prix
            List<WebElement> priceElements = driver.findElements(By.cssSelector("[class*='price'], [class*='tarif'], .price, .tarif"));
            if (!priceElements.isEmpty()) {
                activity.setPrices(priceElements.get(0).getText().trim());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'extraction des métadonnées: " + e.getMessage());
        }
    }

    private void extractLinks(Activity activity) {
        try {
            List<String> categories = new ArrayList<>();
            
            // Essayer d'identifier des catégories basées sur les liens de navigation
            List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a, .nav a, .menu a, .navigation a"));
            for (WebElement link : navLinks) {
                String text = link.getText().trim();
                if (!text.isEmpty() && text.length() < 50) {
                    categories.add(text);
                }
            }
            
            // Limiter le nombre de catégories
            if (categories.size() > 10) {
                categories = categories.subList(0, 10);
            }
            
            activity.setCategories(new ArrayList<>(categories));
        } catch (Exception e) {
            System.err.println("Erreur lors de l'extraction des liens: " + e.getMessage());
        }
    }

    private void saveResults() {
        try {
            // Charger les activités existantes
            List<Activity> existingActivities = new ArrayList<>();
            Path jsonPath = Paths.get(System.getProperty("user.home"), ".quartissimo", "export.json");
            File jsonFile = jsonPath.toFile();
            if (jsonFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                existingActivities = mapper.readValue(jsonFile, new TypeReference<List<Activity>>() {});
            }
            
            // Ajouter les nouvelles activités
            existingActivities.addAll(activities);
            
            // Sauvegarder le fichier mis à jour
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, existingActivities);
            
            System.out.println("Scraping terminé pour: " + targetUrl);
            System.out.println("Activités trouvées: " + activities.size());
            System.out.println("Total des activités sauvegardées: " + existingActivities.size());
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    public List<Activity> getActivities() {
        return activities;
    }
} 