package me.unprankable.bendinghub;

import me.unprankable.bendinghub.chat.ChatManager;
import me.unprankable.bendinghub.placeholder.BendinghubExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Bendinghub extends JavaPlugin {
    public static Bendinghub plugin;
    public static Logger log;
    public static ConfigManager configManager;
    public static ChatManager chatManager;

    @Override
    public void onEnable() {
        plugin = this;
        Bendinghub.log = this.getLogger();
        configManager = new ConfigManager(plugin);
        configManager.load();
        // Initialize chat manager after config is loaded (ChannelManager reads config)
        chatManager = new ChatManager(plugin);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new BendinghubExpansion(this).register();
            Bendinghub.log.info("Registered PlaceholderAPI expansion: %chatcolor%");
        }
        // Register admin commands
        this.getCommand("bendinghub").setExecutor(new commandExecutor(this));
        Bendinghub.log.info("Enabling Bendinghub...");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void debug(String message) {
        if (configManager != null && configManager.getConfig().getBoolean("debug")) {
            log.info("[DEBUG] " + message);
        }
    }
}
