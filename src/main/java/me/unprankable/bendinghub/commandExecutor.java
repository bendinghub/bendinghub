package me.unprankable.bendinghub;

import me.unprankable.bendinghub.command.Reload;
import me.unprankable.bendinghub.command.Help;
import me.unprankable.bendinghub.command.Channel;
import me.unprankable.bendinghub.command.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class commandExecutor implements CommandExecutor, TabCompleter {
    private static final java.util.List<String> SUBCOMMANDS = java.util.Arrays.asList("reload", "help", "channel", "chatcolor");

    public commandExecutor() {
        Bendinghub.plugin.getCommand("bendinghub").setExecutor(this);
        Bendinghub.plugin.getCommand("bendinghub").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0){
            return false;
        }
        if (SUBCOMMANDS.contains(args[0].toLowerCase()) && !sender.hasPermission("bendinghub.command." + args[0].toLowerCase())) {
            Methods.sendPlayerMessage((Player) sender,org.bukkit.ChatColor.RED + "No permission");
            return true;
        }
        String subcommand = args[0].toLowerCase();
        Player player = (Player) sender;
        if (!player.hasPermission("bendinghub.command." + subcommand)) {
            Methods.sendPlayerMessage((Player) sender,org.bukkit.ChatColor.RED + "You do not have permission to perform this command.");
            return true;
        }
        switch(subcommand){
            case "reload", "rl":
                return Reload.execute(sender, command, label, args);
            case "ch","channel":
                return Channel.execute(sender, command, label, args);
            case "help","?":
                return Help.execute(sender, command, label, args);
            case "chatcolor","cc":
                return ChatColor.execute(sender, command, label, args);
            default:
                if (!sender.hasPermission("bendinghub.command.help")) {
                    Methods.sendPlayerMessage((Player) sender,org.bukkit.ChatColor.RED + "Unknown command, no permission to see help");
                    return true;
                }
                return Help.execute(sender, command, label, args);
        }
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1) {
            String input = args.length == 0 ? "" : args[0];
            return StringUtil.copyPartialMatches(input, SUBCOMMANDS.stream().filter(cmd -> sender.hasPermission("bendinghub.command." + cmd)).toList(), new ArrayList<>());
        }

        String subcommand = args[0].toLowerCase();
        if (sender.hasPermission("bendinghub.command." + subcommand)) {
            switch(subcommand) {
                case "reload", "rl":
                    return Reload.tabComplete(sender, command, alias, args);
                case "help", "?":
                    return Help.tabComplete(sender, command, alias, args);
                case "channel", "ch":
                    return Channel.tabComplete(sender, command, alias, args);
                case "chatcolor", "cc":
                    return ChatColor.tabComplete(sender, command, alias, args);
            }
        }
        return Collections.emptyList();
    }
}

