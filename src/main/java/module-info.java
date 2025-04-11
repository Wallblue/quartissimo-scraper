module com.quartissimo.scrapapp.ui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.seleniumhq.selenium.chrome_driver;

    opens org.quartissimo.scrapapp to javafx.fxml;
    exports org.quartissimo.scrapapp;
}