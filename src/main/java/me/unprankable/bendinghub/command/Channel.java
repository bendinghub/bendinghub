package me.unprankable.bendinghub.command;
import me.unprankable.bendinghub.chat.ChatChannel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.entity.Player;

public class Channel {
    public static boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (player.hasPermission("bendinghub.chat.channel." + (args.length > 1 ? args[1].toLowerCase() : "global"))) {
            Bendinghub.chatManager.getChannelManager().setPlayerChannel(player.getUniqueId(), args.length > 1 ? args[1] : null);
            sender.sendMessage("Switched to channel: " + (args.length > 1 ? args[1] : "default"));
        } else {
            sender.sendMessage("You cannot switch to that channel.");
        }
        return true;
    }

     public static java.util.List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        // When tab-completing the channel name the args array will have length 2 (args[0] is the subcommand)
        if (args.length == 2) {
            String input = args[1] == null ? "" : args[1].toLowerCase();
            return Bendinghub.chatManager.getChannelManager().getChannels().stream()
                    .filter(channel -> sender.hasPermission("bendinghub.chat.channel." + channel.getId().toLowerCase()))
                    .map(ChatChannel::getId)
                    .filter(id -> id.toLowerCase().startsWith(input))
                    .toList();
        }
        return java.util.Collections.emptyList();
    }
}
