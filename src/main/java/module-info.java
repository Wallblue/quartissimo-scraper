module org.quartissimo.scrapapp {
    uses org.quartissimo.pluginapi.Plugin;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.support;
    requires io.github.bonigarcia.webdrivermanager;
    requires org.seleniumhq.selenium.api;

    opens org.quartissimo.scrapapp to javafx.fxml;
    exports org.quartissimo.scrapapp;
    exports org.quartissimo.scrapapp.ui;
    exports org.quartissimo.scrapapp.ui.theme;
    exports org.quartissimo.scrapapp.scraper;
    requires dev.failsafe.core;
    requires org.apache.commons.io;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires org.quartissimo.pluginapi;
    exports org.quartissimo.scrapapp.scraper.models to com.fasterxml.jackson.databind;
    opens org.quartissimo.scrapapp.ui to javafx.fxml;
}
