package me.unprankable.bendinghub.tab;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.mojang.authlib.GameProfile;
import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class FakePlayer {

    public void updateNetworkPlayerSlot(UUID uuid, String name, String formatStr) {
        Player targetLocalPlayer = Bukkit.getPlayer(uuid);
        if (targetLocalPlayer == null) {
            sendFakePlayerTabPacket(uuid, name, formatStr);
            return;
        }
        targetLocalPlayer.playerListName(Methods.convertMiniMessageStringToComponent(formatStr));
    }

    public void removeNetworkPlayerSlot(UUID uuid, String name) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);

        // Use getUUIDLists() instead of getLists(0)
        packet.getUUIDLists().write(0, List.of(uuid));

        broadcastPacket(packet);
    }

    public void sendFakePlayerTabPacket(UUID uuid, String name, String formatStr) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);

        // 1. Set the Actions
        packet.getPlayerInfoActions().write(0, EnumSet.of(
                EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_LISTED,
                EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME
        ));

        // 2. IMPORTANT: Create the list FIRST
        List<PlayerInfoData> dataList = new ArrayList<>();

        // 3. Create the data object using the proper wrapper
        // Since we are not using the constructor that triggers 'getId()',
        // we use the 'fromHandle' or simple wrapper methods.
        WrappedGameProfile profile = new WrappedGameProfile(uuid, name);
        WrappedChatComponent chat = WrappedChatComponent.fromJson(
                GsonComponentSerializer.gson().serialize(Methods.convertMiniMessageStringToComponent(formatStr))
        );

        PlayerInfoData data = new PlayerInfoData(profile, 42, EnumWrappers.NativeGameMode.SURVIVAL, chat);
        dataList.add(data);

        // 4. Inject the list into the packet
        // Because we are setting the entire list, we aren't writing to "index 0" of a non-existent list
        packet.getPlayerInfoDataLists().write(0, dataList);

        broadcastPacket(packet);
    }

    private void broadcastPacket(PacketContainer packet) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (Exception e) {
                Bendinghub.log.warning("Failed to send tab packet: " + e.getMessage());
            }
        }
    }
}