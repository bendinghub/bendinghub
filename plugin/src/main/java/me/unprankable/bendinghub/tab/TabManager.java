package me.unprankable.bendinghub.tab;

import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import me.unprankable.bendinghub.chat.ChatManager;
import me.unprankable.bendinghub.hooks.PlaceholderAPIHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

public class TabManager {
    public static BukkitTask task;
    public FakePlayer fakePlayer;
    public TabPluginMessageListener tabPluginMessageListener;

    public TabManager(){
        fakePlayer = new FakePlayer();
        tabPluginMessageListener = new TabPluginMessageListener();
        Bukkit.getMessenger().registerOutgoingPluginChannel(Bendinghub.plugin, PlayerDataObject.BENDINGHUB_TAB);
        Bukkit.getMessenger().registerIncomingPluginChannel(Bendinghub.plugin, PlayerDataObject.BENDINGHUB_TAB, tabPluginMessageListener);
    }

    public void updateAllPlayers(){
        for (Player player: Bukkit.getOnlinePlayers()){
            updateHeaderFooter(player);
            setPlayerTags(player);
        }
    }

    public void updateHeaderFooter(Player player){
        //convert legacy to minimessage
        FileConfiguration cfg = Bendinghub.configManager.getConfig();
        String header = Bendinghub.configManager.getTabHeader();
        String footer = Bendinghub.configManager.getTabFooter();
        ConfigurationSection placeholders = cfg.getConfigurationSection("tab.placeholders");
        if (placeholders != null) {
            for (String phKey : placeholders.getKeys(false)) {
                String phValue = placeholders.getString(phKey);
                if (phValue != null) {
                    header = header.replace(phKey, phValue);
                    footer = footer.replace(phKey, phValue);
                }
            }
        }
        header = ChatManager.convertLegacyToMiniMessage(PlaceholderAPIHook.parsePlaceholders(player, header));
        footer = ChatManager.convertLegacyToMiniMessage(PlaceholderAPIHook.parsePlaceholders(player, footer));
        player.sendPlayerListHeaderAndFooter(Methods.convertMiniMessageStringToComponent(header), Methods.convertMiniMessageStringToComponent(footer));
    }

    public void setPlayerTags(Player player){
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
        player.playerListName(Methods.convertMiniMessageStringToComponent(format));
    }
}
