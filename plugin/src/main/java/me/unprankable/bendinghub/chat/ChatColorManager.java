package me.unprankable.bendinghub.chat;

import me.unprankable.bendinghub.Bendinghub;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatColorManager {
    private final ConcurrentHashMap<UUID, String> playerChatColors;
    // persistence is now handled by StorageManager (data.db)

    public ChatColorManager() {
        this.playerChatColors = new ConcurrentHashMap<>();
        loadPlayerChatColors();
    }

    public void loadPlayerChatColors() {
        playerChatColors.clear();
        if (Bendinghub.storageManager != null && Bendinghub.storageManager.isConnected()) {
            try {
                Map<UUID, String> loaded = Bendinghub.storageManager.loadAllPlayerChatColors();
                playerChatColors.putAll(loaded);
                Bendinghub.log.info("Loaded " + playerChatColors.size() + " player chat colors from data.db.");
            } catch (SQLException e) {
                Bendinghub.log.severe("Failed to load player chat colors from data.db: " + e.getMessage());
            }
        } else {
            Bendinghub.log.info("StorageManager not available; starting with an empty chat color map.");
        }
    }

    public void savePlayerChatColors() {
        if (Bendinghub.storageManager == null || !Bendinghub.storageManager.isConnected()) {
            Bendinghub.log.severe("StorageManager not available; cannot save player chat colors to data.db.");
            return;
        }
        int saved = 0;
        for (Map.Entry<UUID, String> entry : playerChatColors.entrySet()) {
            try {
                Bendinghub.storageManager.setPlayerChatColor(entry.getKey(), entry.getValue());
                saved++;
            } catch (SQLException e) {
                Bendinghub.log.severe("Failed to save chat color for " + entry.getKey() + ": " + e.getMessage());
            }
        }
        Bendinghub.log.info("Saved " + saved + " player chat colors to data.db.");
    }

    public String getPlayerChatColor(UUID playerId) {
        return playerChatColors.getOrDefault(playerId, "<white>");
    }

    public void setPlayerChatColor(UUID playerId, String colorCode) {
        if (colorCode == null || colorCode.isEmpty()) {
            playerChatColors.put(playerId, "<white>");
            try {
                if (Bendinghub.storageManager != null && Bendinghub.storageManager.isConnected()) {
                    Bendinghub.storageManager.setPlayerChatColor(playerId, "<white>");
                }
            } catch (SQLException e) {
                Bendinghub.log.severe("Failed to persist chat color for " + playerId + ": " + e.getMessage());
            }
        } else {
            playerChatColors.put(playerId, colorCode);
        }
        try {
            if (Bendinghub.storageManager != null && Bendinghub.storageManager.isConnected()) {
                Bendinghub.storageManager.setPlayerChatColor(playerId, playerChatColors.get(playerId));
            }
        } catch (SQLException e) {
            Bendinghub.log.severe("Failed to persist chat color for " + playerId + ": " + e.getMessage());
        }
    }

}

