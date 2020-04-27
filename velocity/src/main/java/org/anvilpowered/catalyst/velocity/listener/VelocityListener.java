package org.anvilpowered.catalyst.velocity.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.ModInfo;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import org.anvilpowered.anvil.api.Anvil;
import org.anvilpowered.anvil.api.core.coremember.CoreMemberManager;
import org.anvilpowered.anvil.api.core.model.coremember.CoreMember;
import org.anvilpowered.anvil.api.data.registry.Registry;
import org.anvilpowered.anvil.api.util.TextService;
import org.anvilpowered.catalyst.api.data.config.AdvancedServerInfo;
import org.anvilpowered.catalyst.api.data.key.CatalystKeys;
import org.anvilpowered.catalyst.api.listener.ChatListener;
import org.anvilpowered.catalyst.api.listener.JoinListener;
import org.anvilpowered.catalyst.api.listener.LeaveListener;
import org.anvilpowered.catalyst.api.plugin.PluginMessages;
import org.anvilpowered.catalyst.api.service.BroadcastService;
import org.anvilpowered.catalyst.api.service.LoggerService;
import org.anvilpowered.catalyst.api.service.TabService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class VelocityListener {

    @Inject
    private ProxyServer proxyServer;

    @Inject
    private Registry registry;

    @Inject
    private TabService<TextComponent> tabService;

    @Inject
    private ChatListener<Player> chatListener;

    @Inject
    private JoinListener<Player> joinListener;

    @Inject
    private LeaveListener<Player> leaveListener;

    @Inject
    private LoggerService<TextComponent> loggerService;


    @Inject
    private PluginMessages<TextComponent> pluginMessages;

    @Inject
    private BroadcastService<TextComponent> broadcastService;

    @Inject
    private TextService<TextComponent, CommandSource> textService;

    @Subscribe
    public void onPlayerLeave(DisconnectEvent event) {
        leaveListener.onPlayerLeave(event.getPlayer(), event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {

        Player player = event.getPlayer();
        boolean[] flags = new boolean[8];
        Anvil.getServiceManager().provide(CoreMemberManager.class).getPrimaryComponent()
            .getOneOrGenerateForUser(
                player.getUniqueId(),
                player.getUsername(),
                player.getRemoteAddress().getHostString(),
                flags
            ).thenAcceptAsync(optionalMember -> {
            if (!optionalMember.isPresent()) {
                return;
            }
            if (flags[0]) {
                broadcastService.broadcast(
                    textService.deserialize(
                        registry.getOrDefault(CatalystKeys.FIRST_JOIN)
                            .replace("%player%", player.getUsername()))
                );
            }
            CoreMember<?> coreMember = optionalMember.get();
            if (Anvil.getServiceManager().provide(CoreMemberManager.class).getPrimaryComponent().checkBanned(coreMember)) {
                player.disconnect(
                    pluginMessages.getBanMessage(coreMember.getBanReason(), coreMember.getBanEndUtc())
                );
            }
        }).join();

        if (event.getPlayer().getVirtualHost().isPresent()) {
            if (registry.getOrDefault(CatalystKeys.ADVANCED_SERVER_INFO_ENABLED)) {
                AtomicBoolean hostNameExists = new AtomicBoolean(false);
                for (AdvancedServerInfo serverInfo : registry.getOrDefault(CatalystKeys.ADVANCED_SERVER_INFO)) {
                    if (serverInfo.hostName.equalsIgnoreCase(event.getPlayer().getVirtualHost().get().getHostString())) {
                        hostNameExists.set(true);
                    }
                }
                if (!hostNameExists.get()) {
                    event.getPlayer().disconnect(LegacyComponentSerializer.legacy().deserialize("&4Please re-connect using the correct IP!", '&'));
                }
            }
            joinListener.onPlayerJoin(event.getPlayer(), event.getPlayer().getUniqueId(), event.getPlayer().getVirtualHost().get().getHostString());
        }
    }

    @Subscribe
    public void onChat(PlayerChatEvent e) {
        Player player = e.getPlayer();
        e.setResult(PlayerChatEvent.ChatResult.denied());
        Anvil.getServiceManager().provide(CoreMemberManager.class).getPrimaryComponent()
            .getOneForUser(
                player.getUniqueId()
            ).thenAcceptAsync(optionalMember -> {
            if (!optionalMember.isPresent()) {
                return;
            }
            CoreMember<?> coreMember = optionalMember.get();
            if (Anvil.getServiceManager().provide(CoreMemberManager.class).getPrimaryComponent().checkMuted(coreMember)) {
                player.sendMessage(
                    pluginMessages.getMuteMessage(coreMember.getMuteReason(), coreMember.getMuteEndUtc())
                );
            } else {
                chatListener.onPlayerChat(e.getPlayer(), e.getPlayer().getUniqueId(), e.getMessage());
            }
        });
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(new LegacyChannelIdentifier("GlobalTab"))) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        ByteArrayDataInput in = event.dataAsDataStream();
        String subChannel = in.readUTF();

        if (subChannel.endsWith("Balance")) {
            String[] packet = in.readUTF().split(":");
            String userName = packet[0];
            double balance = Double.parseDouble(packet[1]);
            tabService.setBalance(userName, balance);
        }
    }


    @Subscribe
    public void onServerListPing(ProxyPingEvent proxyPingEvent) {
        ServerPing serverPing = proxyPingEvent.getPing();
        ServerPing.Builder builder = ServerPing.builder();
        ModInfo modInfo = null;
        String playerProvidedHost;
        AtomicBoolean hostNameExists = new AtomicBoolean(false);
        List<AdvancedServerInfo> advancedServerInfoList = new ArrayList<>();

        if (proxyPingEvent.getConnection().getVirtualHost().isPresent()) {
            playerProvidedHost = proxyPingEvent.getConnection().getVirtualHost().get().getHostString();
        } else {
            return;
        }

        boolean useCatalyst = registry.getOrDefault(CatalystKeys.ADVANCED_SERVER_INFO_ENABLED);


        if (useCatalyst) {
            advancedServerInfoList = registry.get(CatalystKeys.ADVANCED_SERVER_INFO).orElseThrow(() -> new IllegalArgumentException("Invalid server configuration!"));
            advancedServerInfoList.forEach(advancedServerInfo -> {
                if (playerProvidedHost.equals(advancedServerInfo.hostName)) {
                    hostNameExists.set(true);
                    builder.description(LegacyComponentSerializer.legacy().deserialize(advancedServerInfo.motd, '&'));
                }
            });
            if (!hostNameExists.get()) {
                builder.description(LegacyComponentSerializer.legacy().deserialize("&4Using the direct IP to connect has been disabled!", '&'));
            }
        } else {
            builder.description(LegacyComponentSerializer.legacy().deserialize(registry.getOrDefault(CatalystKeys.MOTD), '&'));
        }

        if (proxyServer.getConfiguration().isAnnounceForge()) {
            if (useCatalyst) {
                for (AdvancedServerInfo advancedServerInfo : advancedServerInfoList) {
                    if (playerProvidedHost.equalsIgnoreCase(advancedServerInfo.hostName)) {
                        for (RegisteredServer pServer : proxyServer.getAllServers()) {
                            try {
                                serverPing = pServer.ping().get();
                            } catch (InterruptedException | ExecutionException e) {
                                return;
                            }
                            if (advancedServerInfo.port == pServer.getServerInfo().getAddress().getPort()) {
                                if (serverPing.getModinfo().isPresent()) {
                                    modInfo = serverPing.getModinfo().get();
                                }
                            }
                        }
                    }
                }
            } else {
                for (String server : proxyServer.getConfiguration().getAttemptConnectionOrder()) {
                    Optional<RegisteredServer> registeredServer = proxyServer.getServer(server);
                    if (!registeredServer.isPresent()) return;
                    ServerPing ping = registeredServer.get().ping().join();
                    if (ping == null) continue;
                    if (ping.getModinfo().isPresent()) {
                        modInfo = ping.getModinfo().get();
                    }
                }
            }
            if (modInfo != null) {
                builder.mods(modInfo);
            }
        }

        if (registry.getOrDefault(CatalystKeys.SERVER_PING).equalsIgnoreCase("players")) {
            if (proxyServer.getPlayerCount() > 0) {
                ServerPing.SamplePlayer[] samplePlayers = new ServerPing.SamplePlayer[proxyServer.getPlayerCount()];
                List<Player> proxiedPlayers = new ArrayList<>(proxyServer.getAllPlayers());
                for (int i = 0; i < proxyServer.getPlayerCount(); i++) {
                    samplePlayers[i] = new ServerPing.SamplePlayer(proxiedPlayers.get(i).getUsername(), UUID.randomUUID());
                }
                builder.samplePlayers(samplePlayers);
            }
        } else if (registry.getOrDefault(CatalystKeys.SERVER_PING).equalsIgnoreCase("MESSAGE")) {
            builder.samplePlayers(new ServerPing.SamplePlayer(registry.getOrDefault(CatalystKeys.SERVER_PING_MESSAGE), UUID.randomUUID()));
        }

        if (serverPing.getFavicon().isPresent()) {
            builder.favicon(serverPing.getFavicon().get());
        }

        builder.onlinePlayers(proxyServer.getPlayerCount());
        builder.version(serverPing.getVersion());
        builder.maximumPlayers(proxyServer.getConfiguration().getShowMaxPlayers());
        proxyPingEvent.setPing(builder.build());
    }
}