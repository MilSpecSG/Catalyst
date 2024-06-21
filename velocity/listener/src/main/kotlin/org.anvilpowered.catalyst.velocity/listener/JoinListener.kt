/*
 *   Catalyst - AnvilPowered.org
 *   Copyright (C) 2019-2024 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.anvilpowered.catalyst.velocity.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.velocity.user.toAnvilPlayer
import org.anvilpowered.catalyst.api.chat.ChannelService
import org.anvilpowered.catalyst.api.chat.placeholder.OnlineUserFormat
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.api.user.MinecraftUser
import org.anvilpowered.catalyst.api.user.MinecraftUserRepository
import org.anvilpowered.catalyst.core.chat.StaffListService
import org.anvilpowered.catalyst.core.chat.bridge.discord.WebhookSender
import org.apache.logging.log4j.Logger

class JoinListener(
    private val proxyServer: ProxyServer,
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val logger: Logger,
    private val channelService: ChannelService,
    private val webhookSender: WebhookSender,
    private val staffListService: StaffListService,
    private val minecraftUserRepository: MinecraftUserRepository,
    private val onlineUserFormatResolver: OnlineUserFormat.Resolver,
) {
    @Subscribe
    fun onPlayerJoin(event: PostLoginEvent) = runBlocking {
        val player = event.player
        val result = minecraftUserRepository.put(
            MinecraftUser.CreateDto(
                id = player.uniqueId,
                username = player.username,
                ipAddress = player.remoteAddress.hostString,
            ),
        )

        val user = MinecraftUser.Online(result.entity, player.toAnvilPlayer())

        if (result.created && registry[catalystKeys.JOIN_LISTENER_ENABLED]) {
            proxyServer.sendMessage(onlineUserFormatResolver.resolve(registry[catalystKeys.JOIN_MESSAGE_FIRST], user))
        }

        staffListService.getStaffNames(
            player.username,
            player.hasPermission(registry[catalystKeys.PERMISSION_STAFFLIST_ADMIN]),
            player.hasPermission(registry[catalystKeys.PERMISSION_STAFFLIST_STAFF]),
            player.hasPermission(registry[catalystKeys.PERMISSION_STAFFLIST_OWNER]),
        )
        val joinMessage = onlineUserFormatResolver.resolve(registry[catalystKeys.JOIN_MESSAGE_NORMAL], user)
        if (registry[catalystKeys.JOIN_LISTENER_ENABLED]) {
            proxyServer.sendMessage(joinMessage)
            logger.info(PlainTextComponentSerializer.plainText().serialize(joinMessage))
        }

        val availableChannels = channelService.getAvailable(user.player)
        if (registry[catalystKeys.CHAT_DISCORD_ENABLED]) {
            availableChannels.forEach { channel ->
                webhookSender.sendSpecialMessage(user, channel.discordChannelId, catalystKeys.JOIN_MESSAGE_NORMAL)
            }
        }

        player.sendMessage(
            Component.text()
                .append(Component.text("Welcome to the server, ", NamedTextColor.GRAY, TextDecoration.ITALIC))
                .append(Component.text(player.username, NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("!", NamedTextColor.GRAY, TextDecoration.ITALIC))
                .append(Component.newline())
                .append(Component.text("You are currently chatting in channel ", NamedTextColor.AQUA, TextDecoration.ITALIC))
                .append(channelService.getForPlayer(user.player.id).name)
                .build(),
        )
    }
}
