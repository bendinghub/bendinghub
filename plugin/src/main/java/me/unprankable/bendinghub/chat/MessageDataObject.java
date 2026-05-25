package me.unprankable.bendinghub.chat;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.UUID;

public class MessageDataObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String BENDINGHUB_CHAT = "bendinghub:chat";
    private String channelId;
    private String message;
    private UUID senderUUID;
    private String serverName;

    public MessageDataObject(ChatChannel channel, String message, Player player, String serverName) {
        this.channelId = channel.getId();
        this.message = message;
        this.senderUUID = player.getUniqueId();
        this.serverName = serverName;
    }

    public byte[] serialize() {
        // Serialization logic if needed, but since we're using Java's built-in serialization, this can be left empty.
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(this);
            objOut.flush();
            return byteOut.toByteArray();
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static MessageDataObject deserialize(byte[] data) {
        // Deserialization logic if needed, but since we're using Java's built-in serialization, this can be left empty.
        // This method would be used on the receiving end to convert the byte array back into a MessageDataObject.
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
            ObjectInputStream objIn = new ObjectInputStream(byteIn);

            return (MessageDataObject) objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendObject(){
        Player player = Bukkit.getPlayer(senderUUID);
        player.sendPluginMessage(Bendinghub.plugin, BENDINGHUB_CHAT, serialize());
    }
}
