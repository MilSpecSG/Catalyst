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

package org.anvilpowered.catalyst.core.chat.builder

import net.kyori.adventure.text.Component
import org.anvilpowered.anvil.core.user.Player
import org.anvilpowered.anvil.core.user.PlayerService
import org.anvilpowered.catalyst.api.chat.ChannelMessage
import org.anvilpowered.catalyst.api.chat.ChannelService
import org.anvilpowered.catalyst.api.chat.LuckpermsService
import org.anvilpowered.catalyst.api.chat.placeholder.MessageContentFormat
import org.anvilpowered.catalyst.api.chat.placeholder.OnlineUserFormat
import org.anvilpowered.catalyst.api.config.ChatChannel
import org.anvilpowered.catalyst.api.user.MinecraftUser
import org.anvilpowered.catalyst.api.user.MinecraftUserRepository
import org.apache.logging.log4j.Logger
import java.util.UUID

internal class ChannelMessageBuilderImpl(
    private val logger: Logger,
    private val playerService: PlayerService,
    private val luckpermsService: LuckpermsService,
    private val minecraftUserRepository: MinecraftUserRepository,
    private val channelService: ChannelService,
    private val onlineUserFormatResolver: OnlineUserFormat.Resolver,
    private val messageContentFormatResolver: MessageContentFormat.Resolver,
) : ChannelMessage.Builder {

    private var user: MinecraftUser? = null
    private var player: Player? = null
    private var channel: ChatChannel? = null
    private var rawContent: Component? = null

    override fun user(user: MinecraftUser): ChannelMessage.Builder {
        player = playerService[user.uuid]
            ?: error("User ${user.username} with id ${user.uuid} is not on the server!")
        this.user = user
        return this
    }

    override suspend fun userId(userId: UUID): ChannelMessage.Builder =
        user(requireNotNull(minecraftUserRepository.findById(userId)) { "Could not find user with id $userId" })

    override fun channel(channel: ChatChannel): ChannelMessage.Builder {
        this.channel = channel
        return this
    }

    override suspend fun channelId(channelId: String): ChannelMessage.Builder =
        channel(requireNotNull(channelService[channelId]) { "Could not find channel with id $channelId" })

    override fun rawContent(rawContent: Component): ChannelMessage.Builder {
        this.rawContent = rawContent
        return this
    }

    override suspend fun build(): ChannelMessage {
        val user = requireNotNull(user) { "User is null" }
        val player = requireNotNull(player) { "Player is null" }
        val channel = requireNotNull(channel) { "Channel is null" }
        val rawContent = requireNotNull(rawContent) { "Content is null" }

        val name = onlineUserFormatResolver.resolve(
            format = luckpermsService.nameFormat(player.id, channel.id) ?: channel.nameFormat,
            MinecraftUser.Online(user, player),
        )

        val content = messageContentFormatResolver.resolve(
            format = luckpermsService.getMessageContentFormat(player.id, channel.id) ?: channel.contentFormat,
            rawContent,
        )

        return ChannelMessage(MinecraftUser.Online(user, player), channel, name, content)
    }

    class Factory(
        private val logger: Logger,
        private val playerService: PlayerService,
        private val luckpermsService: LuckpermsService,
        private val minecraftUserRepository: MinecraftUserRepository,
        private val channelService: ChannelService,
        private val onlineUserFormatResolver: OnlineUserFormat.Resolver,
        private val messageContentFormatResolver: MessageContentFormat.Resolver,
    ) : ChannelMessage.Builder.Factory {
        override fun builder(): ChannelMessage.Builder = ChannelMessageBuilderImpl(
            logger,
            playerService,
            luckpermsService,
            minecraftUserRepository,
            channelService,
            onlineUserFormatResolver,
            messageContentFormatResolver,
        )
    }
}
