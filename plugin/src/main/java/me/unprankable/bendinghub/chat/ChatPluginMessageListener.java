package me.unprankable.bendinghub.chat;

import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class ChatPluginMessageListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (Bendinghub.chatManager == null || Bendinghub.configManager == null || !Bendinghub.configManager.isChatEnabled()) {
            return;
        }

        MessageDataObject data = MessageDataObject.deserialize(message);
        if (data == null) {
            return;
        }

        String serverId = Bendinghub.configManager.getConfig().getString("chat.proxy.server-id", "server-1");
        if (serverId.equalsIgnoreCase(data.getServerName())) {
            Bendinghub.debug("ChatPluginMessageListener: ignoring echo for server-id " + serverId);
            return;
        }

        ChatChannel chatChannel = Bendinghub.chatManager.getChannelManager().getChannelById(data.getChannelId());
        if (chatChannel == null) {
            Bendinghub.debug("ChatPluginMessageListener: unknown channel '" + data.getChannelId() + "', dropping payload");
            return;
        }

        if (!isForwardedChannel(chatChannel.getId())) {
            Bendinghub.debug("ChatPluginMessageListener: channel '" + chatChannel.getId() + "' is not configured for proxy forwarding");
            return;
        }

        String formattedMessage = data.getMessage();
        Bukkit.getScheduler().runTask(Bendinghub.plugin, () -> {
            int playerCount = 0;
            for (Player recipient : Bukkit.getOnlinePlayers()) {
                String permission = chatChannel.getPermission();
                if (permission != null && !permission.isBlank() && !recipient.hasPermission(permission)) {
                    continue;
                }
                try {
                    Methods.sendPlayerMessage(recipient, formattedMessage);
                    playerCount++;
                } catch (Exception exception) {
                    Bendinghub.log.warning("Failed to send cross-server chat to player " + recipient.getName() + ": " + exception.getMessage());
                }
            }

            try {
                Methods.sendConsoleMessage(formattedMessage);
            } catch (Exception exception) {
                Bendinghub.log.warning("Failed to send cross-server chat to console: " + exception.getMessage());
            }

            Bendinghub.debug("ChatPluginMessageListener: delivered message on channel '" + chatChannel.getId() + "' to " + playerCount + " players");
        });
    }

    private boolean isForwardedChannel(String channelId) {
        if (channelId == null || Bendinghub.configManager == null) {
            return false;
        }
        return Bendinghub.configManager.getConfig().getStringList("chat.proxy.forward-channels").stream()
                .anyMatch(forwarded -> forwarded.equalsIgnoreCase(channelId));
    }
}
