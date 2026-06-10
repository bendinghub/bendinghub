package me.unprankable.bendinghub;

import me.unprankable.bendinghub.chat.ChatManager;
import me.unprankable.bendinghub.hooks.DiscordSRVChatListenerHook;
import me.unprankable.bendinghub.hooks.TownyChatHook;
import me.unprankable.bendinghub.placeholderapi.BendinghubExpansion;
import me.unprankable.bendinghub.tab.TabListener;
import me.unprankable.bendinghub.tab.TabManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.swing.event.TableModelListener;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Bendinghub extends JavaPlugin {
    public static Bendinghub plugin;
    public static Logger log;
    public static ConfigManager configManager;
    public static ChatManager chatManager;
    public static TabManager tabManager;
    public static commandExecutor commandExecutor;
    public static boolean luckpermsEnabled;
    public static StorageManager storageManager;

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(new TabListener(), this);
        Bendinghub.log = this.getLogger();
        configManager = new ConfigManager();
        TownyChatHook townyChatHook = new TownyChatHook();
        townyChatHook.register(this);
        configManager.load();
        commandExecutor.loadCommands();
        // Initialize storage manager before chat manager so chat can use persistent storage
        storageManager = new StorageManager(this.getLogger());
        try {
            storageManager.init();
        } catch (SQLException e) {
            Bendinghub.log.severe("Failed to initialize StorageManager (SQLite). Chat persistence and other features may not work.");
            Bendinghub.log.log(Level.SEVERE, "StorageManager initialization error", e);
        }
        // Initialize chat manager after config is loaded (ChannelManager reads config)
        if (configManager.isChatEnabled()) {
            chatManager = new ChatManager();
            Bendinghub.log.info("Chat feature is enabled; ChatManager initialized.");
        } else {
            Bendinghub.log.info("Chat feature is disabled in config; skipping ChatManager initialization.");
        }

        tabManager = new TabManager();
        TabManager.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            tabManager.updateAllPlayers();
        },0L , configManager.getConfig().getInt("tab.updateIntervalTicks"));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new BendinghubExpansion().register();
            Bendinghub.log.info("Registered PlaceholderAPI expansion: Bendinghub");
        }


        // Register commands
        commandExecutor = new commandExecutor();
        luckpermsEnabled = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
        Bendinghub.log.info("Enabling Bendinghub...");
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV") && configManager.isChatEnabled()) {
            DiscordSRV.api.subscribe(new DiscordSRVChatListenerHook());
        }
    }

    @Override
    public void onDisable() {
        // Persist chat data if the chat manager is enabled
        if (chatManager != null && configManager != null && configManager.isChatEnabled()) {
            try {
                chatManager.getChannelManager().savePlayerChannels();
            } catch (Exception e) {
                Bendinghub.log.log(Level.WARNING, "Failed to save player channels on shutdown", e);
            }
            try {
                chatManager.getChatColorManager().savePlayerChatColors();
            } catch (Exception e) {
                Bendinghub.log.log(Level.WARNING, "Failed to save player chat colors on shutdown", e);
            }
        }

        Scoreboard scoreboard = getServer().getScoreboardManager().getMainScoreboard();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Team team = scoreboard.getTeam(player.getName());
            if (team != null) {
                team.unregister();
            }
        }
        TabManager.task.cancel();

        // Close persistent storage
        if (storageManager != null) storageManager.close();
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
