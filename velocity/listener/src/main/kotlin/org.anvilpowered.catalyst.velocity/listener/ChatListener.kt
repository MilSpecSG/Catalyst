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
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.core.user.hasPermissionNotSet
import org.anvilpowered.anvil.core.user.hasPermissionSet
import org.anvilpowered.anvil.velocity.user.toAnvilPlayer
import org.anvilpowered.catalyst.api.chat.ChannelMessage
import org.anvilpowered.catalyst.api.chat.ChannelService
import org.anvilpowered.catalyst.api.chat.LuckpermsService
import org.anvilpowered.catalyst.api.chat.build
import org.anvilpowered.catalyst.api.chat.placeholder.ChannelMessageFormat
import org.anvilpowered.catalyst.api.chat.placeholder.OnlineUserFormat
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.api.user.MinecraftUserRepository
import org.anvilpowered.catalyst.api.user.getOnlineUser
import org.anvilpowered.catalyst.core.chat.ChatFilter
import org.anvilpowered.catalyst.core.chat.ChatService
import org.anvilpowered.catalyst.core.chat.bridge.discord.WebhookSender
import org.apache.logging.log4j.Logger

class ChatListener(
    private val proxyServer: ProxyServer,
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val logger: Logger,
    private val chatService: ChatService,
    private val channelService: ChannelService,
    private val luckpermsService: LuckpermsService,
    private val webhookSender: WebhookSender,
    private val chatFilter: ChatFilter,
    private val channelMessageBuilderFactory: ChannelMessage.Builder.Factory,
    private val minecraftUserRepository: MinecraftUserRepository,
    private val onlineUserFormatResolver: OnlineUserFormat.Resolver,
    private val channelMessageFormatResolver: ChannelMessageFormat.Resolver,
) {
    @Subscribe
    fun onPlayerChat(event: PlayerChatEvent) = runBlocking {
        val player = event.player.toAnvilPlayer()
        if (!registry[catalystKeys.CHAT_ENABLED] || chatService.isDisabledForPlayer(player)) {
            return@runBlocking
        }
        event.result = PlayerChatEvent.ChatResult.denied()
        val rawMessage = if (player.hasPermissionSet(registry[catalystKeys.PERMISSION_CHAT_COLOR])) {
            MiniMessage.miniMessage().deserialize(event.message)
        } else {
            Component.text(event.message)
        }
        // TODO: Move this to dedicated class
        var rawContent = chatService.highlightPlayerNames(player, rawMessage)
        if (player.hasPermissionNotSet(registry[catalystKeys.PERMISSION_LANGUAGE_ADMIN]) &&
            registry[catalystKeys.CHAT_FILTER_ENABLED]
        ) {
            rawContent = chatFilter.replaceSwears(rawContent)
        }

        val user = minecraftUserRepository.getOnlineUser(player)

        // TODO: Properly resolve player written placeholders (and/or escape)
        // Idea: %search% -> google.com/search?q=%search%
        val formattedRawContent = onlineUserFormatResolver.resolve(rawContent, OnlineUserFormat.Placeholders(listOf("source")), user)

        val channel = channelService.getForPlayer(player.id)

        val channelMessage = channelMessageBuilderFactory.build {
            user(user.user)
            channel(channel)
            rawContent(formattedRawContent)
        }

        val formatted = channelMessageFormatResolver.resolve(channel.messageFormat, channelMessage)
        val resolved = ChannelMessage.Resolved(channelMessage, formatted)
        chatService.sendMessage(resolved)
        if (registry[catalystKeys.CHAT_DISCORD_ENABLED]) {
            webhookSender.sendChannelMessage(
                user,
                resolved.backing.content, // TODO: Format for discord, create Player indirection
                resolved.backing.channel.discordChannelId,
            )
        }
    }
}
