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
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.velocity.user.toAnvilPlayer
import org.anvilpowered.catalyst.api.chat.ChannelService
import org.anvilpowered.catalyst.api.chat.placeholder.OnlineUserFormat
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.api.user.MinecraftUserRepository
import org.anvilpowered.catalyst.api.user.getOnlineUser
import org.anvilpowered.catalyst.core.chat.StaffListService
import org.anvilpowered.catalyst.core.chat.bridge.discord.WebhookSender
import org.apache.logging.log4j.Logger

class LeaveListener(
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
    fun onPlayerLeave(event: DisconnectEvent) = runBlocking {
        if (event.loginStatus == DisconnectEvent.LoginStatus.PRE_SERVER_JOIN) {
            return@runBlocking
        }
        val player = event.player.toAnvilPlayer()
        val user = minecraftUserRepository.getOnlineUser(player)
        staffListService.removeStaffNames(player.username)
        val leaveMessage = onlineUserFormatResolver.resolve(registry[catalystKeys.LEAVE_MESSAGE], user)

        if (registry[catalystKeys.LEAVE_LISTENER_ENABLED]) {
            proxyServer.sendMessage(leaveMessage)
            logger.info(PlainTextComponentSerializer.plainText().serialize(leaveMessage))
        }

        val availableChannels = channelService.getAvailable(user.player)
        if (registry[catalystKeys.CHAT_DISCORD_ENABLED]) {
            availableChannels.forEach { channel ->
                webhookSender.sendSpecialMessage(user, channel.discordChannelId, catalystKeys.LEAVE_MESSAGE)
            }
        }
    }
}
