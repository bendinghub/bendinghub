package me.unprankable.bendinghub.command;

import me.unprankable.bendinghub.Methods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Help {
    public static boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            Methods.sendPlayerMessage((Player) sender,"Bendinghub Commands:");
            Methods.sendPlayerMessage((Player) sender,"/bendinghub channel <channel> - Switch your active chat channel");
            Methods.sendPlayerMessage((Player) sender,"/bendinghub chatcolor <color|reset> - Set your chat message color");
            Methods.sendPlayerMessage((Player) sender,"/bendinghub help - Show this help message");
            Methods.sendPlayerMessage((Player) sender,"/bendinghub reload - Reload the plugin configuration (Admin only)");
            return true;// Show command usage
        }
        String subcommand = args[1].toLowerCase();
        //send what the command is used for
        //send a description of the command
        //send the usage of the command
        switch (subcommand) {
            case "channel", "ch":
                Methods.sendPlayerMessage((Player) sender,"Used to switch your chat channel");
                Methods.sendPlayerMessage((Player) sender,"/bendinghub channel <channel> - Switch your active chat channel");
                Methods.sendPlayerMessage((Player) sender,"Switches your active chat channel to the specified channel. If no channel is specified, shows a list of available channels.");
                break;
            case "chatcolor", "cc":
                Methods.sendPlayerMessage((Player) sender,"Used to set your chat message color");
                Methods.sendPlayerMessage((Player) sender,"/bendinghub chatcolor <color|reset> - Set your chat message color");
                Methods.sendPlayerMessage((Player) sender,"Sets your chat message color to the specified color. Use 'reset' to clear your chat color.");
                break;
            case "reload", "rl":
                Methods.sendPlayerMessage((Player) sender,"Used to reload the plugin configuration");
                Methods.sendPlayerMessage((Player) sender,"/bendinghub reload <config|channels|chatcolors|all> - Reload the plugin configuration");
                Methods.sendPlayerMessage((Player) sender,"Reloads the specified part of the plugin configuration. Use 'all' to reload everything.");
                break;
            default:
                Methods.sendPlayerMessage((Player) sender,"Bendinghub Commands:");
                Methods.sendPlayerMessage((Player) sender,"/bendinghub channel <channel> - Switch your active chat channel");
                Methods.sendPlayerMessage((Player) sender,"/bendinghub chatcolor <color|reset> - Set your chat message color");
                Methods.sendPlayerMessage((Player) sender,"/bendinghub help - Show this help message");
                Methods.sendPlayerMessage((Player) sender,"/bendinghub reload - Reload the plugin configuration (Admin only)");
        }
        return true;
    }

    public static java.util.List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return java.util.Arrays.asList("channel", "chatcolor", "reload","help");
    }
}
