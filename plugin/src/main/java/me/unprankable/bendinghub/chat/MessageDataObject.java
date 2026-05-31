package me.unprankable.bendinghub.chat;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MessageDataObject {
    private static final int SERIAL_VERSION = 1;
    private static final String BENDINGHUB_CHAT = "bendinghub:chat";
    private final String channelId;
    private final String message;
    private final UUID senderUUID;
    private final String senderName;
    private final String serverName;

    public MessageDataObject(ChatChannel channel, String message, Player player, String serverName) {
        this.channelId = channel.getId();
        this.message = message;
        this.senderUUID = player.getUniqueId();
        this.senderName = player.getName();
        this.serverName = serverName;
    }

    public MessageDataObject(String channelId, String message, UUID senderUUID, String senderName, String serverName) {
        this.channelId = channelId;
        this.message = message;
        this.senderUUID = senderUUID;
        this.senderName = senderName;
        this.serverName = serverName;
    }

    public byte[] serialize() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(SERIAL_VERSION);
        out.writeUTF(channelId == null ? "" : channelId);
        out.writeUTF(message == null ? "" : message);
        out.writeLong(senderUUID == null ? 0L : senderUUID.getMostSignificantBits());
        out.writeLong(senderUUID == null ? 0L : senderUUID.getLeastSignificantBits());
        out.writeUTF(senderName == null ? "" : senderName);
        out.writeUTF(serverName == null ? "" : serverName);
        return out.toByteArray();
    }

    public static MessageDataObject deserialize(byte[] data) {
        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(data);
            int version = in.readInt();
            if (version != SERIAL_VERSION) {
                Bendinghub.log.warning("Unsupported chat payload version: " + version);
                return null;
            }

            String channelId = in.readUTF();
            String message = in.readUTF();
            UUID senderUUID = new UUID(in.readLong(), in.readLong());
            String senderName = in.readUTF();
            String serverName = in.readUTF();
            return new MessageDataObject(channelId, message, senderUUID, senderName, serverName);
        } catch (Exception e) {
            Bendinghub.log.warning("Failed to decode cross-server chat payload: " + e.getMessage());
            return null;
        }
    }

    public void sendObject(){
        Player player = Bukkit.getPlayer(senderUUID);
        if (player == null) {
            return;
        }

        String subchannel = Bendinghub.configManager != null
                ? Bendinghub.configManager.getConfig().getString("chat.proxy.forward-subchannel", BENDINGHUB_CHAT)
                : BENDINGHUB_CHAT;
        player.sendPluginMessage(Bendinghub.plugin, subchannel, serialize());
    }

    public String getChannelId() {
        return channelId;
    }

    public String getMessage() {
        return message;
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getServerName() {
        return serverName;
    }
}
