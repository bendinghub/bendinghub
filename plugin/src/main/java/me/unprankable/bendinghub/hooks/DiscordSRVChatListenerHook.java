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



public class DiscordSRVChatListenerHook {
    public static MiniMessage mm = MiniMessage.miniMessage();
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
        
        Bendinghub.debug("onDiscordChatProcess: player " + player.getName() + " in channel '" + currentChannel.getId() + "'");
        
        switch (currentChannel.getId()){
            case "global":
                Bendinghub.debug("onDiscordChatProcess: routing message from " + player.getName() + " to Discord global channel");
                event.setChannel("global");
                break;
            case "staff":
                Bendinghub.debug("onDiscordChatProcess: routing message from " + player.getName() + " to Discord staff channel");
                event.setChannel("staff");
                break;
            default:
                Bendinghub.debug("onDiscordChatProcess: channel '" + currentChannel.getId() + "' not forwarded to Discord, cancelling");
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

        if (channel != null && channel.equalsIgnoreCase("staff")) {
            Bendinghub.log.info("DiscordSRV: Routing message from Discord staff channel to Minecraft staff players");
            event.setCancelled(true);

            // Get the message and convert it to plain text to avoid ClassLoader issues with shaded Adventure library
            String messageText = "<#A4A4A4>[<red>Staff</red>]</#A4A4A4> " + Methods.convertComponentStringToMiniMessage(event.getMinecraftMessage().toString());

            //Jump over to the Minecraft main server thread to safely run player logic
            Bukkit.getScheduler().runTask(Bendinghub.plugin, () -> {

                ChatChannel staffChannel = Bendinghub.chatManager.getChannelManager().getChannelById("staff");
                if (staffChannel == null) {
                    Bendinghub.log.warning("DiscordSRV: Staff channel not found in configuration");
                    return;
                }

                int staffPlayerCount = 0;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    boolean isStaff = staffChannel.canView(player, player);
                    Bendinghub.debug("Player: " + player.getName() + " Staff: " + isStaff);
                    if (isStaff) {
                        try {
                            Methods.sendPlayerMessage(player, messageText);
                            staffPlayerCount++;
                        } catch (Exception e) {
                            Bendinghub.log.warning("Failed to send Discord message to player " + player.getName() + ": " + e.getMessage());
                        }
                    }
                }
                
                Bendinghub.debug("DiscordSRV: Message sent to " + staffPlayerCount + " staff players and console");
                try {
                    Methods.sendConsoleMessage(messageText);
                } catch (Exception e) {
                    Bendinghub.log.warning("Failed to send Discord message to console: " + e.getMessage());
                }
            });
        } else {
            Bendinghub.debug("DiscordMessageProcessed: channel '" + channel + "' is not 'staff', ignoring");
        }
    }


}
