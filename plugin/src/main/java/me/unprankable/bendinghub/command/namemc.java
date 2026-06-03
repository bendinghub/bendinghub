package me.unprankable.bendinghub.command;

import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class namemc extends BendinghubCommand{

    public static boolean likedServer(UUID playerUUID) {
        try {
            final URLConnection url = new URL("https://api.namemc.com/server/mc.bendinghub.net/likes?profile=" + playerUUID.toString()).openConnection();
            url.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

            //parse json response
            byte[] responseBytes = url.getInputStream().readAllBytes();
            String response = new String(responseBytes, java.nio.charset.StandardCharsets.UTF_8);
            Bendinghub.debug("NameMC API response for player " + playerUUID + ": " + response);
            return Boolean.parseBoolean(response);
        } catch  (Exception e) {
            Bendinghub.log.warning("Failed to check if player " + playerUUID + " liked the server on NameMC: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getAuthor(){
        return "Unprankable";
    }

    @Override
    public String getName(){
        return "namemc";
    }

    @Override
    public boolean onlyPlayers(){
        return true;
    }

    @Override
    public List<String> getAliases(){
        return List.of("nm");
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(" Only players can use this command");
            return true;
        }
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        boolean liked = likedServer(playerUUID);
        if (liked){
            Methods.sendPlayerMessage(player , "<green> Thank you for supporting the server, you can now recieve the VIP rank");
            //execute command to give player vip rank
            if (Bendinghub.luckpermsEnabled){
                Bendinghub.plugin.getServer().dispatchCommand(Bendinghub.plugin.getServer().getConsoleSender(), "lp user " + playerUUID.toString() + " parent add vip");
            } else {
                Methods.sendPlayerMessage(player, "<red>Luckperms not found on server");
            }
            return true;
        } else {
            Methods.sendPlayerMessage(player , "<red> You have not liked the server on NameMC \n You can like the server <click:open_url:https://namemc.com/server/mc.bendinghub.net><blue><u>here</u></blue></click> \n to receive the VIP rank</red>");
            return true;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }
}
