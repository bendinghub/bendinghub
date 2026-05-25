package me.unprankable.bendinghub.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BendinghubExpansion extends PlaceholderExpansion {
    public BendinghubExpansion() {

    }

    @Override
    public @NotNull String getIdentifier() {
        return "bendinghub";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", Bendinghub.plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return Bendinghub.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        String placeholder = getIdentifier() + "_" + params;
        Bendinghub.debug("Placeholder requested:" + placeholder);
        String value;
        switch (params){
            case "chatcolor":
                value = Bendinghub.plugin.chatManager.getChatColorManager().getPlayerChatColor(player.getUniqueId());
                break;
            case "channel":
                value = Bendinghub.plugin.chatManager.getChannelManager().getPlayerChannel(player.getUniqueId()).getId();
                break;
            default:
                value = null;
                break;
        }
        Bendinghub.debug("Placeholder: " + placeholder + "value is " + value);
        return value;
    }
}

