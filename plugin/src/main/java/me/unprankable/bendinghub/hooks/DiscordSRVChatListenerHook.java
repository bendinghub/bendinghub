package me.unprankable.bendinghub.hooks;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import me.unprankable.bendinghub.chat.ChatChannel;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;


public class DiscordSRVChatListenerHook {
    public static MiniMessage mm = MiniMessage.miniMessage();
    public static final List<String> ALLOWED_RECIEVING_CHANNELS = List.of("staff","local","global");
    @Subscribe(priority = ListenerPriority.NORMAL)
    public void onDiscordChatProcess(GameChatMessagePreProcessEvent event) {
        // If chat is disabled or not initialized, cancel all Discord forwarding
        if (Bendinghub.chatManager == null || Bendinghub.configManager == null || !Bendinghub.configManager.isChatEnabled()) {
            Bendinghub.debug("onDiscordChatProcess: chat disabled or not initialized, cancelling");
            event.setCancelled(true);
            return;
        }
        
        Player player = event.getPlayer();
        if (player == null) {
            Bendinghub.debug("onDiscordChatProcess: player is null, cancelling");
            event.setCancelled(true);
            return;
        }

        // Get the player's current channel
        ChatChannel currentChannel = Bendinghub.chatManager.getChannelManager().getPlayerChannel(player.getUniqueId());

        // If the channel shouldn't go to Discord (e.g., Staff, Local, Party)
        if (currentChannel == null) {
            Bendinghub.debug("onDiscordChatProcess: player " + player.getName() + " has no channel assigned, cancelling");
            event.setCancelled(true);
            return;
        }
        String channelId = currentChannel.getId();
        Bendinghub.debug("onDiscordChatProcess: player " + player.getName() + " in channel '" + currentChannel.getId() + "'");


        if (Methods.doesChannelExist(channelId)){
            event.setChannel(channelId);
        } else {
            event.setCancelled(true);
        }
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    public void onDiscordMessageProcessed(DiscordGuildMessagePostProcessEvent event) {
        // If chat is disabled or not initialized, don't route Discord messages to Minecraft
        if (Bendinghub.chatManager == null || Bendinghub.configManager == null || !Bendinghub.configManager.isChatEnabled()) {
            Bendinghub.debug("DiscordMessageProcessed: chat disabled or chatManager null, skipping");
            return;
        }
        
        TextChannel channelSentIn = event.getChannel();
        Bendinghub.debug("DiscordMessageProcessed: received message from Discord channel: " + channelSentIn.getName());
        
        String channel = DiscordSRV.getPlugin().getDestinationGameChannelNameForTextChannel(channelSentIn);
        Bendinghub.debug("DiscordMessageProcessed: mapped Discord channel '" + channelSentIn.getName() + "' to game channel: " + channel);
        ALLOWED_RECIEVING_CHANNELS.contains(channel.toLowerCase());
        if (channel != null && ALLOWED_RECIEVING_CHANNELS.contains(channel.toLowerCase())) {
            event.setCancelled(true);

            ChatChannel chatChannel = Bendinghub.chatManager.getChannelManager().getChannelById(channel);
            // Get the message and convert it to plain text to avoid ClassLoader issues with shaded Adventure library
            String messageText = chatChannel.getPrefix() + " <reset>" + Methods.convertComponentStringToMiniMessage(event.getMinecraftMessage().toString());

            //Jump over to the Minecraft main server thread to safely run player logic
            Bukkit.getScheduler().runTask(Bendinghub.plugin, () -> {

                if (chatChannel == null) {
                    Bendinghub.log.warning("DiscordSRV: " + channel + " channel not found in configuration");
                    return;
                }

                int PlayerCount = 0;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    boolean allowed = chatChannel.canView(player, player);
                    Bendinghub.debug("Player: " + player.getName() + " allowed: " + allowed);
                    if (allowed) {
                        try {
                            Methods.sendPlayerMessage(player, messageText);
                            PlayerCount++;
                        } catch (Exception e) {
                            Bendinghub.log.warning("Failed to send Discord message to player " + player.getName() + ": " + e.getMessage());
                        }
                    }
                }
                
                Bendinghub.debug("DiscordSRV: Message sent to " + PlayerCount + " players and console");
                try {
                    Methods.sendConsoleMessage(messageText);
                } catch (Exception e) {
                    Bendinghub.log.warning("Failed to send Discord message to console: " + e.getMessage());
                }
            });
        } else {
            Bendinghub.debug("DiscordMessageProcessed: channel '" + channel + "' is not 'staff/local/global', ignoring");
        }
    }


}
