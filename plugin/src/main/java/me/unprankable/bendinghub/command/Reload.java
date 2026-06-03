package me.unprankable.bendinghub.command;

import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Reload extends BendinghubCommand{

    @Override
    public String getAuthor(){
        return "Unprankable";
    }

    @Override
    public String getName(){
        return "reload";
    }

    @Override
    public List<String> getAliases(){
        return List.of("rl");
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        String params;
        if (args.length == 1){
            params = "all";
        } else {
            params = args[1].toLowerCase();
        }
        String[] reloads = params.split(",");
        for (String reload : reloads) {
            reload = reload.trim();
            switch (reload) {
                case "config", "configuration", "cfg", "conf":
                    Bendinghub.log.info("Reloading Bendinghub configuration...");
                    Bendinghub.configManager.reload();
                    sender.sendMessage("Bendinghub configuration reloaded.");
                    break;
                case "channels", "ch", "channel", "chan":
                    if (!Bendinghub.configManager.isChatEnabled()) break;
                    Bendinghub.log.info("Reloading player chat channels...");
                    Bendinghub.chatManager.getChannelManager().loadPlayerChannels();
                    sender.sendMessage("Chat channels reloaded.");
                    break;
                case "chatcolors", "cc", "color", "colors":
                    if (!Bendinghub.configManager.isChatEnabled()) break;
                    Bendinghub.log.info("Reloading player chat colors...");
                    Bendinghub.chatManager.getChatColorManager().loadPlayerChatColors();
                    sender.sendMessage("Player chat colors reloaded.");
                    break;
                case "all", "*", "everything", "full":
                    Bendinghub.log.info("Reloading all Bendinghub data...");
                    Bendinghub.reloadPlugin();
                    sender.sendMessage("All Bendinghub data reloaded.");
                    break;
            }
        }
        return true;
    }

    @Override
    public java.util.List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> Reloadable = new ArrayList<>();
        Reloadable.add("config");
        Reloadable.add("channels");
        Reloadable.add("chatcolors");
        Reloadable.add("all");
        return Reloadable;
    }
}

