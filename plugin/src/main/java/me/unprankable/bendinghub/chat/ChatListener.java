package me.unprankable.bendinghub.chat;

import io.papermc.paper.chat.ChatRenderer;
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

    public ChatListener() {
    }
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        // If chat is disabled or the ChatManager hasn't been initialized, do nothing and let vanilla chat run.
        if (Bendinghub.chatManager == null || Bendinghub.configManager == null || !Bendinghub.configManager.isChatEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        ChatChannel channel = Bendinghub.chatManager.getChannelManager().getPlayerChannel(player.getUniqueId());

        if (channel == null) {
            player.sendMessage(mm.deserialize("<red>Channel not found. Moved to global"));
            Bendinghub.chatManager.getChannelManager().setPlayerChannel(player.getUniqueId(), "global");
            channel = Bendinghub.chatManager.getChannelManager().getPlayerChannel(player.getUniqueId());
        }

        if (channel != null && channel.getPermission() != null && !player.hasPermission(channel.getPermission())){
            player.sendMessage(mm.deserialize("<red>You no longer have access to this channel. Moved to global"));
            Bendinghub.chatManager.getChannelManager().setPlayerChannel(player.getUniqueId(), "global");
            channel = Bendinghub.chatManager.getChannelManager().getPlayerChannel(player.getUniqueId());
        }

        if (channel == null) {
            event.setCancelled(true);
            player.sendMessage(mm.deserialize("<red>No valid chat channel is configured."));
            return;
        }

        final ChatChannel activeChannel = channel;
        //check if player can send message in this channel
        if (!activeChannel.canSend(player)) {
            event.setCancelled(true);
            player.sendMessage(mm.deserialize("<red>You cannot send messages in this channel."));
            return;
        }
        String plainText = PlainTextComponentSerializer.plainText().serialize(event.message());
        String formatted = activeChannel.fillInFormatValues(player, plainText);
        String resolved = Bendinghub.chatManager.convertLegacyToMiniMessage(formatted);
        //create message data object
        //send message data object to proxy
        //MessageDataObject messageDataObject = new MessageDataObject(activeChannel, resolved, player, Bendinghub.configManager.getConfig().getString("chat.proxy.server-id"));
        //messageDataObject.sendObject();
        // Filter viewers based on permissions and distance
        event.viewers().removeIf(viewer -> {
            if (!(viewer instanceof Player recipient)) return false; // Keep console

            return !activeChannel.canView(player, recipient); // Custom hook check
        });

        // Format and render the message
        event.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, message) -> mm.deserialize(resolved)));
//        event.renderer((source, sourceDisplayName, message, viewer) -> mm.deserialize(resolved));

    }
}
