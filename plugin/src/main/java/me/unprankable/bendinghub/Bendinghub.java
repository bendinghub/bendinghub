package me.unprankable.bendinghub;

import me.unprankable.bendinghub.chat.ChatManager;
import me.unprankable.bendinghub.hooks.DiscordSRVChatListenerHook;
import me.unprankable.bendinghub.hooks.TownyChatHook;
import me.unprankable.bendinghub.placeholderapi.BendinghubExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import github.scarsz.discordsrv.DiscordSRV;

import java.util.logging.Logger;

public final class Bendinghub extends JavaPlugin {
    public static Bendinghub plugin;
    public static Logger log;
    public static ConfigManager configManager;
    public static ChatManager chatManager;
    public static commandExecutor commandExecutor;
    public static boolean luckpermsEnabled;

    @Override
    public void onEnable() {
        plugin = this;
        Bendinghub.log = this.getLogger();
        configManager = new ConfigManager();
        TownyChatHook townyChatHook = new TownyChatHook();
        townyChatHook.register(this);
        configManager.load();
        // Initialize chat manager after config is loaded (ChannelManager reads config)
        if (configManager.isChatEnabled()) {
            chatManager = new ChatManager();
            Bendinghub.log.info("Chat feature is enabled; ChatManager initialized.");
        } else {
            Bendinghub.log.info("Chat feature is disabled in config; skipping ChatManager initialization.");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new BendinghubExpansion().register();
            Bendinghub.log.info("Registered PlaceholderAPI expansion: Bendinghub");
        }
        // Register admin commands
        commandExecutor = new commandExecutor();
        luckpermsEnabled = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
        Bendinghub.log.info("Enabling Bendinghub...");
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV") && configManager.isChatEnabled()) {
            DiscordSRV.api.subscribe(new DiscordSRVChatListenerHook());
        }
    }

    @Override
    public void onDisable() {

    }
    public static void reloadPlugin() {
        if (configManager != null) {
            configManager.reload();
        }
        if (chatManager != null && configManager != null && configManager.isChatEnabled()) {
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
