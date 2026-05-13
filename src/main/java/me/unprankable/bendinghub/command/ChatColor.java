package me.unprankable.bendinghub.command;

import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.chat.ChatListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatColor {
        private static final MiniMessage mm = MiniMessage.miniMessage();
    public static boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            sender.sendMessage("Usage: /chatcolor <color|reset>");
            return true;
        }
        String input = args[1];
        if (input.equals("reset")) {
            Bendinghub.chatManager.getChatColorManager().setPlayerChatColor(player.getUniqueId(), null);
            sender.sendMessage("Chat color reset to default.");
            return true;
        }
        Bendinghub.chatManager.getChatColorManager().setPlayerChatColor(player.getUniqueId(), input);
        String message = "Chat color set to: " + input + "Example";
        message = ChatListener.convertLegacyToMiniMessage(message);
        sender.sendMessage(mm.deserialize(message));
        return true;
    }

    public static List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2) {
            String input = args[1] == null ? "" : args[1].toLowerCase();
            List<String> colors = new ArrayList<>();
            colors.add("<red>>");
            colors.add("<green>");
            colors.add("<blue>");
            colors.add("<yellow>");
            colors.add("<aqua>");
            colors.add("<purple>");
            colors.add("<white>");
            colors.add("<black>");
            colors.add("reset");
            colors.add("none");
            return colors;
        }
        return Collections.emptyList();
    }
}

