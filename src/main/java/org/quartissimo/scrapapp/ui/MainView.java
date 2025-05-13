package org.quartissimo.scrapapp.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartissimo.scrapapp.scraper.ScraperLauncher;
import org.quartissimo.scrapapp.scraper.VisitParisRegionScraper;
import org.quartissimo.scrapapp.scraper.models.Activity;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MainView extends BorderPane {
    private final ListView<String> categoryListView;
    private final ListView<Activity> resultsListView;
    private final TextArea detailsArea;
    private final Button scrapeButton;
    private final Button scrapeAllButton;
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private final ExecutorService executorService;
    private Set<String> selectedCategories = new HashSet<>();
    private List<Activity> allActivities = new ArrayList<>();
    private Menu themesMenu;
    private Scene scene;
    private File currentThemeFile;

    public MainView() {
        executorService = Executors.newSingleThreadExecutor();

        MenuBar menuBar = new MenuBar();
        themesMenu = new Menu("Thèmes");
        menuBar.getMenus().add(themesMenu);
        setTop(menuBar);

        VBox categoryPanel = new VBox(10);
        categoryPanel.setPadding(new Insets(10));
        Label categoryLabel = new Label("Filter by Categories");
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
        Label resultsLabel = new Label("Activities");
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
        Label detailsLabel = new Label("Details");
        detailsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsPanel.getChildren().addAll(detailsLabel, detailsArea);

        HBox controlsPanel = new HBox(10);
        controlsPanel.setPadding(new Insets(10));
        scrapeButton = new Button("Scrape Visit Paris Region");
        scrapeButton.setOnAction(e -> startScraping());
        scrapeAllButton = new Button("Scrape toutes les catégories");
        scrapeAllButton.setOnAction(e -> startScrapingAll());
        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(200);
        statusLabel = new Label("Ready");
        controlsPanel.getChildren().addAll(scrapeButton, scrapeAllButton, progressBar, statusLabel);

        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(controlsPanel);
        setLeft(categoryPanel);
        setCenter(resultsPanel);
        setBottom(detailsPanel);
        setTop(new VBox(menuBar, controlsPanel));

        loadActivitiesFromFile();
        loadThemes();
        applyDefaultTheme();
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
            File jsonFile = new File("src/main/resources/export.json");
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
            File jsonFile = new File("src/main/resources/export.json");
            if (jsonFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                allActivities = mapper.readValue(jsonFile, new TypeReference<List<Activity>>() {});
                filterActivities();
            }
        } catch (IOException e) {
            statusLabel.setText("Error loading activities: " + e.getMessage());
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
        statusLabel.setText("Showing " + filteredActivities.size() + " activities");
    }

    private void displayActivityDetails(Activity activity) {
        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(activity.getTitle()).append("\n\n");
        if (!activity.getCategories().isEmpty()) {
            details.append("Categories: ").append(String.join(", ", activity.getCategories())).append("\n\n");
        }
        if (!activity.getShortDescription().isEmpty()) {
            details.append("Description: ").append(activity.getShortDescription()).append("\n\n");
        }
        if (!activity.getAddress().isEmpty() || !activity.getZipcode().isEmpty() || !activity.getCity().isEmpty()) {
            details.append("Location: ");
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
            details.append("Phone: ").append(activity.getPhoneNumber()).append("\n\n");
        }
        if (!activity.getWebSite().isEmpty()) {
            details.append("Website: ").append(activity.getWebSite()).append("\n\n");
        }
        if (!activity.getAvailabilities().isEmpty()) {
            details.append("Availabilities: ").append(activity.getAvailabilities()).append("\n\n");
        }
        if (!activity.getPrices().isEmpty()) {
            details.append("Prices: ").append(activity.getPrices());
        }
        detailsArea.setText(details.toString());
    }

    private void startScraping() {
        scrapeButton.setDisable(true);
        scrapeAllButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1);
        statusLabel.setText("Scraping in progress...");
        executorService.submit(() -> {
            try {
                VisitParisRegionScraper scraper = new VisitParisRegionScraper();
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
                    scrapeAllButton.setDisable(false);
                    progressBar.setVisible(false);
                    statusLabel.setText("Scraping completed successfully");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    scrapeButton.setDisable(false);
                    scrapeAllButton.setDisable(false);
                    progressBar.setVisible(false);
                    statusLabel.setText("Error during scraping: " + e.getMessage());
                });
            }
        });
    }

    private void startScrapingAll() {
        scrapeButton.setDisable(true);
        scrapeAllButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1);
        statusLabel.setText("Scraping toutes les catégories...");
        executorService.submit(() -> {
            try {
                VisitParisRegionScraper scraper = new VisitParisRegionScraper();
                scraper.setCategoriesToScrape(Collections.emptyList());
                ScraperLauncher scraperLauncher = new ScraperLauncher(scraper);
                scraperLauncher.launchScrapers();
                Platform.runLater(() -> {
                    loadActivitiesFromFile();
                    loadCategories();
                    loadThemes();
                    scrapeButton.setDisable(false);
                    scrapeAllButton.setDisable(false);
                    progressBar.setVisible(false);
                    statusLabel.setText("Scraping completed successfully");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    scrapeButton.setDisable(false);
                    scrapeAllButton.setDisable(false);
                    progressBar.setVisible(false);
                    statusLabel.setText("Error during scraping: " + e.getMessage());
                });
            }
        });
    }
}
