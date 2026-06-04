package me.unprankable.bendinghub.command;

import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import me.unprankable.bendinghub.chat.ChatManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatColor extends BendinghubCommand{

    private static final MiniMessage mm = MiniMessage.miniMessage();






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


        String input;
        if (args.length <= 1){
            input = "off";
        } else {
            input = Bendinghub.chatManager.convertLegacyToMiniMessage(args[1]);
        }
        Player player;
        if (args.length <= 2){
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

        input = Methods.filter(player, input);

        Bendinghub.chatManager.getChatColorManager().setPlayerChatColor(player.getUniqueId(), input);
        Methods.sendPlayerMessage(player,"<green>Chat color set to: " + input + "Example");
        if (!player.getUniqueId().toString().equals(((Player) sender).getUniqueId().toString())){
            Methods.sendPlayerMessage((Player) sender, "<green>Set chat color to " + input + "Example<green> For: <yellow>" + player.getName() + "<reset>");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        switch(args.length){
            case 2:
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
            case 3:
                if(sender.hasPermission("bendinghub.command.chatcolor.others")){
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                } else {
                    return List.of();
                }
            default:
                return List.of();
        }
    }
}

