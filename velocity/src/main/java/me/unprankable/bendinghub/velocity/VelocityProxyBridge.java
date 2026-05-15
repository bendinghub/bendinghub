package me.unprankable.bendinghub.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.nio.ByteBuffer;
import java.util.Optional;

@Plugin(id = "bendinghub-proxy", name = "BendinghubProxy", version = "1.0.0", authors = {"unprankable"})
public class VelocityProxyBridge {
    private static final String PROXY_CHANNEL = "BungeeCord";

    private final ProxyServer proxy;

    @Inject
    public VelocityProxyBridge(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        // Verify the channel is the classic BungeeCord channel
        Optional<String> maybeChannel = event.getIdentifier().asString();
        String channelName = maybeChannel.orElse(null);
        if (channelName == null || !PROXY_CHANNEL.equals(channelName)) {
            return;
        }

        // Copy data to a byte[] and parse as the Bungee Forward message
        ByteBuffer data = event.data();
        if (data == null) return;
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);

        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        try {
            String subchannel = in.readUTF();
            if (!"Forward".equals(subchannel)) {
                return;
            }
            String target = in.readUTF(); // usually "ALL" or a server name
            String forwardSubchannel = in.readUTF(); // e.g. "bendinghub:chat"
            short payloadLength = in.readShort();
            byte[] payload = new byte[payloadLength];
            in.readFully(payload);

            // Broadcast payload to all backend servers (or specific server if target is not ALL)
            if ("ALL".equalsIgnoreCase(target)) {
                for (RegisteredServer server : proxy.getAllServers()) {
                    server.sendPluginMessage(forwardSubchannel, ByteBuffer.wrap(payload));
                }
            } else {
                // forward only to a specific server if it matches
                proxy.getServer(target).ifPresent(registeredServer ->
                        registeredServer.sendPluginMessage(forwardSubchannel, ByteBuffer.wrap(payload)));
            }

        } catch (Exception e) {
            // Swallow exceptions to avoid proxy instability; log to console
            proxy.getConsoleCommandSource().sendMessage(com.velocitypowered.api.proxy.message.TextComponent.of("[BendinghubProxy] Error parsing plugin message: " + e.getMessage()));
        }
    }
}

