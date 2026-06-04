package me.unprankable.bendinghub.chat;

import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import me.unprankable.bendinghub.hooks.PlaceholderAPIHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.List;
import java.util.Optional;

public class ChatChannel {
    private final String id;
    private final String prefix;
    private final String permission;
    private final String format;
    private final double radius; // For local chat, 0 means no distance limit

    public ChatChannel(String id, String prefix, String permission, String format, double radius) {
        this.id = id;
        this.prefix = prefix;
        this.permission = permission;
        this.format = format;
        this.radius = radius;
    }
    public ChatChannel(String id, String prefix, String permission, String format) {
        this(id, prefix, permission, format, 0);
    }

    public String getId() {
        return id;
    }

    public String getPrefix() {
        return prefix;
    }
    public String getPermission() {
        return permission;
    }
    public String getFormat() {
        return format;
    }
    public double getRadius() {
        return radius;
    }
    public String fillInFormatValues(Player player) {
        return PlaceholderAPIHook.parsePlaceholders(player, this.format.replace("<prefix>", this.getPrefix()));
    }
    public String setMessage(Player sender, Player viewer,String format ,String message){
        message = ChatManager.convertLegacyToMiniMessage(message);
        message = Methods.filter(sender, message);

        if(sender.hasPermission("bendinghub.chat.placeholders")){//convert legacy again just incase a placeholder has a legacy code
            return ChatManager.convertLegacyToMiniMessage(PlaceholderAPIHook.parsePlaceholders(viewer, format.replace("<message>",message)));
        } else {
            return format.replace("<message>",message);
        }
    }

    public boolean canView(Player viewer, Player sender) {
        if (this.permission != null && !viewer.hasPermission(this.permission)) return false;

        if ((this.radius > 0) && (!viewer.getWorld().equals(sender.getWorld()) || viewer.getLocation().distance(sender.getLocation()) > this.radius)) return false;

        for (JavaPlugin plugin : ChatHook.getHooks().keySet()) {
            ChatHook hook = ChatHook.getHooks().get(plugin);
            try {
                return hook.canView(viewer, sender, this);
            } catch (Exception e){
                Bendinghub.log.severe("Error in chat hook from plugin " + plugin.getName() + ".");
                e.printStackTrace();
            }
        }
        return true;
    }
    public boolean canSend(Player sender) {
        if (this.permission != null && !sender.hasPermission(this.permission)) return false;

        //check hooks
        for (JavaPlugin plugin : ChatHook.getHooks().keySet()) {
            ChatHook hook = ChatHook.getHooks().get(plugin);
            try {
                return hook.canSend(sender, this);
            } catch (Exception e){
                Bendinghub.log.severe("Error in chat hook from plugin " + plugin.getName() + ".");
                e.printStackTrace();
            }
        }
        return true;
    }

}
