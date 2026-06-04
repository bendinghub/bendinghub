package me.unprankable.bendinghub.chat;

import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {
    private final ChannelManager channelManager;
    private final ChatColorManager chatColorManager;
    public static ConcurrentHashMap<String, String> formats = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> colors = new ConcurrentHashMap<>();
    private final ChatListener chatListener;
    private final ChatPluginMessageListener chatPluginMessageListener;

    public ChatManager() {
        this.channelManager = new ChannelManager();
        this.chatColorManager = new ChatColorManager();
        this.chatListener = new ChatListener();
        this.chatPluginMessageListener = new ChatPluginMessageListener();
        buildFormatAndColorCodes();

        // Register the chat listener
        Bukkit.getPluginManager().registerEvents(chatListener, Bendinghub.plugin);

        if (Bendinghub.configManager != null && Bendinghub.configManager.getConfig().getBoolean("chat.proxy.enabled", true)) {
            String forwardSubchannel = MessageDataObject.BENDINGHUB_CHAT;
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

    private static void buildFormatAndColorCodes(){
        formats.put("bold", "<bold>");
        formats.put("underline", "<underline>");
        formats.put("italic", "<italic>");
        formats.put("strikethrough", "<strikethrough>");
        formats.put("obfuscated", "<obf>");

        colors.put("black", "<black>");
        colors.put("dark_blue", "<dark_blue>");
        colors.put("dark_green", "<dark_green>");
        colors.put("dark_aqua", "<dark_aqua>");
        colors.put("dark_red", "<dark_red>");
        colors.put("dark_purple", "<dark_purple>");
        colors.put("gold", "<gold>");
        colors.put("gray", "<gray>");
        colors.put("dark_gray", "<dark_gray>");
        colors.put("blue", "<blue>");
        colors.put("green", "<green>");
        colors.put("aqua", "<aqua>");
        colors.put("red", "<red>");
        colors.put("light_purple", "<light_purple>");
        colors.put("yellow", "<yellow>");
        colors.put("white", "<white>");
        colors.put("rainbow", "<rainbow>");
        colors.put("gradient", "<gradient:.*>");
        colors.put("hex", "<#([A-Fa-f0-9]{6})>");
    }

    public static List<String> hasColors(String input){//if list is empty then it has no colors
        List<String> colorList = new ArrayList<>();
        for(String color : colors.keySet()){
            if(input.toLowerCase().matches(".*" + colors.get(color).toLowerCase() + ".*") || input.toLowerCase().contains(colors.get(color).toLowerCase())){
                colorList.add(color);
            }
        }
        return colorList;
    }

    public static List<String> hasFormats(String input){//if list is empty then it has no formats
        List<String> formatList = new ArrayList<>();
        for(String format : formats.keySet()){
            if(input.toLowerCase().matches(".*" + formats.get(format).toLowerCase() + ".*") || input.toLowerCase().contains(formats.get(format).toLowerCase())){
                formatList.add(format);
            }
        }
        return formatList;
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
