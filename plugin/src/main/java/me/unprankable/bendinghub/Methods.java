package me.unprankable.bendinghub;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import me.unprankable.bendinghub.chat.ChatManager;
import me.unprankable.bendinghub.hooks.PlaceholderAPIHook;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getCommandMap;

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

    public static Component convertMiniMessageStringToComponent(String minimessageString){
        String converted = Bendinghub.chatManager.convertLegacyToMiniMessage(minimessageString);
        Component deserialized = mm.deserialize(converted);
        return deserialized;
    }

    public static boolean doesChannelExist(String mcChannelName) {
        // Returns the JDA TextChannel if configured, or null if it doesn't exist
        TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(mcChannelName);
        return channel != null;
    }

    public static void registerAlias(String alias, String mainCommand, String subCommand) {
        getCommandMap().register(Bendinghub.plugin.getName(), new BukkitCommand(alias) {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                Bukkit.dispatchCommand(sender, mainCommand + " " + subCommand + " " + String.join(" ", args));
                return true;
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String aliasLabel, String[] args) throws IllegalArgumentException {
                // Build the string representation of what the target command looks like so far
                String fullCommandWithArgs = mainCommand + " " + subCommand + " " + String.join(" ", args);

                // Fetch suggestions directly from Minecraft's main command engine
                List<String> completions = Bukkit.getCommandMap().tabComplete(sender, fullCommandWithArgs);
                if (completions == null){
                    completions = List.of();
                }
                return completions;
            }
        });
    }
    public static String filter(Player player, String message){
        List<String> blacklisted = Bendinghub.configManager.getConfig().getStringList("chat.chatcolor.blacklist");
        for (String blacklist: blacklisted){
            message = message.replace(blacklist,"*".repeat(blacklist.length()));
        }

        List<String> inputColors = ChatManager.hasColors(message);
        List<String> inputFormats = ChatManager.hasFormats(message);

        if (!inputColors.isEmpty()) {
            for (String color : inputColors) {
                if (!player.hasPermission("bendinghub.chat.color." + color.toLowerCase())) {
                    Methods.sendPlayerMessage(player,"<red>You do not have permission to use the " + color + " chat color.");
                    message = message.replaceAll(ChatManager.colors.get(color), "");
                }
            }
        }

        if (!inputFormats.isEmpty()) {
            for (String format : inputFormats) {
                if (!player.hasPermission("bendinghub.chat.format." + format.toLowerCase())) {
                    Methods.sendPlayerMessage(player,"<red>You do not have permission to use the " + format + " chat format.");
                    message = message.replaceAll(ChatManager.formats.get(format), "");
                }
            }
        }
        return message;
    }
}
