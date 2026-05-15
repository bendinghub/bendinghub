package me.unprankable.bendinghub.chat;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.unprankable.bendinghub.Bendinghub;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CrossServerChatBridge implements PluginMessageListener {
    private static final String PROXY_CHANNEL = "BungeeCord";
    private static final long SEEN_TTL_MS = 30_000L;

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<String, Long> seenMessageIds = new ConcurrentHashMap<>();

    public CrossServerChatBridge() {

        Bendinghub.plugin.getServer().getMessenger().registerOutgoingPluginChannel(Bendinghub.plugin, PROXY_CHANNEL);
        Bendinghub.plugin.getServer().getMessenger().registerIncomingPluginChannel(Bendinghub.plugin, PROXY_CHANNEL, this);
    }

    public void shutdown() {
        Bendinghub.plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(Bendinghub.plugin, PROXY_CHANNEL);
        Bendinghub.plugin.getServer().getMessenger().unregisterIncomingPluginChannel(Bendinghub.plugin, PROXY_CHANNEL, this);
        seenMessageIds.clear();
    }

    public boolean isEnabled() {
        return Bendinghub.configManager.getConfig().getBoolean("chat.proxy.enabled", false);
    }

    public boolean shouldForwardChannel(String channelId) {
        if (!isEnabled()) {
            return false;
        }
        return Bendinghub.configManager.getConfig().getStringList("chat.proxy.forward-channels")
                .stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(channelId));
    }

    public void forward(Player player, String channelId, String resolvedMiniMessageText) {
        if (!isEnabled() || player == null || !player.isOnline()) {
            return;
        }

        String serverId = Bendinghub.configManager.getConfig().getString("chat.proxy.server-id", "default");
        String forwardSubchannel = Bendinghub.configManager.getConfig().getString("chat.proxy.forward-subchannel", "bendinghub:chat");
        String messageId = UUID.randomUUID().toString();

        markSeen(messageId);

        // Payload data that all backend servers will parse.
        ByteArrayDataOutput payloadOut = ByteStreams.newDataOutput();
        payloadOut.writeUTF(serverId);
        payloadOut.writeUTF(messageId);
        payloadOut.writeUTF(channelId == null ? "global" : channelId.toLowerCase());
        payloadOut.writeUTF(player.getUniqueId().toString());
        payloadOut.writeUTF(player.getName());
        payloadOut.writeUTF(resolvedMiniMessageText == null ? "" : resolvedMiniMessageText);
        byte[] payload = payloadOut.toByteArray();

        // BungeeCord Forward: send payload to ALL servers via proxy.
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(forwardSubchannel);
        out.writeShort(payload.length);
        out.write(payload);

        // Plugin message send must be performed from the server thread.
        Bukkit.getScheduler().runTask(Bendinghub.plugin, () -> {
            if (player.isOnline()) {
                player.sendPluginMessage(Bendinghub.plugin, PROXY_CHANNEL, out.toByteArray());
            }
        });
    }

    @Override
    public void onPluginMessageReceived(String channel, Player receiver, byte[] message) {
        if (!PROXY_CHANNEL.equals(channel) || !isEnabled()) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        String expectedSubchannel = Bendinghub.configManager.getConfig().getString("chat.proxy.forward-subchannel", "bendinghub:chat");
        if (!expectedSubchannel.equalsIgnoreCase(subchannel)) {
            return;
        }

        short payloadLength = in.readShort();
        byte[] payload = new byte[payloadLength];
        in.readFully(payload);

        ByteArrayDataInput payloadIn = ByteStreams.newDataInput(payload);
        String originServerId = payloadIn.readUTF();
        String messageId = payloadIn.readUTF();
        String channelId = payloadIn.readUTF();
        payloadIn.readUTF(); // sender UUID (currently unused)
        payloadIn.readUTF(); // sender name (currently unused; already in resolved format)
        String resolvedMiniMessageText = payloadIn.readUTF();

        if (isDuplicate(messageId)) {
            return;
        }
        markSeen(messageId);

        String localServerId = Bendinghub.configManager.getConfig().getString("chat.proxy.server-id", "default");
        if (localServerId.equalsIgnoreCase(originServerId)) {
            return;
        }

        ChatChannel chatChannel = Bendinghub.chatManager.getChannelManager().getChannelById(channelId);
        if (chatChannel == null) {
            return;
        }

        Component rendered = mm.deserialize(resolvedMiniMessageText);
        Bukkit.getScheduler().runTask(Bendinghub.plugin, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                String permission = chatChannel.getPermission();
                if (permission == null || permission.isEmpty() || online.hasPermission(permission)) {
                    online.sendMessage(rendered);
                }
            }
        });
    }

    private boolean isDuplicate(String messageId) {
        cleanupSeenCache();
        return messageId != null && seenMessageIds.containsKey(messageId);
    }

    private void markSeen(String messageId) {
        if (messageId != null && !messageId.isBlank()) {
            seenMessageIds.put(messageId, System.currentTimeMillis());
        }
    }

    private void cleanupSeenCache() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = seenMessageIds.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (now - entry.getValue() > SEEN_TTL_MS) {
                iterator.remove();
            }
        }
    }
}


