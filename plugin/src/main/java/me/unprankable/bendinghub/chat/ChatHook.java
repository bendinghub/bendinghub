package me.unprankable.bendinghub.chat;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHook {
    private static ConcurrentHashMap<JavaPlugin, ChatHook> hooks = new ConcurrentHashMap<>();
//    public ChatChannelHook(String name, ChatChannel channel) {
//        this.name = name;
//        this.channel = channel;
//        hooks.put(name.toLowerCase(), this);
//    }

    public static ConcurrentHashMap<JavaPlugin, ChatHook> getHooks() {
        return hooks;
    }

    public boolean canView(Player viewer, Player sender, ChatChannel channel) {
        return channel != null;
    }

    public boolean canSend(Player sender, ChatChannel channel) {
        return true;
    }

    public void register(JavaPlugin plugin) {
        hooks.put(plugin, this);
    }
}
