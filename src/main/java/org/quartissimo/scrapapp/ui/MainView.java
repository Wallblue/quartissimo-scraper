package org.quartissimo.scrapapp.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.quartissimo.pluginapi.Plugin;
import org.quartissimo.scrapapp.PluginLoader;
import org.quartissimo.scrapapp.scraper.GenericScraper;
import org.quartissimo.scrapapp.scraper.ScraperLauncher;
import org.quartissimo.scrapapp.scraper.VisitParisRegionScraper;
import org.quartissimo.scrapapp.scraper.models.Activity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainView extends BorderPane {
    private final ListView<String> categoryListView;
    private final ListView<Activity> resultsListView;
    private final TextArea detailsArea;
    private final Button scrapeButton;
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private final Label currentSiteLabel;
    private final ExecutorService executorService;
    private Set<String> selectedCategories = new HashSet<>();
    private List<Activity> allActivities = new ArrayList<>();
    private Menu themesMenu;
    private Menu pluginMenu;
    private Scene scene;
    private File currentThemeFile;

    public MainView() {
        executorService = Executors.newSingleThreadExecutor();

        MenuBar menuBar = new MenuBar();
        themesMenu = new Menu("Thèmes");
        menuBar.getMenus().add(themesMenu);
        pluginMenu = new Menu("Plugins");
        menuBar.getMenus().add(pluginMenu);
        setTop(menuBar);

        pluginMenu.getItems().clear();

        PluginLoader loader = new PluginLoader();
        List<Plugin> plugins = loader.loadPluginsFrom();

        for (Plugin plugin : plugins) {
            MenuItem item = plugin.createMenuItem();

            if (item == null) {
                plugin.execute();
            } else {
                pluginMenu.getItems().add(item);
            }
        }

        VBox categoryPanel = new VBox(10);
        categoryPanel.setPadding(new Insets(10));
        Label categoryLabel = new Label("Filtrer par catégories");
        categoryLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        categoryListView = new ListView<>();
        categoryListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        loadCategories();
        categoryListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedCategories = new HashSet<>(categoryListView.getSelectionModel().getSelectedItems());
            filterActivities();
        });
        categoryPanel.getChildren().addAll(categoryLabel, categoryListView);

        VBox resultsPanel = new VBox(10);
        resultsPanel.setPadding(new Insets(10));
        Label resultsLabel = new Label("Activités");
        resultsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        resultsListView = new ListView<>();
        resultsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Activity item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });
        resultsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displayActivityDetails(newValue);
            }
        });
        resultsPanel.getChildren().addAll(resultsLabel, resultsListView);

        VBox detailsPanel = new VBox(10);
        detailsPanel.setPadding(new Insets(10));
        Label detailsLabel = new Label("Détails");
        detailsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsPanel.getChildren().addAll(detailsLabel, detailsArea);

        HBox controlsPanel = new HBox(10);
        controlsPanel.setPadding(new Insets(10));
        
        // Champ de saisie d'URL
        Label urlLabel = new Label("URL du site à scraper:");
        TextField urlField = new TextField();
        urlField.setPromptText("https://example.com");
        urlField.setPrefWidth(300);
        
        scrapeButton = new Button("Scraper Visit Paris Region");
        scrapeButton.setOnAction(e -> startScraping());
        
        Button customScrapeButton = new Button("Scraper Site Personnalisé");
        customScrapeButton.setOnAction(e -> startCustomScraping(urlField.getText()));
        customScrapeButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 8;");
        
        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(200);
        statusLabel = new Label("Prêt");
        controlsPanel.getChildren().addAll(urlLabel, urlField, scrapeButton, customScrapeButton, progressBar, statusLabel);

        currentSiteLabel = new Label("Aucun site scrappé");
        currentSiteLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4e8cff;");

        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(controlsPanel);
        setLeft(categoryPanel);
        setCenter(resultsPanel);
        setBottom(detailsPanel);

        Label titleLabel = new Label("Quartissimo");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4e8cff;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox menuBarWithTitle = new HBox();
        menuBarWithTitle.getChildren().addAll(menuBar, spacer, titleLabel);
        menuBarWithTitle.setAlignment(Pos.CENTER_LEFT);
        menuBarWithTitle.setPadding(new Insets(5, 10, 5, 0));

        VBox topBox = new VBox(menuBarWithTitle, controlsPanel, currentSiteLabel);
        setTop(topBox);

        loadActivitiesFromFile();
        loadThemes();
        applyDefaultTheme();

        resultsListView.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #4e8cff;");
        resultsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Activity item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item.getTitle());
                    Circle dot = new Circle(5, Color.web("#4e8cff"));
                    setGraphic(dot);
                    if (isSelected()) {
                        setStyle("-fx-background-color: #f4f8ff; -fx-padding: 8px; -fx-font-size: 13px; -fx-text-fill: #2566c7;");
                    } else {
                        setStyle("-fx-background-color: #f4f8ff; -fx-padding: 8px; -fx-font-size: 13px; -fx-text-fill: -fx-text-inner-color;");
                    }
                }
            }
        });

        categoryListView.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #4e8cff;");
        categoryListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (isSelected()) {
                        setStyle("-fx-background-color: #eaf2ff; -fx-padding: 6px; -fx-font-size: 12px; -fx-text-fill: #2566c7;");
                    } else {
                        setStyle("-fx-background-color: #eaf2ff; -fx-padding: 6px; -fx-font-size: 12px; -fx-text-fill: -fx-text-inner-color;");
                    }
                }
            }
        });

        scrapeButton.setStyle("-fx-background-color: #4e8cff; -fx-text-fill: white; -fx-background-radius: 8;");
        progressBar.setStyle("-fx-accent: #4e8cff;");
        detailsArea.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #4e8cff;");
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    private void loadThemes() {
        themesMenu.getItems().clear();
        MenuItem defaultItem = new MenuItem("Défaut");
        defaultItem.setOnAction(e -> applyDefaultTheme());
        themesMenu.getItems().add(defaultItem);
        File themesDir = new File("src/main/resources/themes");
        if (themesDir.exists() && themesDir.isDirectory()) {
            File[] files = themesDir.listFiles((dir, name) -> name.endsWith(".css"));
            if (files != null) {
                Arrays.sort(files);
                for (File file : files) {
                    if (file.getName().equals("default.css")) continue;
                    String themeName = file.getName().replace(".css", "");
                    MenuItem item = new MenuItem(themeName);
                    item.setOnAction(e -> applyTheme(file));
                    themesMenu.getItems().add(item);
                }
            }
        }
    }

    private void applyTheme(File cssFile) {
        if (scene == null) {
            scene = getScene();
        }
        if (scene != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssFile.toURI().toString());
            currentThemeFile = cssFile;
        }
    }

    private void applyDefaultTheme() {
        File defaultTheme = new File("src/main/resources/themes/default.css");
        if (scene == null) {
            scene = getScene();
        }
        if (scene != null && defaultTheme.exists()) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(defaultTheme.toURI().toString());
            currentThemeFile = defaultTheme;
        }
    }

    private void loadCategories() {
        try {
            File jsonFile = new File(System.getProperty("user.home") + "/.quartissimo/export.json");
            if (jsonFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                List<Activity> activities = mapper.readValue(jsonFile, new TypeReference<List<Activity>>() {});
                Set<String> uniqueCategories = new HashSet<>();
                for (Activity activity : activities) {
                    uniqueCategories.addAll(activity.getCategories());
                }
                ObservableList<String> categories = FXCollections.observableArrayList(
                        uniqueCategories.stream().sorted().collect(Collectors.toList()));
                categoryListView.setItems(categories);
            }
        } catch (Exception e) {
        }
    }

    private void loadActivitiesFromFile() {
        try {
            File jsonFile = new File(System.getProperty("user.home") + "/.quartissimo/export.json");
            if (jsonFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                allActivities = mapper.readValue(jsonFile, new TypeReference<List<Activity>>() {});
                filterActivities();
            }
        } catch (IOException e) {
            statusLabel.setText("Erreur lors du chargement des activités : " + e.getMessage());
        }
    }

    private void filterActivities() {
        ObservableList<Activity> filteredActivities;
        if (selectedCategories.isEmpty()) {
            filteredActivities = FXCollections.observableArrayList(allActivities);
        } else {
            List<Activity> filtered = allActivities.stream()
                    .filter(activity -> {
                        for (String category : activity.getCategories()) {
                            if (selectedCategories.contains(category)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            filteredActivities = FXCollections.observableArrayList(filtered);
        }
        resultsListView.setItems(filteredActivities);
        statusLabel.setText("Affichage de " + filteredActivities.size() + " activités");
    }

    private void displayActivityDetails(Activity activity) {
        StringBuilder details = new StringBuilder();
        details.append("Titre : ").append(activity.getTitle()).append("\n\n");
        if (!activity.getCategories().isEmpty()) {
            details.append("Catégories : ").append(String.join(", ", activity.getCategories())).append("\n\n");
        }
        if (!activity.getShortDescription().isEmpty()) {
            details.append("Description : ").append(activity.getShortDescription()).append("\n\n");
        }
        if (!activity.getAddress().isEmpty() || !activity.getZipcode().isEmpty() || !activity.getCity().isEmpty()) {
            details.append("Lieu : ");
            if (!activity.getAddress().isEmpty()) {
                details.append(activity.getAddress()).append(", ");
            }
            if (!activity.getZipcode().isEmpty()) {
                details.append(activity.getZipcode()).append(" ");
            }
            if (!activity.getCity().isEmpty()) {
                details.append(activity.getCity());
            }
            details.append("\n\n");
        }
        if (!activity.getPhoneNumber().isEmpty()) {
            details.append("Téléphone : ").append(activity.getPhoneNumber()).append("\n\n");
        }
        if (!activity.getWebSite().isEmpty()) {
            details.append("Site web : ").append(activity.getWebSite()).append("\n\n");
        }
        if (!activity.getAvailabilities().isEmpty()) {
            details.append("Horaires : ").append(activity.getAvailabilities()).append("\n\n");
        }
        if (!activity.getPrices().isEmpty()) {
            details.append("Tarifs : ").append(activity.getPrices());
        }
        detailsArea.setText(details.toString());
    }

    private void startScraping() {
        scrapeButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1);
        statusLabel.setText("Scrapping en cours...");
        updateCurrentSite("Visit Paris Region");
        executorService.submit(() -> {
            try {
                VisitParisRegionScraper scraper = new VisitParisRegionScraper();
                scraper.setOnSiteScrapedCallback(this::updateCurrentSite);
                if (!selectedCategories.isEmpty()) {
                    scraper.setCategoriesToScrape(new ArrayList<>(selectedCategories));
                }
                ScraperLauncher scraperLauncher = new ScraperLauncher(scraper);
                scraperLauncher.launchScrapers();
                Platform.runLater(() -> {
                    loadActivitiesFromFile();
                    loadCategories();
                    loadThemes();
                    scrapeButton.setDisable(false);
                    progressBar.setVisible(false);
                    int nbSites = allActivities.size();
                    statusLabel.setText("Scrapping terminé : " + nbSites + " sites ont été scrappés");
                    updateCurrentSite("tout les sites on été scrappé");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    scrapeButton.setDisable(false);
                    progressBar.setVisible(false);
                    statusLabel.setText("Erreur lors du scrapping : " + e.getMessage());
                    updateCurrentSite("Aucun site scrappé");
                });
            }
        });
    }

    private void startCustomScraping(String url) {
        if (url == null || url.trim().isEmpty()) {
            statusLabel.setText("Veuillez saisir une URL valide");
            return;
        }
        
        // Validation basique de l'URL
        final String finalUrl;
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            finalUrl = "https://" + url;
        } else {
            finalUrl = url;
        }
        
        scrapeButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1);
        statusLabel.setText("Scrapping en cours...");
        updateCurrentSite("Site personnalisé: " + finalUrl);
        
        executorService.submit(() -> {
            try {
                GenericScraper scraper = new GenericScraper(finalUrl);
                ScraperLauncher scraperLauncher = new ScraperLauncher(scraper);
                scraperLauncher.launchScrapers();
                
                // Récupérer les résultats
                List<Activity> newActivities = scraper.getActivities();
                allActivities.addAll(newActivities);
                
                Platform.runLater(() -> {
                    loadActivitiesFromFile();
                    loadCategories();
                    loadThemes();
                    scrapeButton.setDisable(false);
                    progressBar.setVisible(false);
                    statusLabel.setText("Scrapping terminé : " + newActivities.size() + " activité(s) trouvée(s)");
                    updateCurrentSite("Site personnalisé scrapé avec succès");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    scrapeButton.setDisable(false);
                    progressBar.setVisible(false);
                    statusLabel.setText("Erreur lors du scrapping : " + e.getMessage());
                    updateCurrentSite("Aucun site scrappé");
                });
            }
        });
    }

    private void updateCurrentSite(String site) {
        Platform.runLater(() -> currentSiteLabel.setText("Scrapping : " + site));
    }
}
