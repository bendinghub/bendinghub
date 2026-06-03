package me.unprankable.bendinghub.command;

import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import me.unprankable.bendinghub.chat.MessageDataObject;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ClearChat extends BendinghubCommand{

    @Override
    public String getAuthor(){
        return "Unprankable";
    }

    @Override
    public String getName(){
        return "clearchat";
    }

    @Override
    public List<String> getAliases(){
        return List.of("clear");
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            // If run from console, pick any online player to send the plugin-message (proxy requires a player)
            Bendinghub.log.info("No online players to clear chat for");
        }

        int numBlankLines = Bendinghub.configManager.getClearChatLines();
        String message = "";
        for (int i = 0; i < numBlankLines; i++){
            message += "<newline>";
        }
        message += "<aqua>Chat has been cleared by<yellow> " + player.getName() + "</yellow>.</aqua>";

        for (Player online: Bukkit.getOnlinePlayers()){
            Methods.sendPlayerMessage(online, message);
        }
        Methods.sendConsoleMessage(message);

        if (Bendinghub.configManager.getConfig().getBoolean("chat.proxy.enabled")){
            MessageDataObject messageDataObject = MessageDataObject.ClearChatObject(player);
            Bukkit.getScheduler().runTask(Bendinghub.plugin, messageDataObject::sendObject);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }
}


