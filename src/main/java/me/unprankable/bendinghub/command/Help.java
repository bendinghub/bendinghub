package me.unprankable.bendinghub.command;

public class Help {
    public static boolean execute(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        sender.sendMessage("Bendinghub Commands:");
        sender.sendMessage("/bendinghub reload - Reload the plugin configuration (Admin only)");
        sender.sendMessage("/bendinghub channel <channel> - Switch your active chat channel");
        sender.sendMessage("/chatcolor <color|reset> - Set your chat message color");
        return true;
    }

    public static java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        return java.util.Collections.emptyList();
    }
}
