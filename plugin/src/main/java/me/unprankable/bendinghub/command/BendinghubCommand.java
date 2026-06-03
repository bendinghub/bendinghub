package me.unprankable.bendinghub.command;

import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BendinghubCommand {
    // Example Command
    private static final ConcurrentHashMap<String, BendinghubCommand> Commands = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> aliases = new ConcurrentHashMap<>();

    public BendinghubCommand(){
        Commands.put(getName(), this);
        if (!getAliases().isEmpty()){
            for (String alias: getAliases()){
                aliases.put(alias, getName());
            }
        }
        Methods.registerAlias(getName(), Bendinghub.plugin.getName(), getName());
    }

    public static List<String> getCommandNames(){
        return Commands.keySet().stream().toList();
    }

    public static BendinghubCommand getCommand(String name){
        if(Commands.containsKey(name)){
            return Commands.get(name);
        } else {
            String canonical = aliases.getOrDefault(name, "help");
            return Commands.get(canonical);
        }
    }

    public String getName(){
        return "yourSubCommandName";
    }

    public List<String> getAliases(){
        return List.of();
    }

    public String getAuthor(){
        return "Author";
    }

    public String helpMessage(){
        String message = "<green>Bendinghub Command Help Menu\n";
        message += "<blue>Command</blue>: <yellow>" + getName() + "</yellow>.\n";
        message += "<blue>Author</blue>: <yellow>" + getAuthor() + "</yellow>.\n";
        message += "<blue>Permission</blue>: <yellow>" + getPermission() + "</yellow>.\n";
        message += "<blue>Player Required</blue>: <yellow>" + onlyPlayers() + "</yellow>.\n";
        message += "<blue>Usage</blue>: <yellow>" + getUsage() + "</yellow>.\n";
        message += "If you need help with this command you can ask staff</green>";
        return message;
    }

    public String getPermission(){
        return "bendinghub.command." + getName();
    }

    public boolean onlyPlayers(){
        return false;
    }

    public String getUsage(){
        return "/bh " + getName();
    }

    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        /* Your code block here */
        return true;
    }

    public List<String> tabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        /* Your code block here */
        return List.of();
    }
}
