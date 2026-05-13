package me.unprankable.bendinghub.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BendinghubExpansion extends PlaceholderExpansion {
    private final Bendinghub plugin;

    public BendinghubExpansion(Bendinghub plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bendinghub";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if(params.equalsIgnoreCase("chatcolor")) {
            return plugin.chatManager.getChatColorManager().getPlayerChatColor(player.getUniqueId());
        }
        return null;
    }
}

