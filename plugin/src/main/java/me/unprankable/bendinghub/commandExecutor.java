package me.unprankable.bendinghub;

import me.unprankable.bendinghub.command.*;
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
    private static final List<String> SUBCOMMANDS = BendinghubCommand.getCommandNames();

    public static void loadCommands(){
        new Help();
        new Reload();
        new Channel();
        new ChatColor();
        new ClearChat();
        new namemc();
        Methods.registerAlias("tc", "bendinghub:channel", "town");
        Methods.registerAlias("townchat", "bendinghub:channel", "town");
        Methods.registerAlias("nc", "bendinghub:channel", "nation");
        Methods.registerAlias("nationchat", "bendinghub:channel", "nation");
        Methods.registerAlias("g", "bendinghub:channel", "global");
        Methods.registerAlias("global", "bendinghub:channel", "town");
        Methods.registerAlias("globalchat", "bendinghub:channel", "global");
    }

    public commandExecutor() {
        Bendinghub.plugin.getCommand("bendinghub").setExecutor(this);
        Bendinghub.plugin.getCommand("bendinghub").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0){
            return false;
        }

        String subcommand = args[0].toLowerCase();
        BendinghubCommand bhcommand = BendinghubCommand.getCommand(subcommand);

        Player player = (Player) sender;
        if (!player.hasPermission(bhcommand.getPermission())) {
            Methods.sendPlayerMessage((Player) sender,org.bukkit.ChatColor.RED + "You do not have permission to perform this command.");
            return true;
        }

        if(bhcommand.onlyPlayers()){
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
        }

        boolean result = bhcommand.execute(sender, command, label, args);
        if (!result){
            sender.sendMessage(bhcommand.helpMessage());
        }
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1) {
            String input = args.length == 0 ? "" : args[0];
            return StringUtil.copyPartialMatches(input, BendinghubCommand.getCommandNames().stream().filter(cmd -> sender.hasPermission("bendinghub.command." + cmd)).toList(), new ArrayList<>());
        }

        String subcommand = args[0].toLowerCase();
        BendinghubCommand bhcommand = BendinghubCommand.getCommand(subcommand);

        if (sender.hasPermission(bhcommand.getPermission())) {
            return bhcommand.tabComplete(sender, command, alias, args);
        }

        return Collections.emptyList();
    }
}

