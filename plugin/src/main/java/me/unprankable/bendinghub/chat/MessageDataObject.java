package me.unprankable.bendinghub.chat;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MessageDataObject {
    public enum Reason {
        CHAT,
        CLEAR,
        BROADCAST
    }

    private static final int SERIAL_VERSION = 2;
    public static final String BENDINGHUB_CHAT = "bendinghub:chat";

    private final String channelId;
    private final String message;
    private final UUID senderUUID;
    private final String senderName;
    private final String serverName;
    private final Reason reason;

    public MessageDataObject(ChatChannel channel, String message, Player player, String serverName) {
        this(channel.getId(), message, player.getUniqueId(), player.getName(), serverName, Reason.CHAT);
    }

    public MessageDataObject(ChatChannel channel, String message, UUID senderUUID, String senderName, String serverName) {
        this(channel.getId(), message, senderUUID, senderName, serverName, Reason.CHAT);
    }

    public MessageDataObject(String channelId, String message, Player player, String serverName) {
        this(channelId, message, player.getUniqueId(), player.getName(), serverName, Reason.CHAT);
    }

    public MessageDataObject(String channelId, String message, UUID senderUUID, String senderName, String serverName) {
        this(channelId, message, senderUUID, senderName, serverName, Reason.CHAT);
    }

    public MessageDataObject(String channelId, String message, UUID senderUUID, String senderName, String serverName, Reason reason) {
        this.channelId = channelId;
        this.message = message;
        this.senderUUID = senderUUID;
        this.senderName = senderName;
        this.serverName = serverName;
        this.reason = reason == null ? Reason.CHAT : reason;
    }

    public static MessageDataObject ClearChatObject(Player player) {
        String channelId = "global";
        String message = "";
        int numBlankLines = Bendinghub.configManager.getClearChatLines();

        for (int i = 0; i < numBlankLines; i++){
            message += "<newline>";
        }

        message += "<aqua>Chat has been cleared by<yellow> " + player.getName() + "</yellow>.</aqua>";
        String serverName = Bendinghub.configManager.getConfig().getString("chat.proxy.server-id");
        MessageDataObject messageDataObject = new MessageDataObject(channelId, message, player, serverName);
        return messageDataObject;
    }

    public static MessageDataObject BroadcastMessageObject(String message){ //This is for clearchat
        String channelId = "global";
        String senderName = "CONSOLE";
        String serverName = Bendinghub.configManager != null
                ? Bendinghub.configManager.getConfig().getString("chat.proxy.server-id", "server-1")
                : "server-1";
        return new MessageDataObject(channelId, message, null, senderName, serverName, Reason.BROADCAST);
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
        out.writeUTF(reason == null ? Reason.CHAT.name() : reason.name());
        return out.toByteArray();
    }

    public static MessageDataObject deserialize(byte[] data) {
        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(data);
            int version = in.readInt();
            if (version != 1 && version != SERIAL_VERSION) {
                Bendinghub.log.warning("Unsupported chat payload version: " + version);
                return null;
            }

            String channelId = in.readUTF();
            String message = in.readUTF();
            UUID senderUUID = new UUID(in.readLong(), in.readLong());
            String senderName = in.readUTF();
            String serverName = in.readUTF();
            Reason reason;
            if (version >= SERIAL_VERSION) {
                String reasonValue = in.readUTF();
                try {
                    reason = Reason.valueOf(reasonValue.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    reason = Reason.CHAT;
                }
            } else {
                reason = Reason.CHAT;
            }
            return new MessageDataObject(channelId, message, senderUUID, senderName, serverName, reason);
        } catch (Exception e) {
            Bendinghub.log.warning("Failed to decode cross-server chat payload: " + e.getMessage());
            return null;
        }
    }

    public void sendObject() {
        Player player = senderUUID == null ? null : Bukkit.getPlayer(senderUUID);
        if (player == null) {
            player = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        }
        if (player == null) {
            Bendinghub.log.warning("Unable to send plugin message for reason " + reason + ": no online player is available to act as the sender.");
            return;
        }

        player.sendPluginMessage(Bendinghub.plugin, BENDINGHUB_CHAT, serialize());
        Bendinghub.debug("MessageDataObject sent:\n" + this);
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

    public Reason getReason() {
        return reason;
    }

    public String toString() {
        String message = "";
        message += "{";
        message += "\"channelId\":";
        message += "\"" + getChannelId() + "\",";
        message += "\"message\":";
        message += "\"" + getMessage() + "\",";
        message += "\"senderUUID\":";
        message += "\"" + getSenderUUID() + "\",";
        message += "\"senderName\":";
        message += "\"" + getSenderName() + "\",";
        message += "\"serverName\":";
        message += "\"" + getServerName() + "\",";
        message += "\"reason\":";
        message += "\"" + getReason() + "\"";
        message += "}";
        return message;

    }
}
