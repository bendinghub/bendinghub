package me.unprankable.bendinghub.hooks;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.unprankable.bendinghub.Bendinghub;
import org.bukkit.entity.Player;

public class TownyHook {
    public static boolean isTownyEnabled() {
        return Bendinghub.plugin.getServer().getPluginManager().isPluginEnabled("Towny");
    }
    public static Town getPlayersTown(Player player){
        if (!isTownyEnabled()) {
            Bendinghub.debug("Towny is not enabled, cannot get player's town.");
            return null;
        }
        Resident resident = TownyAPI.getInstance().getResident(player);
        if(resident.hasTown()){
            return resident.getTownOrNull();
        }
        return null;
    }

    public static boolean isInTown(Player player){
        if(!isTownyEnabled()) {
            Bendinghub.debug("Towny is not enabled, cannot check if player is in a town.");
            return false;
        }
        Resident resident = TownyAPI.getInstance().getResident(player);
        return resident.hasTown();
    }

    public static boolean areInSameTown(Player playerOne, Player playerTwo) {
        if(!isTownyEnabled()) {
            Bendinghub.debug("Towny is not enabled, cannot check if players are in the same town.");
            return false;
        }
        Town townOne = getPlayersTown(playerOne);
        Town townTwo = getPlayersTown(playerTwo);
        if(townOne == null || townTwo == null) return false;
        return townOne.getName().equals(townTwo.getName());
    }

    public static Nation getPlayersNation(Player player){
        if(!isTownyEnabled()) {
            Bendinghub.debug("Towny is not enabled, cannot get player's nation.");
            return null;
        }
        Resident resident = TownyAPI.getInstance().getResident(player);
        if(resident.hasTown()){
            Town town = resident.getTownOrNull();
            if(town.hasNation()){
                return town.getNationOrNull();
            }
        }
        return null;
    }

    public static boolean isInNation(Player player){
        if(!isTownyEnabled()) {
            Bendinghub.debug("Towny is not enabled, cannot check if player is in a nation.");
            return false;
        }
        Resident resident = TownyAPI.getInstance().getResident(player);
        if(resident.hasTown()){
            Town town = resident.getTownOrNull();
            return town.hasNation();
        }
        return false;
    }

    public static boolean areInSameNation(Player playerOne, Player playerTwo) {
        if(!isTownyEnabled()) {
            Bendinghub.debug("Towny is not enabled, cannot check if players are in the same nation.");
            return false;
        }
        Nation nationOne = getPlayersNation(playerOne);
        Nation nationTwo = getPlayersNation(playerTwo);
        if(nationOne == null || nationTwo == null) return false;
        return nationOne.getName().equals(nationTwo.getName());
    }
}
