package me.unprankable.bendinghub.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.unprankable.bendinghub.Bendinghub;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final ChatManager chatManager;

    public ChatListener(final ChatManager chatManager) {
        this.chatManager = chatManager;
    }
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        ChatChannel channel = chatManager.getChannelManager().getPlayerChannel(player.getUniqueId());

        if (channel == null) {
            player.sendMessage(mm.deserialize("<red>Channel not found. Moved to global"));
            chatManager.getChannelManager().setPlayerChannel(player.getUniqueId(), "global");
        }

        if (channel.getPermission() != null && !player.hasPermission(channel.getPermission())){
            player.sendMessage(mm.deserialize("<red>You no longer have access to this channel. Moved to global"));
            chatManager.getChannelManager().setPlayerChannel(player.getUniqueId(), "global");
        }

        // Filter viewers based on permissions and distance
        event.viewers().removeIf(viewer -> {
            if (!(viewer instanceof Player recipient)) return false; // Keep console

            // Permission check for recipient (e.g., can they see staff chat?)
            if (channel.getPermission() != null && !recipient.hasPermission(channel.getPermission())) {
                return true; // Remove this viewer
            }

            // Distance check for Local chat
            if (channel.getRadius() > 0) {
                if (!recipient.getWorld().equals(player.getWorld()) ||
                        recipient.getLocation().distance(player.getLocation()) > channel.getRadius()) {
                    return true; // Remove this viewer
                }
            }

            return false; // Keep this viewer
        });

        // Format and render the message
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            String plainText = PlainTextComponentSerializer.plainText().serialize(message);
            String formatted = channel.fillInFormatValues(player, plainText);
            String resolved = convertLegacyToMiniMessage(formatted);
//            Bendinghub.log.info("Formatted message: " + formatted);
//            Bendinghub.log.info("Resolved message: " + resolved);
//            Bendinghub.log.info("Original message: " + plainText);
//            Bendinghub.log.info("converted: " + convertLegacyToMiniMessage(formatted));
            return mm.deserialize(resolved);
        });
    }

    /**
     * Convert Bukkit color codes (&0-&f, &l, &m, &n, &o, &k, &r) to MiniMessage equivalents.
     */
    public static String convertLegacyToMiniMessage(String text) {
        // Color codes
        text = text.replace("§","&");
        text = text.replace("&0", "<black>");
        text = text.replace("&1", "<dark_blue>");
        text = text.replace("&2", "<dark_green>");
        text = text.replace("&3", "<dark_aqua>");
        text = text.replace("&4", "<dark_red>");
        text = text.replace("&5", "<dark_purple>");
        text = text.replace("&6", "<gold>");
        text = text.replace("&7", "<gray>");
        text = text.replace("&8", "<dark_gray>");
        text = text.replace("&9", "<blue>");
        text = text.replace("&a", "<green>");
        text = text.replace("&b", "<aqua>");
        text = text.replace("&c", "<red>");
        text = text.replace("&d", "<light_purple>");
        text = text.replace("&e", "<yellow>");
        text = text.replace("&f", "<white>");

        // Format codes
        text = text.replace("&l", "<bold>");
        text = text.replace("&m", "<strikethrough>");
        text = text.replace("&n", "<underline>");
        text = text.replace("&o", "<italic>");
        text = text.replace("&k", "<obfuscated>");
        text = text.replace("&r", "<reset>");

        return text;
    }
}
