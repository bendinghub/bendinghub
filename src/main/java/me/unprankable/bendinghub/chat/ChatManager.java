package me.unprankable.bendinghub.chat;

import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.Bukkit;

public class ChatManager {
    private final Bendinghub plugin;
    private final ChannelManager channelManager;
    private final ChatColorManager chatColorManager;
    private final ChatListener chatListener;

    public ChatManager(final Bendinghub plugin) {
        this.plugin = plugin;
        this.channelManager = new ChannelManager(plugin);
        this.chatColorManager = new ChatColorManager(plugin);
        this.chatListener = new ChatListener(this);
        
        // Register the chat listener
        Bukkit.getPluginManager().registerEvents(chatListener, plugin);
        plugin.getLogger().info("ChatManager initialized and ChatListener registered.");
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public ChatColorManager getChatColorManager() {
        return chatColorManager;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }
    
    public ChatManager getInstance() {
        return this;
    }
}
