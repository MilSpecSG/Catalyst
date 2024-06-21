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

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.core.user.Player
import org.anvilpowered.anvil.core.user.PlayerService
import org.anvilpowered.anvil.core.user.hasPermissionSet
import org.anvilpowered.catalyst.api.chat.LuckpermsService
import org.anvilpowered.catalyst.api.chat.PrivateMessage
import org.anvilpowered.catalyst.api.chat.placeholder.PrivateMessageFormat
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.api.user.MinecraftUser
import org.anvilpowered.catalyst.api.user.MinecraftUserRepository
import org.apache.logging.log4j.Logger
import java.util.UUID

class PrivateMessageService(
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val logger: Logger,
    private val playerService: PlayerService,
    private val luckpermsService: LuckpermsService,
    private val minecraftUserRepository: MinecraftUserRepository,
    private val privateMessageFormatResolver: PrivateMessageFormat.Resolver,
) {
    private var replyMap = mutableMapOf<UUID, UUID>()

    suspend fun sendMessage(source: Player, recipient: Player, content: Component) {
        val sourceUser = minecraftUserRepository.findById(source.id)?.let { MinecraftUser.Online(it, source) }
            ?: throw IllegalStateException("User ${source.username} with id ${source.id} is not in the database!")

        val recipientUser = minecraftUserRepository.findById(recipient.id)?.let { MinecraftUser.Online(it, recipient) }
            ?: throw IllegalStateException("User ${recipient.username} with id ${recipient.id} is not in the database!")

        val message = PrivateMessage(sourceUser, recipientUser, content)

        source.sendMessage(privateMessageFormatResolver.resolve(registry[catalystKeys.CHAT_DM_FORMAT_SOURCE], message))
        recipient.sendMessage(privateMessageFormatResolver.resolve(registry[catalystKeys.CHAT_DM_FORMAT_RECIPIENT], message))
        replyMap[recipient.id] = source.id
        replyMap[source.id] = recipient.id
        socialSpy(message)
    }

    suspend fun reply(source: Player, content: Component) {
        val recipientId = replyMap[source.id]
        if (recipientId == null) {
            source.sendMessage(Component.text("No person to reply to.").color(NamedTextColor.RED))
            return
        }

        val recipient = playerService[recipientId]
        if (recipient == null) {
            source.sendMessage(Component.text("The person you last replied to is offline.").color(NamedTextColor.RED))
            return
        }

        sendMessage(source, recipient, content)
    }

    // TODO: Send PM from console

    private suspend fun socialSpy(message: PrivateMessage) {
        val socialSpyMessage = privateMessageFormatResolver.resolve(registry[catalystKeys.CHAT_DM_FORMAT_SOCIALSPY], message)
        playerService.getAll()
            .filter { it.hasPermissionSet(registry[catalystKeys.PERMISSION_SOCIALSPY_BASE]) }
            .filter { it.id != message.source.player.id && it.id != message.recipient.player.id }
            .forEach { it.sendMessage(socialSpyMessage) }
    }
}
