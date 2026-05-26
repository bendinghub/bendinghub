package me.unprankable.bendinghub;

import me.unprankable.bendinghub.hooks.PlaceholderAPIHook;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Methods {
    public static MiniMessage mm = MiniMessage.miniMessage();
    public static void sendPlayerMessage(Player player, String message){
        String converted = Bendinghub.chatManager.convertLegacyToMiniMessage(message);
        String parsed = PlaceholderAPIHook.parsePlaceholders(player, converted);
        Component deserialized = mm.deserialize(parsed);
        player.sendMessage(deserialized);
    }

    public static void sendConsoleMessage(String message){
        String converted = Bendinghub.chatManager.convertLegacyToMiniMessage(message);
        Component deserialized = mm.deserialize(converted);
        Bukkit.getConsoleSender().sendMessage(deserialized);
    }
    public static String convertComponentStringToMiniMessage(String debugString) {
        // Regex matches content="..." and looks ahead for named colors, hex values, or null colors
        Pattern pattern = Pattern.compile("content=\"([^\"]*)\".*?(?:color=NamedTextColor\\{name=\"([^\"]+)\"|color=TextColorImpl\\{value=\"([^\"]+)\"|color=null)");
        Matcher matcher = pattern.matcher(debugString);

        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String content = matcher.group(1);
            String namedColor = matcher.group(2);
            String hexColor = matcher.group(3);

            if (namedColor != null) {
                sb.append("<").append(namedColor).append(">").append(content).append("</").append(namedColor).append(">");
            } else if (hexColor != null) {
                sb.append("<").append(hexColor).append(">").append(content).append("</").append(hexColor).append(">");
            } else {
                sb.append(content);
            }
        }

        return sb.toString();
    }
}
