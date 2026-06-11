package me.unprankable.bendinghub.tab;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.hooks.PlaceholderAPIHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import java.util.UUID;

public class TabPluginMessageListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("bendinghub:tab")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        int ID = in.readInt();
        Bendinghub.debug("message recieved with ID: " + ID);
        String subChannel = in.readUTF();
        String requesting = in.readUTF();
        UUID uuid = UUID.fromString(in.readUTF());
        String text = in.readUTF();

        if (subChannel.equals("REQUEST_PLACEHOLDERS")) {
            Player target = Bukkit.getPlayer(uuid);

            if (target == null) return;

            String parsedText = PlaceholderAPIHook.parsePlaceholders(target,text);

            // Build the response
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(ID);
            out.writeUTF("RESPONSE_PLACEHOLDERS");
            out.writeUTF(requesting);
            out.writeUTF(target.getUniqueId().toString());
            out.writeUTF(parsedText);

            // Send it back to Velocity
            target.sendPluginMessage(Bendinghub.plugin, "bendinghub:tab", out.toByteArray());
            Bendinghub.debug("message sent with ID: " + ID);

        }
    }
}