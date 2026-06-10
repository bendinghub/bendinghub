package com.houdert6.bendingHub;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent.ServerResult;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import me.unprankable.bendinghub.chat.ChatManager;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Plugin(id = "bendinghub", name = "BendingHub", version = "1.0.0", description = "bendinghub velocity", authors = {"houdert6"})
public class BendingHub {
    private final ProxyServer proxy;
    private static final MinecraftChannelIdentifier MINIGAME_INFO = MinecraftChannelIdentifier.create("bendinghub", "minigameinfo");
    private static final MinecraftChannelIdentifier MINIGAME_SEND = MinecraftChannelIdentifier.create("bendinghub", "minigamesend");
    private static final MinecraftChannelIdentifier BENDINGHUB_CHAT = MinecraftChannelIdentifier.create("bendinghub", "chat");
    private static final MinecraftChannelIdentifier BENDINGHUB_TAB = MinecraftChannelIdentifier.create("bendinghub", "tab");
    private final Map<Player, byte[]> minigameInfoMap = new HashMap<>();

    @Inject
    public BendingHub(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Inject
    @DataDirectory
    private Path dataFolder;

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        ConfigManager.initialize(dataFolder, logger);
        CommandManager manager = proxy.getCommandManager();
        LiteralCommandNode<CommandSource> cmdNode = BrigadierCommand.literalArgumentBuilder("msg")/*.requires(source -> source.hasPermission("bendinghub.msg"))*/.executes(ctx -> BrigadierCommand.FORWARD).then(BrigadierCommand.requiredArgumentBuilder("to",
                StringArgumentType.word()).suggests((ctx, builder) -> {
                    suggest(builder, "*");
                    suggest(builder, "**");
                    for (Player p : proxy.getAllPlayers()) {
                        suggest(builder, p.getUsername());
                    }
                    return builder.buildFuture();
        }).executes(ctx -> BrigadierCommand.FORWARD).then(BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString()).executes(ctx -> {
            boolean forwardSameServer = ConfigManager.toml().getBoolean("private-messages.forward-same-server-message-commands", true);
            boolean forwardInvalid = ConfigManager.toml().getBoolean("private-messages.forward-invalid-targets", true);

            String playerName = ctx.getArgument("to", String.class);
            if (playerName.equals("*") || playerName.equals("**")) {
                Optional<ServerConnection> sourceConn = ctx.getSource() instanceof Player p ? p.getCurrentServer() : Optional.empty();
                String message = ctx.getArgument("message", String.class);
                for (Player player : proxy.getAllPlayers()) {
                    Optional<ServerConnection> targetConn = player.getCurrentServer();
                    if (sourceConn.isEmpty() || targetConn.isEmpty() || sourceConn.get().getServer() != targetConn.get().getServer() || !forwardSameServer) {
                        // Send a message to all players not on the same server
                        dm(ctx.getSource(), player, message);
                    }
                    return forwardSameServer ? BrigadierCommand.FORWARD : Command.SINGLE_SUCCESS; // let essx message players on the same server as the target
                }
            }
            Player player = proxy.getPlayer(playerName).orElse(null);
            if (player == null) {
                if (!forwardInvalid) {
                    if (ConfigManager.toml().containsPrimitive("private-messages.invalid-target-error")) {
                        ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.toml().getString("private-messages.invalid-target-error")));
                    }
                    return Command.SINGLE_SUCCESS;
                }
                return BrigadierCommand.FORWARD;
            }
            Optional<ServerConnection> targetConn = player.getCurrentServer();
            Optional<ServerConnection> sourceConn;
            if (ctx.getSource() instanceof Player p) {
                sourceConn = p.getCurrentServer();
            } else {
                sourceConn = Optional.empty();
            }
            if (targetConn.isPresent() && sourceConn.isPresent() && targetConn.get().getServer() == sourceConn.get().getServer()) {
                if (forwardSameServer)
                    return BrigadierCommand.FORWARD; // If both players are on the same server then it's just probably better to let EssentialsX handle the message command like usual
            }
            dm(ctx.getSource(), player, ctx.getArgument("message", String.class));
            return Command.SINGLE_SUCCESS; // don't send the msg command to essx
        }))).build();
        BrigadierCommand cmd = new BrigadierCommand(cmdNode);
        CommandMeta msg = manager.metaBuilder(cmd)
                .plugin(this)
                .aliases("w", "m", "pm", "tell", "whisper")
                .build();
        manager.register(msg, cmd);

        proxy.getChannelRegistrar().register(MINIGAME_INFO);
        proxy.getChannelRegistrar().register(MINIGAME_SEND);
        // Register the bendinghub chat channel so the proxy can receive backend chat plugin messages
        proxy.getChannelRegistrar().register(BENDINGHUB_CHAT);
        proxy.getChannelRegistrar().register(BENDINGHUB_TAB);
        logger.info("Registered!");
    }

    @Subscribe
    public void onServerConnect(ServerPreConnectEvent event) {
        if (event.getResult().getServer().isPresent() && event.getResult().getServer().get().getServerInfo().getName().equals("minigame") && !minigameInfoMap.containsKey(event.getPlayer())) {
            Optional<RegisteredServer> arena = proxy.getServer("arena");
            if (arena.isPresent()) {
                event.setResult(ServerResult.allowed(arena.get()));
            }
        }
    }
    @Subscribe
    public void onPluginMsg(PluginMessageEvent event) {
        if (event.getIdentifier().equals(MINIGAME_SEND)) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            if (!(event.getSource() instanceof ServerConnection)) {
                logger.warn("minigame send request not sent from a server");
                return;
            }
            if (!(event.getTarget() instanceof Player p)) {
                logger.warn("minigame send request not sent targetting a player");
                return;
            }
            minigameInfoMap.put(p, event.getData());
            ScheduledTask task = proxy.getScheduler().buildTask(this, () -> minigameInfoMap.remove(p)).delay(1, TimeUnit.MINUTES).schedule();
            Optional<RegisteredServer> minigame = proxy.getServer("minigame");
            if (minigame.isPresent()) {
                p.createConnectionRequest(minigame.get()).connectWithIndication().thenAccept(success -> {
                    task.cancel();
                    if (success) {
                        proxy.getScheduler().buildTask(this, () -> minigameInfoMap.remove(p)).delay(5, TimeUnit.SECONDS).schedule();
                    } else {
                        task.cancel();
                        minigameInfoMap.remove(p);
                    }
                });
            }
        } else if (event.getIdentifier().equals(MINIGAME_INFO)) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            if (!(event.getSource() instanceof ServerConnection conn)) {
                logger.warn("minigame info request not sent from a server");
                return;
            }
            if (!(event.getTarget() instanceof Player p)) {
                logger.warn("minigame info request not sent targetting a player");
                return;
            }
            if (minigameInfoMap.containsKey(p)) {
                conn.sendPluginMessage(MINIGAME_INFO, minigameInfoMap.get(p));
            }
        }
    }

    @Subscribe
    public void onChatPluginMessageFromBackend(PluginMessageEvent event){
        if(!BENDINGHUB_CHAT.equals(event.getIdentifier())) return;
        if (!(event.getSource() instanceof ServerConnection)) return;
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        Collection<RegisteredServer> servers = proxy.getAllServers();
        for(RegisteredServer server : servers){
            server.sendPluginMessage(BENDINGHUB_CHAT, event.getData());
        }

    }

    @Subscribe
    public void onTabPluginMessage(PluginMessageEvent event) {
        // Ensure the packet belongs to our custom channel
        if (!event.getIdentifier().equals(BENDINGHUB_TAB)) {
            return;
        }

        // Consume the event so Velocity doesn't try to forward it natively or throw channel errors
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        // Verify the source came from a backend Spigot server connection
        if (!(event.getSource() instanceof com.velocitypowered.api.proxy.ServerConnection)) {
            return;
        }

        com.velocitypowered.api.proxy.ServerConnection sourceServer = (com.velocitypowered.api.proxy.ServerConnection) event.getSource();
        String originServerName = sourceServer.getServerInfo().getName();

        byte[] rawPayload = event.getData();

        // Relay the exact payload to every sub-server EXCEPT the one that sent it
        for (RegisteredServer subServer : proxy.getAllServers()) {
            String targetServerName = subServer.getServerInfo().getName();

            // Anti-Loop / Echo Filter: Skip sending it back to the origin server
            if (targetServerName.equalsIgnoreCase(originServerName)) {
                continue;
            }

            subServer.sendPluginMessage(BENDINGHUB_TAB, rawPayload);
        }
    }

    private void dm(CommandSource from, Player player, String msg) {
        Toml msgsConfig = ConfigManager.toml().getTable("private-messages");
        boolean doLegacy = msgsConfig.getBoolean("legacy-format", true);
        boolean doMiniMsg = msgsConfig.getBoolean("minimessage-format", false);
        // [from -> me] msg
        //player.sendMessage(Component.empty().append(Component.text("[").color(NamedTextColor.GOLD)).append(Component.text(from instanceof Player p ? p.getUsername() : "Console (proxy)").color(NamedTextColor.RED)).append(Component.text(" -> ").color(NamedTextColor.GOLD)).append(Component.text("me").color(NamedTextColor.RED)).append(Component.text("] ").color(NamedTextColor.GOLD)).append(LegacyComponentSerializer.legacyAmpersand().deserialize(msg)));
        String receivingFormat = msgsConfig.getString("message-format-receiving").replace("%player%", from instanceof Player p ? p.getUsername() : "Console (proxy)");
        Component receivingMsg;
        if (doLegacy && doMiniMsg) {
            receivingMsg = MiniMessage.miniMessage().deserialize(receivingFormat.replace("%message%", ChatManager.convertLegacyToMiniMessage(msg)));
        } else if (doLegacy) {
            receivingMsg = MiniMessage.miniMessage().deserialize(receivingFormat.replace("%message%", "<bhpm-legacymsg>"), Placeholder.component("bhpm-legacymsg", LegacyComponentSerializer.legacyAmpersand().deserialize(msg)));
        } else if (doMiniMsg) {
            receivingMsg = MiniMessage.miniMessage().deserialize(receivingFormat.replace("%message%", msg));
        } else {
            receivingMsg = MiniMessage.miniMessage().deserialize(receivingFormat.replace("%message%", "<bhpm-plainmsg>"), Placeholder.unparsed("bhpm-plainmsg", msg));
        }
        player.sendMessage(receivingMsg);
        // [me -> player] msg
        //from.sendMessage(Component.empty().append(Component.text("[").color(NamedTextColor.GOLD)).append(Component.text("me").color(NamedTextColor.RED)).append(Component.text(" -> ").color(NamedTextColor.GOLD)).append(Component.text(player.getUsername()).color(NamedTextColor.RED)).append(Component.text("] ").color(NamedTextColor.GOLD)).append(LegacyComponentSerializer.legacyAmpersand().deserialize(msg)));
        String sendingFormat = msgsConfig.getString("message-format-sending").replace("%player%", player.getUsername());
        Component sendingMsg;
        if (doLegacy && doMiniMsg) {
            sendingMsg = MiniMessage.miniMessage().deserialize(sendingFormat.replace("%message%", ChatManager.convertLegacyToMiniMessage(msg)));
        } else if (doLegacy) {
            sendingMsg = MiniMessage.miniMessage().deserialize(sendingFormat.replace("%message%", "<bhpm-legacymsg>"), Placeholder.component("bhpm-legacymsg", LegacyComponentSerializer.legacyAmpersand().deserialize(msg)));
        } else if (doMiniMsg) {
            sendingMsg = MiniMessage.miniMessage().deserialize(sendingFormat.replace("%message%", msg));
        } else {
            sendingMsg = MiniMessage.miniMessage().deserialize(sendingFormat.replace("%message%", "<bhpm-plainmsg>"), Placeholder.unparsed("bhpm-plainmsg", msg));
        }
        from.sendMessage(sendingMsg);
    }
    private void suggest(SuggestionsBuilder builder, String suggestion) {
        if (suggestion.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
            builder.suggest(suggestion);
        }
    }
}
