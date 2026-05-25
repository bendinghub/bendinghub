package me.unprankable.bendinghub;

import me.unprankable.bendinghub.hooks.PlaceholderAPIHook;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Methods {
    public static MiniMessage mm = MiniMessage.miniMessage();
    public static void sendPlayerMessage(Player player, String message){
        String converted = Bendinghub.chatManager.convertLegacyToMiniMessage(message);
        String parsed = PlaceholderAPIHook.parsePlaceholders(player, converted);
        Component deserialized = mm.deserialize(parsed);
        player.sendMessage(deserialized);
    }
}
