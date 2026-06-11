package com.houdert6.bendingHub;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.houdert6.bendingHub.BendingHub.convertLegacyToMiniMessage;

public class TabManager {

    private final ProxyServer proxy = BendingHub.plugin.proxy;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private static final AtomicInteger id = new AtomicInteger(0);
    public static final int delay = 2000;
    public TabManager() {
    }

    public String getFooter(){
        List<String> footerList = ConfigManager.toml().getList("tab.footer");

        if (footerList == null || footerList.isEmpty()){
            return "";
        }

        return String.join("\n", footerList);
    }

    public String getHeader(){
        List<String> headerList = ConfigManager.toml().getList("tab.header");

        if (headerList == null || headerList.isEmpty()){
            return "";
        }

        return String.join("\n", headerList);
    }

    public String getTabNameFormat(){
        String tabNameFormat = ConfigManager.toml().getString("tab.tabnameformat");
        if (tabNameFormat == null){
            return "";
        }

        Toml placeholdersTable = ConfigManager.toml().getTable("placeholders");

        if (placeholdersTable != null){
            for (Map.Entry<String, Object> entry : placeholdersTable.toMap().entrySet()){
                String placeholderKey = entry.getKey().replace("\"", "");
                BendingHub.plugin.debug("placeholderkey" + placeholderKey);
                String replacementValue = String.valueOf(entry.getValue());
                BendingHub.plugin.debug("replacement" + replacementValue);
                BendingHub.plugin.debug("tabnameformat" + tabNameFormat);
                tabNameFormat = tabNameFormat.replace(placeholderKey, replacementValue);
                BendingHub.plugin.debug("tabnameformat" + tabNameFormat);
            }
        }

        return tabNameFormat;
    }

    // Add this method to TabManager.java to ensure all players see each other
    public void updateAllTabLists() {
        for (Player viewer : proxy.getAllPlayers()) {
            for (Player target : proxy.getAllPlayers()) {
                if (viewer.getTabList().getEntry(target.getUniqueId()).isEmpty()) {
                    TabListEntry entry = TabListEntry.builder()
                            .tabList(viewer.getTabList())
                            .profile(target.getGameProfile())
                            .displayName(Component.text(target.getUsername()))
                            .build();
                    viewer.getTabList().addEntry(entry);
                }
            }
        }
    }

    public void refreshAllTabNames() {
        for (Player target : proxy.getAllPlayers()) {
            // Stagger the updates to prevent race conditions
            proxy.getScheduler().buildTask(BendingHub.plugin, () -> {
                updateTabForPlayer(target);
            }).delay(delay / 4, TimeUnit.MILLISECONDS).schedule();
        }
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        // When someone joins, refresh everyone's lists
        proxy.getScheduler().buildTask(BendingHub.plugin, () -> {
            updateAllTabLists();
        }).delay(delay, TimeUnit.MILLISECONDS).schedule();
    }

    // 1. Send the request when a player connects to a server
    @Subscribe
    public void onServerConnect(ServerConnectedEvent event) {
        // Update placeholders for the specific player who moved
        updateTabForPlayer(event.getPlayer());

        // Ensure the tab list remains global after the move
        proxy.getScheduler().buildTask(BendingHub.plugin, () -> {
            updateAllTabLists();
        }).delay(delay / 8, TimeUnit.MILLISECONDS).schedule();
        refreshAllTabNames();
    }

    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {
        // Remove the player who left from everyone else's tab list
        Player quitPlayer = event.getPlayer();
        for (Player p : proxy.getAllPlayers()) {
            p.getTabList().removeEntry(quitPlayer.getUniqueId());
        }
    }

    /**
     * Proactively updates the tab list for a specific player by requesting
     * fresh placeholder data from their current backend server.
     */
    public void updateTabForPlayer(Player player) {
        // We ensure we only request if the player is currently connected to a server
        proxy.getScheduler().buildTask(BendingHub.plugin, () -> {
            requestPlaceholders(player, "tabnameformat", getTabNameFormat());
            requestPlaceholders(player, "header", getHeader());
            requestPlaceholders(player, "footer", getFooter());
            requestPlaceholders(player, "group", "%luckperms_primary_group_name%");
        }).delay(delay, TimeUnit.MILLISECONDS).schedule();
    }

    public void requestPlaceholders(Player player, String requesting, String placeholderText){
        // Create the request payload
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        //id of the request
        int currentId = id.getAndIncrement();

        out.writeInt(currentId);
        //action REQUEST_PLACEHOLDERS
        out.writeUTF("REQUEST_PLACEHOLDERS");
        //what your requesting header, footer, tabnameformat, group?
        out.writeUTF(requesting);
        //player to request placeholders for
        out.writeUTF(player.getUniqueId().toString());
        //placeholders to request
        out.writeUTF(placeholderText);
        //send the message
        Optional<ServerConnection> currentServer = player.getCurrentServer();
        currentServer.ifPresent(server -> server.getServer().sendPluginMessage(BendingHub.BENDINGHUB_TAB, out.toByteArray()));
        BendingHub.plugin.debug("message sent with ID: " + id);

    }

    // 2. Listen for the response from the backend server
    @Subscribe
    public void onTabPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(BendingHub.BENDINGHUB_TAB)) return;
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!(event.getSource() instanceof ServerConnection)) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        int ID = in.readInt();
        BendingHub.plugin.debug("message recieved with ID: " + ID);
        String subChannel = in.readUTF();
        String requested = in.readUTF();
        UUID playerUUID = UUID.fromString(in.readUTF());
        String response = in.readUTF();
        // Handle the incoming data
        if (subChannel.equals("RESPONSE_PLACEHOLDERS")) {

            // Find the player on the proxy and update their tab list
            proxy.getPlayer(playerUUID).ifPresent(player -> {
                // Combine the strings (Assuming your backend sends MiniMessage compatible strings like <red>)
                switch (requested){
                    case "tabnameformat":
                        String fullFormat = convertLegacyToMiniMessage(response);
                        Component tabName = mm.deserialize(fullFormat);

                        // Set the specific player's name in the tab list
                        player.getTabList().getEntry(playerUUID).ifPresent(entry -> {
                            entry.setDisplayName(tabName);
                        });
                        // Add this inside onTabPluginMessage, under the "tabnameformat" case
                        BendingHub.plugin.debug("Updating tab name for: " + player.getUsername());
                        BendingHub.plugin.debug("Raw response from backend: " + response);
                        BendingHub.plugin.debug("Formatted component: " + tabName.toString());
                        break;
                    case "header":
                        String headerText = convertLegacyToMiniMessage(response);
                        Component header = mm.deserialize(headerText);

                        // Synchronize to prevent the footer thread from overwriting the header
                        synchronized (player) {
                            player.sendPlayerListHeader(header);
                        }
                        break;

                    case "footer":
                        String footerText = convertLegacyToMiniMessage(response);
                        Component footer = mm.deserialize(footerText);

                        // Synchronize to prevent the header thread from overwriting the footer
                        synchronized (player) {
                            player.sendPlayerListFooter(footer);
                        }
                        break;
                    case "group":
                        // 1. Get the weight from config (defaults to 0 if not found)
                        Long weightLong = ConfigManager.toml().getLong("groupWeights." + response.trim());
                        int playerWeight = weightLong != null ? weightLong.intValue() : 0;

                        // 2. Find the tab entry for this player and apply the order
                        player.getTabList().getEntry(playerUUID).ifPresent(entry -> {

                            // Invert the weight so higher weights result in lower list order numbers
                            int sortOrder = playerWeight * -1;

                            // 3. Apply the order to the entry
                            entry.setListOrder(sortOrder);
                        });
                        break;
                    default:
                        break;
                }
            });
        }
    }
}