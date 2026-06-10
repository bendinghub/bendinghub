package me.unprankable.bendinghub.tab;

import com.mojang.authlib.GameProfile;
import me.unprankable.bendinghub.Bendinghub;
import me.unprankable.bendinghub.Methods;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class FakePlayer {

    public void updateNetworkPlayerSlot(UUID uuid, String name, String formatStr) {
        Player targetLocalPlayer = Bukkit.getPlayer(uuid);
        Bendinghub.debug("updateNetworkPlayerSlot ran");
        if (targetLocalPlayer == null) {
            sendFakePlayerTabPacket(uuid, name, formatStr);
            return;
        }

        targetLocalPlayer.playerListName(Methods.convertMiniMessageStringToComponent(formatStr));
    }

    public void removeNetworkPlayerSlot(UUID uuid, String name) {
        try {
            ClientboundPlayerInfoRemovePacket packet = new ClientboundPlayerInfoRemovePacket(
                    List.of(uuid)
            );
            broadcastPacket(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFakePlayerTabPacket(UUID uuid, String name, String formatStr) {
        try {
            Bendinghub.debug("sendFakePlayerTabPacket ran");
            Component adventureComponent = Methods.convertMiniMessageStringToComponent(formatStr);

            // GameProfile mapping for 1.21.11 Mojang Authlib
            GameProfile gameProfile = new GameProfile(uuid, name != null ? name : "");

            // 1. Convert the Kyori Adventure Component to a raw JSON string
            String json = GsonComponentSerializer.gson().serialize(adventureComponent);

            // 2. 1.21.11 COMPONENT SERIALIZATION FIX:
            // Instead of using the old nested 'Serializer' class, we reflect into modern Mojang Codecs.
            // This reads the raw JSON string directly into a net.minecraft.network.chat.Component via GsonComponentSerializer.
            Class<?> componentClass = net.minecraft.network.chat.Component.class;

            // Look up the modern 1.21.11 JSON parser method: Component.Serializer.fromJson(JsonElement, RegistryAccess) via reflection fallbacks
            Object nmsComponent = null;
            try {
                // Try modern 1.21.11 parsing route using an empty RegistryAccess parameter context
                Class<?> serializerClass = Class.forName("net.minecraft.network.chat.Component$Serializer");
                Method fromJsonMethod = serializerClass.getMethod("fromJson", String.class, net.minecraft.core.RegistryAccess.class);
                nmsComponent = fromJsonMethod.invoke(null, json, net.minecraft.core.RegistryAccess.EMPTY);
            } catch (Exception ex) {
                // Fallback approach utilizing internal Paper/Spigot Chat Component JSON parsing adapters if mapped differently
                for (Method method : componentClass.getMethods()) {
                    if (method.getName().equals("stringToComponent") || method.getName().equals("fromJson")) {
                        if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == String.class) {
                            nmsComponent = method.invoke(null, json);
                            break;
                        }
                    }
                }
            }

            if (nmsComponent == null) {
                Bendinghub.log.warning("Could not parse chat component JSON for fake player tab slot.");
                return;
            }

            // 3. Reflect into 1.21.11 inner packet entry structure to bypass private buffer restrictions:
            // ClientboundPlayerInfoUpdatePacket$Entry
            Class<?> packetClass = ClientboundPlayerInfoUpdatePacket.class;
            Class<?> entryClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry");
            Class<?> gameTypeClass = net.minecraft.world.level.GameType.class;

            // Target Constructor layout: Entry(UUID uuid, GameProfile profile, boolean listed, int latency, GameType gameMode, Component displayName)
            Constructor<?> entryConstructor = entryClass.getDeclaredConstructor(
                    UUID.class,
                    GameProfile.class,
                    boolean.class,
                    int.class,
                    gameTypeClass,
                    componentClass
            );
            entryConstructor.setAccessible(true);

            // Grab the native Survival gamemode enum object context safely
            // Cleaner reflection approach:
            Object survivalMode = java.util.Arrays.stream(gameTypeClass.getEnumConstants())
                    .filter(e -> e.toString().equalsIgnoreCase("SURVIVAL"))
                    .findFirst()
                    .orElse(null);

            // Construct our custom single-player list row entry mapping
            Object playerEntry = entryConstructor.newInstance(
                    uuid,
                    gameProfile,
                    true,         // listed in tab
                    42,           // latency ping bars
                    survivalMode, // game mode
                    nmsComponent  // custom display name component
            );

            // Define modern packet execution actions
            EnumSet<?> actionsSet = EnumSet.of(
                    ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
            );

            // Initialize the packet using the open public Collection constructor (bypasses private RegistryFriendlyByteBuf limitations!)
            Constructor<?> packetConstructor = packetClass.getConstructor(EnumSet.class, java.util.Collection.class);
            Object packet = packetConstructor.newInstance(actionsSet, Collections.singletonList(playerEntry));

            broadcastPacket((net.minecraft.network.protocol.Packet<?>) packet);
        } catch (Exception e) {
            Bendinghub.log.log(Level.SEVERE, "1.21.11 Packet Fabrication Error: ", e);
        }
    }

    private void broadcastPacket(net.minecraft.network.protocol.Packet<?> packet) {
        for (Player localPlayer : Bukkit.getOnlinePlayers()) {
            try {
                // Safe version-independent reflection lookup to pull out CraftPlayer cleanly on 1.21.11
                String cbPackage = Bukkit.getServer().getClass().getPackage().getName();
                Class<?> craftPlayerClass = Class.forName(cbPackage + ".entity.CraftPlayer");

                if (craftPlayerClass.isInstance(localPlayer)) {
                    Object craftPlayerInstance = craftPlayerClass.cast(localPlayer);
                    Object serverPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayerInstance);

                    // Access network connection wrapper
                    Object connection = serverPlayer.getClass().getField("connection").get(serverPlayer);

                    // Route packet through whichever runtime mapping method signature is active inside your jar
                    try {
                        Method sendMethod = connection.getClass().getMethod("send", net.minecraft.network.protocol.Packet.class);
                        sendMethod.invoke(connection, packet);
                    } catch (NoSuchMethodException e) {
                        Method sendPacketMethod = connection.getClass().getMethod("sendPacket", net.minecraft.network.protocol.Packet.class);
                        sendPacketMethod.invoke(connection, packet);
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}