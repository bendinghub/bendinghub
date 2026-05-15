package me.unprankable.bendinghub;

import me.unprankable.bendinghub.chat.ChatManager;
import me.unprankable.bendinghub.hooks.TownyChatHook;
import me.unprankable.bendinghub.placeholderapi.BendinghubExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Bendinghub extends JavaPlugin {
    public static Bendinghub plugin;
    public static Logger log;
    public static ConfigManager configManager;
    public static ChatManager chatManager;
    public static commandExecutor commandExecutor;

    @Override
    public void onEnable() {
        plugin = this;
        Bendinghub.log = this.getLogger();
        configManager = new ConfigManager();
        TownyChatHook townyChatHook = new TownyChatHook();
        townyChatHook.register(this);
        configManager.load();
        // Initialize chat manager after config is loaded (ChannelManager reads config)
        chatManager = new ChatManager();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new BendinghubExpansion().register();
            Bendinghub.log.info("Registered PlaceholderAPI expansion: Bendinghub");
        }
        // Register admin commands
        commandExecutor = new commandExecutor();
        Bendinghub.log.info("Enabling Bendinghub...");
    }

    @Override
    public void onDisable() {
        if (chatManager != null) {
            chatManager.shutdown();
        }
    }
    public static void reloadPlugin() {
        if (configManager != null) {
            configManager.reload();
        }
        if (chatManager != null) {
            chatManager.getChannelManager().loadPlayerChannels();
            chatManager.getChatColorManager().loadPlayerChatColors();
        }
    }
    public static void debug(String message) {
        if (configManager != null && configManager.getConfig().getBoolean("debug")) {
            log.info("[DEBUG] " + message);
        }
    }
}
