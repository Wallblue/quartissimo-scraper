package org.quartissimo.scrapapp;

import org.quartissimo.pluginapi.NeedingResourcePlugin;
import org.quartissimo.pluginapi.Plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PluginLoader {

    private final List<Plugin> loadedPlugins = new ArrayList<>();

    public List<Plugin> loadPluginsFrom() {
        String os = System.getProperty("os.name").toLowerCase();
        String baseDir;
        final String pluginDirName = "/quartissimo/plugins";

        if (os.contains("win")) {
            baseDir = System.getenv("APPDATA");
        } else if (os.contains("mac")) {
            baseDir = System.getProperty("user.home") + "/Library/Application Support";
        } else {
            baseDir = System.getProperty("user.home") + "/.local/share";
        }

        return loadPluginsFrom(baseDir + pluginDirName);
    }

    public List<Plugin> loadPluginsFrom(String pluginDirPath) {
        File pluginDir = new File(pluginDirPath);
        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            boolean created = pluginDir.mkdirs();
            if (created) {
                System.out.println("Dossier créé : " + pluginDir.getAbsolutePath());
            } else {
                System.out.println("Échec de la création du dossier : " + pluginDir.getAbsolutePath());
            }
            return loadedPlugins;
        }

        File[] jarFiles = pluginDir.listFiles((_, name) -> name.endsWith(".jar"));
        if (jarFiles == null) return loadedPlugins;

        for (File jar : jarFiles) {
            try {
                URLClassLoader loader = new URLClassLoader(
                        new URL[]{jar.toURI().toURL()},
                        this.getClass().getClassLoader()
                );

                ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class, loader);
                Path resourcesFolder = Paths.get(System.getProperty("user.home"), ".quartissimo");
                for (Plugin plugin : plugins) {
                    if (plugin instanceof NeedingResourcePlugin) {
                        ((NeedingResourcePlugin) plugin).setResourcesFolderPath(resourcesFolder);
                    }

                    System.out.println("Plugin chargé : " + plugin.getName());
                    loadedPlugins.add(plugin);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de " + jar.getName());
                e.printStackTrace();
            }
        }

        return loadedPlugins;
    }

    public List<Plugin> getLoadedPlugins() {
        return loadedPlugins;
    }
}