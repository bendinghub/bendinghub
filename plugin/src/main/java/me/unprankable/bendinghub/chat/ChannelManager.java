package me.unprankable.bendinghub.chat;

import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.Map;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {
    private final ConcurrentHashMap<UUID, String> playerChannels;
    private final ConcurrentHashMap<String, ChatChannel> registeredChannels;
    // persistence moved to StorageManager (data.db)

    public ChannelManager() {
        this.playerChannels = new ConcurrentHashMap<>();
        this.registeredChannels = new ConcurrentHashMap<>();

        ConfigurationSection channelsSection = Bendinghub.configManager.getChannels();
        if (channelsSection != null) {
            for (String channelId : channelsSection.getKeys(false)) {
                String prefix = channelsSection.getString(channelId + ".prefix", "");
                String permission = channelsSection.getString(channelId + ".permission", "");
                String format = channelsSection.getString(channelId + ".format", "<prefix><displayname>: <message>");
                // Apply global placeholders from config.chat.placeholders (e.g. "<rank>" -> "%luckperms_prefix%")
                try {
                    FileConfiguration cfg = Bendinghub.configManager.getConfig();
                    ConfigurationSection placeholders = cfg.getConfigurationSection("chat.placeholders");
                    if (placeholders != null) {
                        for (String phKey : placeholders.getKeys(false)) {
                            String phValue = placeholders.getString(phKey);
                            if (phValue != null) {
                                format = format.replace(phKey, phValue);
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // If config isn't available for some reason, continue with raw format
                }
                double radius = channelsSection.getDouble(channelId + ".radius", 0);
                ChatChannel channel = new ChatChannel(channelId, prefix, permission, format, radius);
                registeredChannels.put(channelId, channel);
            }
        }

        // Now that channels are registered, load saved player channel selections
        loadPlayerChannels();
    }

    public void loadPlayerChannels() {
        playerChannels.clear();
        if (Bendinghub.storageManager != null && Bendinghub.storageManager.isConnected()) {
            try {
                Map<UUID, String> loaded = Bendinghub.storageManager.loadAllPlayerChannels();
                for (Map.Entry<UUID, String> e : loaded.entrySet()) {
                    UUID uuid = e.getKey();
                            String channelId = e.getValue();
                            if (channelId != null && registeredChannels.containsKey(channelId)) {
                                playerChannels.put(uuid, channelId);
                            } else {
                                playerChannels.put(uuid, "global");
                            }
                }
                Bendinghub.log.info("Loaded " + playerChannels.size() + " player channels from data.db.");
            } catch (SQLException ex) {
                Bendinghub.log.severe("Failed to load player channels from data.db: " + ex.getMessage());
            }
        } else {
            Bendinghub.log.info("StorageManager not available; starting with empty player channel assignments.");
        }
    }

    public void savePlayerChannels() {
        if (Bendinghub.storageManager == null || !Bendinghub.storageManager.isConnected()) {
            Bendinghub.log.severe("StorageManager not available; cannot save player channels to data.db.");
            return;
        }
        int saved = 0;
        for (Map.Entry<UUID, String> entry : playerChannels.entrySet()) {
            try {
                Bendinghub.storageManager.setPlayerChannel(entry.getKey(), entry.getValue());
                saved++;
            } catch (SQLException ex) {
                Bendinghub.log.severe("Failed to save player channel for " + entry.getKey() + ": " + ex.getMessage());
            }
        }
        Bendinghub.log.info("Saved " + saved + " player channels to data.db.");
    }

    public ChatChannel getPlayerChannel(UUID uuid) {
        String channelId = playerChannels.getOrDefault(uuid, "global");
        return registeredChannels.get(channelId);
    }

    public void setPlayerChannel(UUID uuid, String channelId) {
        if (registeredChannels.containsKey(channelId)) {
            playerChannels.put(uuid, channelId);
        }
        // Persist single change
        if (Bendinghub.storageManager != null && Bendinghub.storageManager.isConnected()) {
            try {
                Bendinghub.storageManager.setPlayerChannel(uuid, playerChannels.getOrDefault(uuid, "global"));
            } catch (SQLException ex) {
                Bendinghub.log.severe("Failed to persist player channel for " + uuid + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Reload channels from config and fix player assignments.
     */
    public void reloadChannels() {
        registeredChannels.clear();

        ConfigurationSection channelsSection = Bendinghub.configManager.getChannels();
        if (channelsSection != null) {
            for (String channelId : channelsSection.getKeys(false)) {
                String prefix = channelsSection.getString(channelId + ".prefix", "");
                String permission = channelsSection.getString(channelId + ".permission", "");
                String format = channelsSection.getString(channelId + ".format", "<prefix><displayname>: <message>");
                double radius = channelsSection.getDouble(channelId + ".radius", 0);

                // Apply placeholders from config.chat.placeholders
                try {
                    FileConfiguration cfg = Bendinghub.configManager.getConfig();
                    ConfigurationSection placeholders = cfg.getConfigurationSection("chat.placeholders");
                    if (placeholders != null) {
                        for (String phKey : placeholders.getKeys(false)) {
                            String phValue = placeholders.getString(phKey);
                            if (phValue != null) {
                                format = format.replace(phKey, phValue);
                            }
                        }
                    }
                } catch (Exception ignored) {}

                ChatChannel channel = new ChatChannel(channelId, prefix, permission, format, radius);
                registeredChannels.put(channelId, channel);
            }
        }

        // Ensure players referencing missing channels are moved to global
        for (UUID uuid : playerChannels.keySet()) {
            String ch = playerChannels.get(uuid);
            if (ch == null || !registeredChannels.containsKey(ch)) {
                playerChannels.put(uuid, "global");
            }
        }

        // Persist any changes
        savePlayerChannels();
    }

    public Collection<ChatChannel> getChannels() {
        return registeredChannels.values();
    }

    public ChatChannel getChannelById(String channelId) {
        if (channelId == null) {
            return null;
        }
        return registeredChannels.get(channelId);
    }
}
