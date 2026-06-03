package me.unprankable.bendinghub.command;

import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import me.unprankable.bendinghub.chat.ChatListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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
import java.util.concurrent.ConcurrentHashMap;

public class ChatColor extends BendinghubCommand{
    public static ConcurrentHashMap<String, String> formats = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> colors = new ConcurrentHashMap<>();
    private static final MiniMessage mm = MiniMessage.miniMessage();

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


    @Override
    public String getAuthor(){
        return "Unprankable";
    }

    @Override
    public String getName(){
        return "chatcolor";
    }

    @Override
    public boolean onlyPlayers(){
        return true;
    }

    @Override
    public String getUsage(){
        return "/bh chatcolor <color>";
    }

    @Override
    public List<String> getAliases(){
        return List.of("cc", "color");
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (Bendinghub.configManager == null || !Bendinghub.configManager.isChatEnabled() || Bendinghub.chatManager == null) {
            sender.sendMessage("Chat is disabled on this server.");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        buildFormatAndColorCodes();

        String input;
        if (args.length == 1){
            input = "off";
        } else {
            input = Bendinghub.chatManager.convertLegacyToMiniMessage(args[1]);
        }
        Player player;
        if (args.length == 2){
            player = (Player) sender;
        } else {
            if (sender.hasPermission("bendinghub.command.chatcolor.others")) {
                player = Bukkit.getPlayer(args[2]);
            } else {
                player = (Player) sender;
            }
        }
        if (input.equals("reset") || input.equals("none") || input.equals("clear") || input.equals("off")) {
            Bendinghub.chatManager.getChatColorManager().setPlayerChatColor(player.getUniqueId(), null);
            Methods.sendPlayerMessage(player,"Chat color reset to default.");
            return true;
        }
        List<String> blacklist = Bendinghub.configManager.getConfig().getStringList("chat.chatcolor.blacklist");
        if (blacklist.contains(input.toLowerCase())) {
            Methods.sendPlayerMessage(player,"<red>That chat color is not allowed.");
            return true;
        }
        List<String> inputColors = hasColors(input);
        List<String> inputFormats = hasFormats(input);

        if (!inputColors.isEmpty()) {
            for (String color : inputColors) {
                if (!player.hasPermission("bendinghub.chat.color." + color.toLowerCase())) {
                    Methods.sendPlayerMessage(player,"<red>You do not have permission to use the " + color + " chat color.");
                    input = input.replaceAll(colors.get(color), "");
                }
            }
        }

        if (!inputFormats.isEmpty()) {
            for (String format : inputFormats) {
                if (!player.hasPermission("bendinghub.chat.format." + format.toLowerCase())) {
                    Methods.sendPlayerMessage(player,"<red>You do not have permission to use the " + format + " chat format.");
                    input = input.replaceAll(formats.get(format), "");
                }
            }
        }

        Bendinghub.chatManager.getChatColorManager().setPlayerChatColor(player.getUniqueId(), input);
        Methods.sendPlayerMessage(player,"<green>Chat color set to: " + input + "Example");
        if (!player.getUniqueId().toString().equals(((Player) sender).getUniqueId().toString())){
            Methods.sendPlayerMessage((Player) sender, "<green>Set chat color to " + input + "Example<green> For: <yellow>" + player.getName() + "<reset>");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggest = new ArrayList<>();

        suggest.add("<black>");                  suggest.add("&0");
        suggest.add("<dark_blue>");              suggest.add("&1");
        suggest.add("<dark_green>");             suggest.add("&2");
        suggest.add("<dark_aqua>");              suggest.add("&3");
        suggest.add("<dark_red>");               suggest.add("&4");
        suggest.add("<dark_purple>");            suggest.add("&5");
        suggest.add("<gold>");                   suggest.add("&6");
        suggest.add("<gray>");                   suggest.add("&7");
        suggest.add("<dark_gray>");              suggest.add("&8");
        suggest.add("<blue>");                   suggest.add("&9");
        suggest.add("<green>");                  suggest.add("&a");
        suggest.add("<aqua>");                   suggest.add("&b");
        suggest.add("<red>>");                   suggest.add("&c");
        suggest.add("<light_purple>");           suggest.add("&d");
        suggest.add("<yellow>");                 suggest.add("&e");
        suggest.add("<white>");                  suggest.add("&f");
        suggest.add("<bold>");                   suggest.add("&l");
        suggest.add("<underline>");              suggest.add("&n");
        suggest.add("<italic>");                 suggest.add("&o");
        suggest.add("<strikethrough>");          suggest.add("&m");
        suggest.add("<obf>");                     suggest.add("&k");
        suggest.add("<reset>");                  suggest.add("&r");
        suggest.add("reset");                    suggest.add("none");
        suggest.add("clear");                    suggest.add("off");
        suggest.add("<rainbow>");                suggest.add("<#RRGGBB>");
        suggest.add("<gradient:color1,color2>"); suggest.add("<gradient:#RRGGBB:#RRGGBB>");
        return suggest;
    }
}

