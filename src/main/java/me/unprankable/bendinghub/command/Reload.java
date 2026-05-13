package me.unprankable.bendinghub.command;

import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Reload{
    private static final Bendinghub plugin = Bendinghub.plugin;
    public static boolean execute(CommandSender sender, Command command, String label, String[] args) {
        plugin.getLogger().info("Reloading Bendinghub configuration...");
        plugin.configManager.reload();
        if (plugin.chatManager != null) {
            plugin.chatManager.getChannelManager().reloadChannels();
            plugin.chatManager.getChatColorManager().loadPlayerChatColors();
        }
        sender.sendMessage("Bendinghub reloaded.");
        return true;
    }

    public static java.util.List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return java.util.Collections.singletonList("reload");
        }
        return java.util.Collections.emptyList();
    }
}

