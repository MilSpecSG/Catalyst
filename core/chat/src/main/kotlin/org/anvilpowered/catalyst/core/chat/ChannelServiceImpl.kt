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

package org.anvilpowered.catalyst.core.chat

import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.core.user.Player
import org.anvilpowered.anvil.core.user.PlayerService
import org.anvilpowered.catalyst.api.chat.ChannelService
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.api.config.ChatChannel
import java.util.UUID

class ChannelServiceImpl(
    private val registry: Registry,
    private val playerService: PlayerService,
    private val catalystKeys: CatalystKeys,
) : ChannelService {

    init {
        check(!registry[catalystKeys.PERMISSION_CHANNEL_PREFIX].endsWith('.')) {
            "Channel permission prefix must not end with a '.'"
        }
    }

    /**
     * Maps players to the channel to which they send messages.
     */
    private var playerChannelMapping = mutableMapOf<UUID, String>()

    private var defaultChannelId = registry[catalystKeys.CHAT_DEFAULT_CHANNEL]
    override val defaultChannel: ChatChannel = requireNotNull(get(defaultChannelId)) { "Default chat channel not found" }

    override operator fun get(channelId: String): ChatChannel? = registry[catalystKeys.CHAT_CHANNELS][channelId]
    override fun getForPlayer(playerId: UUID): ChatChannel = playerChannelMapping[playerId]?.let { get(it) } ?: defaultChannel
    override fun getAvailable(player: Player?): List<ChatChannel> {
        return if (player == null) {
            registry[catalystKeys.CHAT_CHANNELS].values.toList()
        } else {
            registry[catalystKeys.CHAT_CHANNELS].values.filter { channel ->
                player.canAccess(channel)
            }
        }
    }

    override fun getReceivers(channelId: String): Sequence<Player> {
        // TODO: Optimize by not recalculating on each chat message
        val channel = checkNotNull(get(channelId)) { "Channel $channelId does not exist" }
        return playerService.getAll()
            .filter { getForPlayer(it.id).id == channelId || (it.canAccess(channel) && channel.alwaysVisible) }
    }

    override fun switch(userUUID: UUID, channelId: String) {
        playerChannelMapping[userUUID] = channelId
    }

    private fun Player.canAccess(channel: ChatChannel): Boolean {
        val permissionValue = hasPermission("${registry[catalystKeys.PERMISSION_CHANNEL_PREFIX]}.${channel.id}")
        return permissionValue == true ||
            (channel.availableByDefault && permissionValue != false)
    }
}
