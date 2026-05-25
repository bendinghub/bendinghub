package me.unprankable.bendinghub.chat;

import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatColorManager {
    private final ConcurrentHashMap<UUID, String> playerChatColors;
    private File playerChatColorsFile;
    private FileConfiguration playerChatColorsConfig;

    public ChatColorManager() {
        this.playerChatColors = new ConcurrentHashMap<>();
        loadPlayerChatColors();
    }

    public void loadPlayerChatColors() {
        playerChatColorsFile = new File(Bendinghub.plugin.getDataFolder(), "playerChatColors.yml");
        playerChatColors.clear();

        if (playerChatColorsFile.exists()) {
            playerChatColorsConfig = YamlConfiguration.loadConfiguration(playerChatColorsFile);
            for (String key : playerChatColorsConfig.getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(key);
                    String colorCode = playerChatColorsConfig.getString(key, "");
                    if (!colorCode.isEmpty()) {
                        playerChatColors.put(playerId, colorCode);
                    }
                } catch (IllegalArgumentException e) {
                    Bendinghub.log.warning("Invalid UUID in playerChatColors.yml: " + key);
                }
            }
            Bendinghub.log.info("Loaded " + playerChatColors.size() + " player chat colors.");
        } else {
            Bendinghub.log.info("No existing playerChatColors.yml found. Starting with an empty chat color map.");
        }
    }

    public void savePlayerChatColors() {
        if (playerChatColorsConfig == null) {
            playerChatColorsConfig = new YamlConfiguration();
        }
        for (Map.Entry<UUID, String> entry : playerChatColors.entrySet()) {
            playerChatColorsConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            playerChatColorsConfig.save(playerChatColorsFile);
            Bendinghub.log.info("Saved " + playerChatColors.size() + " player chat colors.");
        } catch (IOException e) {
            Bendinghub.log.severe("Failed to save playerChatColors.yml: " + e.getMessage());
        }
    }

    public String getPlayerChatColor(UUID playerId) {
        return playerChatColors.getOrDefault(playerId, "<white>");
    }

    public void setPlayerChatColor(UUID playerId, String colorCode) {
        if (colorCode == null || colorCode.isEmpty()) {
            playerChatColors.put(playerId, "<white>");
            savePlayerChatColors();
        } else {
            playerChatColors.put(playerId, colorCode);
        }
        savePlayerChatColors();
    }

}

