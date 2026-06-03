package me.unprankable.bendinghub;

import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private FileConfiguration config;
    private File configFile;

    public ConfigManager() {
    }

    public void load() {
        if (!Bendinghub.plugin.getDataFolder().exists()) {
            Bendinghub.plugin.getDataFolder().mkdirs();
        }

        configFile = new File(Bendinghub.plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            try {
                Bendinghub.plugin.saveResource("config.yml", false);
            } catch (IllegalArgumentException exception) {
                Bendinghub.log.warning("Bundled config.yml was not found. Creating a minimal config file.");
                try {
                    configFile.createNewFile();
                } catch (IOException ioException) {
                    Bendinghub.log.severe("Failed to create config.yml: " + ioException.getMessage());
                }
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        config.addDefault("chat.enabled", true);
        config.addDefault("debug", false);
        config.addDefault("chat.proxy.enabled", true);
        config.addDefault("chat.proxy.server-id", "server-1");
        config.addDefault("chat.proxy.forward-channels", Arrays.asList("global", "staff"));
        config.addDefault("chat.clearchat.recieve", true);
        config.addDefault("chat.placeholders.<displayname>", "%player_name%");
        config.addDefault("chat.placeholders.<rank>", "%luckperms_prefix%");
        config.addDefault("chat.placeholders.<suffix>", "%luckperms_suffix%");
        config.addDefault("chat.placeholders.<chatcolor>", "%bendinghub_chatcolor%");
        config.addDefault("chat.channels.global.prefix", "&7[&aGlobal&7]");
        config.addDefault("chat.channels.global.permission", "bendinghub.channel.global");
        config.addDefault("chat.channels.global.format", "<prefix><displayname>: <message>");
        config.addDefault("chat.channels.global.radius", 0);
        config.addDefault("chat.channels.staff.prefix", "&7[&cStaff&7]");
        config.addDefault("chat.channels.staff.permission", "bendinghub.channel.staff");
        config.addDefault("chat.channels.staff.format", "<prefix><displayname>: <message>");
        config.addDefault("chat.channels.staff.radius", 0);
        config.addDefault("chat.channels.town.prefix", "&7[&bTown&7]");
        config.addDefault("chat.channels.town.permission", "bendinghub.channel.town");
        config.addDefault("chat.channels.town.format", "<prefix><displayname>: <message>");
        config.addDefault("chat.channels.town.radius", 0);
        config.addDefault("chat.channels.nation.prefix", "&7[&eNation&7]");
        config.addDefault("chat.channels.nation.permission", "bendinghub.channel.nation");
        config.addDefault("chat.channels.nation.format", "<prefix><displayname>: <message>");
        config.addDefault("chat.channels.nation.radius", 0);
        config.addDefault("chat.channels.local.prefix", "&7[&6Local&7]");
        config.addDefault("chat.channels.local.permission", "bendinghub.channel.local");
        config.addDefault("chat.channels.local.format", "<prefix><displayname>: <message>");
        config.addDefault("chat.channels.local.radius", 0);
        config.addDefault("chat.channels.nearby.prefix", "&7[&dNearby&7]");
        config.addDefault("chat.channels.nearby.permission", "bendinghub.channel.nearby");
        config.addDefault("chat.channels.nearby.format", "<prefix><displayname>: <message>");
        config.addDefault("chat.channels.nearby.radius", 256);
        config.addDefault("chat.chatcolor.blacklist", Arrays.asList("&k", "<obf>"));
        config.options().copyDefaults(true);

        try {
            config.save(configFile);
        } catch (IOException exception) {
            Bendinghub.log.warning("Failed to save config.yml defaults: " + exception.getMessage());
        }

        Bendinghub.debug("Config loaded");
    }

    public void reload(){
        load();
        if (Bendinghub.chatManager != null) {
            Bendinghub.chatManager.getChannelManager().reloadChannels();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public ConfigurationSection getChannels() {
        return config.getConfigurationSection("chat.channels");
    }

    public boolean isChatEnabled() {
        if (config == null) return true;
        return config.getBoolean("chat.enabled", true);
    }

    public int getClearChatLines(){
        if (config == null) return 100;
        return config.getInt("chat.clearchat.linelength", 100);
    }

    public ConfigurationSection getClearChatServers(){
        return config.getConfigurationSection("chat.clearchat.servers");
    }
}
