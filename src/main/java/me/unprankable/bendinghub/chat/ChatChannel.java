package me.unprankable.bendinghub.chat;

import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.entity.Player;

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
    public String fillInFormatValues(Player player, String message) {
        String text = this.format.replace("<prefix>", this.getPrefix())
                .replace("<displayname>", player.getName())
                .replace("<message>", message);

        // Soft PlaceholderAPI support: if PlaceholderAPI is installed, use it to replace placeholders
        try {
            if (player.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                // Use reflection so PlaceholderAPI is an optional dependency at compile time
                Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                java.lang.reflect.Method setPlaceholders = papiClass.getMethod("setPlaceholders", org.bukkit.OfflinePlayer.class, String.class);
                Object result = setPlaceholders.invoke(null, player, text);
                if (result instanceof String) {
                    text = (String) result;
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {
            // Log a warning once if something goes wrong applying placeholders
            Bendinghub.getPlugin(Bendinghub.class).getLogger().fine("PAPI placeholder replacement failed: " + ex.getMessage());
        }

        return text;
    }

}
