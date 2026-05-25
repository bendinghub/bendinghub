package me.unprankable.bendinghub.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook {
    public static String parsePlaceholders(Player player, String text) {
        if(Bendinghub.plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, text);
        } else {
            Bendinghub.debug("PlaceholderAPI is not enabled, skipping placeholder parsing for: " + text);
            return text;
        }
    }
}
