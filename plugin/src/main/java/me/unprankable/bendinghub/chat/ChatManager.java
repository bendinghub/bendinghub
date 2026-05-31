package me.unprankable.bendinghub.chat;

import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.Bukkit;

public class ChatManager {
    private final ChannelManager channelManager;
    private final ChatColorManager chatColorManager;
    private final ChatListener chatListener;
    private final ChatPluginMessageListener chatPluginMessageListener;

    public ChatManager() {
        this.channelManager = new ChannelManager();
        this.chatColorManager = new ChatColorManager();
        this.chatListener = new ChatListener();
        this.chatPluginMessageListener = new ChatPluginMessageListener();

        // Register the chat listener
        Bukkit.getPluginManager().registerEvents(chatListener, Bendinghub.plugin);

        if (Bendinghub.configManager != null && Bendinghub.configManager.getConfig().getBoolean("chat.proxy.enabled", true)) {
            String forwardSubchannel = Bendinghub.configManager.getConfig().getString("chat.proxy.forward-subchannel", "bendinghub:chat");
            Bukkit.getMessenger().registerOutgoingPluginChannel(Bendinghub.plugin, forwardSubchannel);
            Bukkit.getMessenger().registerIncomingPluginChannel(Bendinghub.plugin, forwardSubchannel, chatPluginMessageListener);
        }

        Bendinghub.log.info("ChatManager initialized and ChatListener registered.");
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

    public ChatPluginMessageListener getChatPluginMessageListener() {
        return chatPluginMessageListener;
    }


    public ChatManager getInstance() {
        return this;
    }


    public static String convertLegacyToMiniMessage(String text) {
        if (text == null) return "";
        //Convert hex codes in the format &#RRGGBB or &x&R&R&G&B&B to <#RRGGBB>
        text = text.replaceAll("(?i)(&|§)x(&|§)([A-Fa-f0-9])(&|§)([A-Fa-f0-9])(&|§)([A-Fa-f0-9])(&|§)([A-Fa-f0-9])(&|§)([A-Fa-f0-9])(&|§)([A-Fa-f0-9])", "<#$3$5$7$9$11$13>");
        text = text.replaceAll("(?i)(&|§)(#([A-Fa-f0-9]{6}))", "<$2>");
        // 2. Standard Color Codes
        text = text.replaceAll("(?i)(&|§)0", "<black>");
        text = text.replaceAll("(?i)(&|§)1", "<dark_blue>");
        text = text.replaceAll("(?i)(&|§)2", "<dark_green>");
        text = text.replaceAll("(?i)(&|§)3", "<dark_aqua>");
        text = text.replaceAll("(?i)(&|§)4", "<dark_red>");
        text = text.replaceAll("(?i)(&|§)5", "<dark_purple>");
        text = text.replaceAll("(?i)(&|§)6", "<gold>");
        text = text.replaceAll("(?i)(&|§)7", "<gray>");
        text = text.replaceAll("(?i)(&|§)8", "<dark_gray>");
        text = text.replaceAll("(?i)(&|§)9", "<blue>");
        text = text.replaceAll("(?i)(&|§)a", "<green>");
        text = text.replaceAll("(?i)(&|§)b", "<aqua>");
        text = text.replaceAll("(?i)(&|§)c", "<red>");
        text = text.replaceAll("(?i)(&|§)d", "<light_purple>");
        text = text.replaceAll("(?i)(&|§)e", "<yellow>");
        text = text.replaceAll("(?i)(&|§)f", "<white>");
        // 3. Formatting & Styles
        text = text.replaceAll("(?i)(&|§)l", "<bold>");
        text = text.replaceAll("(?i)(&|§)m", "<strikethrough>");
        text = text.replaceAll("(?i)(&|§)n", "<underline>");
        text = text.replaceAll("(?i)(&|§)o", "<italic>");
        text = text.replaceAll("(?i)(&|§)k", "<obf>");
        text = text.replaceAll("(?i)(&|§)r", "<reset>");
        return text;
    }
}
