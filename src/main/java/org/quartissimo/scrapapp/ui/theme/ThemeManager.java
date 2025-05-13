package org.quartissimo.scrapapp.ui.theme;

import javafx.scene.Scene;
import java.io.File;

public class ThemeManager {
    public static void applyTheme(Scene scene, File cssFile) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(cssFile.toURI().toString());
    }
}
