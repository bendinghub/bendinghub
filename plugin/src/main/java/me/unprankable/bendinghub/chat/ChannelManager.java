package me.unprankable.bendinghub.chat;

import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {
    private final ConcurrentHashMap<UUID, String> playerChannels;
    private final ConcurrentHashMap<String, ChatChannel> registeredChannels;
    private File playerChannelsFile;
    private FileConfiguration playerChannelsConfig;

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
        playerChannelsFile = new File(Bendinghub.plugin.getDataFolder(), "playerChannels.yml");
        playerChannels.clear();
        if (playerChannelsFile.exists()) {
            playerChannelsConfig = YamlConfiguration.loadConfiguration(playerChannelsFile);

            // Load each player's channel
            for (String key : playerChannelsConfig.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String channelId = playerChannelsConfig.getString(key);
                    if (registeredChannels.containsKey(channelId)) {
                        playerChannels.put(uuid, channelId);
                    }
                } catch (IllegalArgumentException e) {
                    Bendinghub.log.warning("Invalid UUID in playerChannels.yml: " + key);
                }
            }
        } else {
            playerChannelsConfig = new YamlConfiguration();
        }
    }

    public void savePlayerChannels() {
        try {
            playerChannelsConfig = new YamlConfiguration();
            for (UUID uuid : playerChannels.keySet()) {
                playerChannelsConfig.set(uuid.toString(), playerChannels.get(uuid));
            }
            playerChannelsConfig.save(playerChannelsFile);
        } catch (IOException exception) {
            Bendinghub.log.severe("Failed to save playerChannels.yml: " + exception.getMessage());
        }
    }

    public ChatChannel getPlayerChannel(UUID uuid) {
        String channelId = playerChannels.getOrDefault(uuid, "global");
        return registeredChannels.get(channelId);
    }

    public void setPlayerChannel(UUID uuid, String channelId) {
        if (registeredChannels.containsKey(channelId)) {
            playerChannels.put(uuid, channelId);
        }
        savePlayerChannels();
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
