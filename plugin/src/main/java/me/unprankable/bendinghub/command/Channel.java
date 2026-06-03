package me.unprankable.bendinghub.command;
import me.unprankable.bendinghub.Methods;
import me.unprankable.bendinghub.chat.ChatChannel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.entity.Player;

import java.util.List;

public class Channel extends BendinghubCommand{

    @Override
    public String getAuthor(){
        return "Unprankable";
    }

    @Override
    public String getName(){
        return "channel";
    }

    @Override
    public boolean onlyPlayers(){
        return true;
    }

    @Override
    public String getUsage(){
        return "/bh channel <channel_name>";
    }

    @Override
    public List<String> getAliases(){
        return List.of("ch");
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (Bendinghub.configManager == null || !Bendinghub.configManager.isChatEnabled()) {
            sender.sendMessage("Chat is disabled on this server.");
            return true;
        }

        // if no channel is specified, send channel list
        if (args.length == 1) {
            Methods.sendPlayerMessage((Player) sender,"Available channels:");
            for (ChatChannel channel : Bendinghub.chatManager.getChannelManager().getChannels()) {
                if (sender.hasPermission(channel.getPermission())) {
                    Methods.sendPlayerMessage((Player) sender,"- " + channel.getId());
                }
            }
            return true;
        }

        Player player;
        if (args.length == 2){
            player = (Player) sender;
        } else {
            if (sender.hasPermission("bendinghub.command.channel.others")) {
                player = Bukkit.getPlayer(args[2]);
            } else {
                player = (Player) sender;
            }
        }

        if (player.hasPermission("bendinghub.chat.channel." + (args.length > 1 ? args[1].toLowerCase() : "global"))) {
            Bendinghub.chatManager.getChannelManager().setPlayerChannel(player.getUniqueId(), args.length > 1 ? args[1] : null);
            Methods.sendPlayerMessage((Player) sender,"<green>Switched to channel: <yellow>" + (args.length > 1 ? args[1] : "default") + "<reset>");
        } else {
            Methods.sendPlayerMessage((Player) sender,"<red>You cannot switch to that channel.");
        }
        if (!player.getUniqueId().toString().equals(((Player) sender).getUniqueId().toString())){
            Methods.sendPlayerMessage((Player) sender, "<green>Set channel to " + args[1] + " For: <yellow>" + player.getName() + "<reset>");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        // When tab-completing the channel name the args array will have length 2 (args[0] is the subcommand)
        if (Bendinghub.configManager == null || !Bendinghub.configManager.isChatEnabled() || Bendinghub.chatManager == null) {
            return java.util.Collections.emptyList();
        }

        String input = args.length > 1 && args[1] != null ? args[1].toLowerCase() : "";
        return Bendinghub.chatManager.getChannelManager().getChannels().stream()
                .filter(channel -> sender.hasPermission("bendinghub.chat.channel." + channel.getId().toLowerCase()))
                .map(ChatChannel::getId)
                .filter(id -> id.toLowerCase().startsWith(input))
                .toList();
    }
}
