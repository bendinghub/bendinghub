package me.unprankable.bendinghub.tab;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerDataObject {
    public static final String BENDINGHUB_TAB = "bendinghub:tab";
    private static final int SERIAL_VERSION = 2;

    private final String playerTabFormat;
    private final String playerName;
    private final String playerUUID;
    private final String playerGroup;
    private final String serverName;
    private final String action;

    public PlayerDataObject(String playerTabFormat, String playerName, String playerUUID, String playerGroup, String serverName, String action){
        this.playerTabFormat = playerTabFormat;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.playerGroup = playerGroup;
        this.serverName = serverName;
        this.action = action;
    }

    public byte[] serialize() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(SERIAL_VERSION);
        out.writeUTF(playerTabFormat == null ? "prefix name suffix" : playerTabFormat);
        out.writeUTF(playerName == null ? "player" : playerName);
        out.writeUTF(playerUUID == null ? "8e6c8ea4-e952-4dc3-a0cc-0e623dcee05b" : playerUUID);
        out.writeUTF(playerGroup == null ? "group" : playerGroup);
        out.writeUTF(serverName == null ? "server" : serverName);
        out.writeUTF(action == null ? "QUIT" : action);

        return out.toByteArray();
    }

    public static PlayerDataObject deserialize(byte[] data) {
        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(data);
            int version = in.readInt();
            if (version != 1 && version != SERIAL_VERSION) {
                Bendinghub.log.warning("Unsupported tab payload version: " + version);
                return null;
            }

            String playerTabFormat = in.readUTF();
            String playerName = in.readUTF();
            String playerUUID = in.readUTF();
            String playerGroup = in.readUTF();
            String serverName = in.readUTF();
            String action = in.readUTF();

            return new PlayerDataObject(playerTabFormat, playerName, playerUUID, playerGroup, serverName, action);
        } catch (Exception e) {
            Bendinghub.log.warning("Failed to decode cross-server tab payload: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sends the object dynamically.
     * If players are online, it uses standard messaging.
     * If the server is empty, it uses Spigot's central Messenger service.
     */
    public void sendObject() {
        byte[] data = serialize();

        // Attempt to find any player to carrier-route the packet natively
        Player player = playerUUID == null ? null : Bukkit.getPlayer(java.util.UUID.fromString(playerUUID));
        if (player == null) {
            player = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        }

        if (player != null) {
            // Standard approach when players exist on this backend instance
            player.sendPluginMessage(Bendinghub.plugin, BENDINGHUB_TAB, data);
            Bendinghub.debug("MessageDataObject sent via online player link (" + player.getName() + "):\n" + this);
        } else {
            // FIX FOR 0 PLAYERS: Route directly through Spigot's central messaging bus.
            // Velocity maintains an open connection to empty registered servers and can capture this.
            Bukkit.getServer().sendPluginMessage(Bendinghub.plugin, BENDINGHUB_TAB, data);
            Bendinghub.debug("MessageDataObject sent via empty-server fallback bus:\n" + this);
        }
    }

    public String getPlayerTabFormat(){ return playerTabFormat; }
    public String getPlayerName(){ return playerName; }
    public String getPlayerUUID(){ return playerUUID; }
    public String getPlayerGroup(){ return playerGroup; }
    public String getServerName(){ return serverName; }
    public String getAction(){ return action; }

    @Override
    public String toString() {
        return "{\"playerTabFormat\":\"" + getPlayerTabFormat() + "\","
                + "\"playerName\":\"" + getPlayerName() + "\","
                + "\"playerUUID\":\"" + getPlayerUUID() + "\","
                + "\"playerGroup\":\"" + getPlayerGroup() + "\","
                + "\"serverName\":\"" + getServerName() + "\","
                + "\"action\":\"" + getAction() + "\"}";
    }
}