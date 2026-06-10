package me.unprankable.bendinghub.tab;

import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.chat.ChatManager;
import me.unprankable.bendinghub.hooks.PlaceholderAPIHook;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TabListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        Bendinghub.tabManager.setPlayerTags(player);
        Bendinghub.tabManager.updateHeaderFooter(player);
        String format = Bendinghub.configManager.getConfig().getString("tab.tabnameformat");
        ConfigurationSection placeholders = Bendinghub.configManager.getConfig().getConfigurationSection("tab.placeholders");
        if (placeholders != null) {
            for (String phKey : placeholders.getKeys(false)) {
                String phValue = placeholders.getString(phKey);
                if (phValue != null) {
                    format = format.replace(phKey, phValue);
                }
            }
        }
        format = ChatManager.convertLegacyToMiniMessage(PlaceholderAPIHook.parsePlaceholders(player, format));
        String serverName = Bendinghub.configManager.getConfig().getString("chat.proxy.server-id", "server-1");
        PlayerDataObject playerDataObject = new PlayerDataObject(format, player.getName(), player.getUniqueId().toString(), PlaceholderAPIHook.parsePlaceholders(player, "%luckperms_primary_group_name%"), serverName, "JOIN");
        Bukkit.getScheduler().runTaskLater(Bendinghub.plugin, playerDataObject::sendObject, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        String format = Bendinghub.configManager.getConfig().getString("tab.tabnameformat");
        ConfigurationSection placeholders = Bendinghub.configManager.getConfig().getConfigurationSection("tab.placeholders");
        if (placeholders != null) {
            for (String phKey : placeholders.getKeys(false)) {
                String phValue = placeholders.getString(phKey);
                if (phValue != null) {
                    format = format.replace(phKey, phValue);
                }
            }
        }
        format = ChatManager.convertLegacyToMiniMessage(PlaceholderAPIHook.parsePlaceholders(player, format));
        String serverName = Bendinghub.configManager.getConfig().getString("chat.proxy.server-id", "server-1");
        PlayerDataObject playerDataObject = new PlayerDataObject(format, player.getName(), player.getUniqueId().toString(), PlaceholderAPIHook.parsePlaceholders(player, "%luckperms_primary_group_name%"), serverName, "QUIT");
        Bukkit.getScheduler().runTaskLater(Bendinghub.plugin, playerDataObject::sendObject, 1L);
    }
}
