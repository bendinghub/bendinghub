package me.unprankable.bendinghub.hooks;

import me.unprankable.bendinghub.chat.ChatChannel;
import me.unprankable.bendinghub.chat.ChatHook;
import org.bukkit.entity.Player;

public class TownyChatHook extends ChatHook {
    @Override
    public boolean canView(Player viewer, Player sender, ChatChannel channel) {
        String channelId = channel.getId();
        switch(channelId){
            case "town":
                return TownyHook.areInSameTown(viewer, sender);
            case "nation":
                return TownyHook.areInSameNation(viewer, sender);
            default:
                return true;
        }
    }

    @Override
    public boolean canSend(Player sender, ChatChannel channel) {
        String channelId = channel.getId();
        switch(channelId){
            case "town":
                return TownyHook.isInTown(sender);
            case "nation":
                return TownyHook.isInNation(sender);
            default:
                return true;
        }
    }
}
