package me.unprankable.bendinghub;

import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {
    private final Bendinghub plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(final Bendinghub plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            try {
                plugin.saveResource("config.yml", false);
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Bundled config.yml was not found. Creating a minimal config file.");
                try {
                    configFile.createNewFile();
                } catch (IOException ioException) {
                    plugin.getLogger().severe("Failed to create config.yml: " + ioException.getMessage());
                }
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        config.addDefault("debug", false);
        config.options().copyDefaults(true);

        try {
            config.save(configFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save config.yml defaults: " + exception.getMessage());
        }

        Bendinghub.debug("Config loaded");
    }

    public void reload(){
        load();
        Bendinghub.log.info("Config reloaded");
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public ConfigurationSection getChannels() {
        return config.getConfigurationSection("chat.channels");
    }
}
