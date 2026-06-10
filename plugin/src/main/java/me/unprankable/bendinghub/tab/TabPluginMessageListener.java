package me.unprankable.bendinghub.tab;

import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import java.util.UUID;

public class TabPluginMessageListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        Bendinghub.debug("[TabDebug] Received plugin message on channel: " + channel);

        if (!channel.equals(PlayerDataObject.BENDINGHUB_TAB)) {
            return;
        }

        PlayerDataObject data;
        try {
            data = PlayerDataObject.deserialize(message);
            if (data == null) return;
            Bendinghub.debug("[TabDebug] Successfully deserialized data: " + data.toString());
        } catch (Exception e) {
            Bendinghub.debug("[TabDebug] ERROR: Failed to deserialize incoming payload: " + e.getMessage());
            return;
        }

        String serverId = Bendinghub.configManager.getConfig().getString("chat.proxy.server-id", "server-1");
        Bendinghub.debug("[TabDebug] Current Server ID: " + serverId + " | Incoming Packet Server ID: " + data.getServerName());

        if (serverId.equalsIgnoreCase(data.getServerName())) {
            Bendinghub.debug("[TabDebug] Dropping packet: Echo filter matched sender identity (" + serverId + ").");
            return;
        }

        try {
            String subAction = data.getAction();
            UUID playerUuid = UUID.fromString(data.getPlayerUUID());
            String playerName = data.getPlayerName();
            String formatStr = data.getPlayerTabFormat();

            Bendinghub.debug("[TabDebug] Processing action: " + subAction + " for player: " + playerName);

            switch(subAction) {
                case "JOIN", "UPDATE":
                    Bendinghub.debug("[TabDebug] Calling fakePlayer.updateNetworkPlayerSlot...");
                    Bendinghub.tabManager.fakePlayer.updateNetworkPlayerSlot(playerUuid, playerName, formatStr);
                    break;
                case "QUIT":
                    Bendinghub.debug("[TabDebug] Calling fakePlayer.removeNetworkPlayerSlot...");
                    Bendinghub.tabManager.fakePlayer.removeNetworkPlayerSlot(playerUuid, playerName);
                    break;
                default:
                    Bendinghub.debug("[TabDebug] Action not recognized: " + subAction);
            }

        } catch (Exception e) {
            Bendinghub.debug("[TabDebug] ERROR: Exception processing tab payload stream: " + e.getMessage());
        }
    }
}