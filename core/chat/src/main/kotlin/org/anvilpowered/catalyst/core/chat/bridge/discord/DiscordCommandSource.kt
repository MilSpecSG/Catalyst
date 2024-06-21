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

package org.anvilpowered.catalyst.core.chat.bridge.discord

import net.dv8tion.jda.api.JDA
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.anvilpowered.anvil.core.command.CommandSource
import org.anvilpowered.anvil.core.user.Player

internal class DiscordCommandSource(
    private val jda: JDA,
    private val discordChannelId: String,
) : CommandSource {

    override val platformDelegate: Any = error("No platform delegate")
    override val player: Player? = null

    override fun hasPermission(permission: String): Boolean = true

    override fun sendMessage(message: Component) {
        val channel = jda.getTextChannelById(discordChannelId)
        checkNotNull(channel) { "Discord channel $discordChannelId is not found" }
        channel.sendMessage("```" + PlainTextComponentSerializer.plainText().serialize(message) + "```").queue()
    }
}
