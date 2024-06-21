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

package org.anvilpowered.catalyst.core.command.channel

import com.velocitypowered.api.command.CommandSource
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.velocity.user.requiresPermission
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.api.config.ChatChannel
import org.anvilpowered.catalyst.proxy.chat.ChannelSwitchFrontend
import org.anvilpowered.kbrig.builder.ArgumentBuilder
import org.anvilpowered.kbrig.context.executesScoped
import org.anvilpowered.kbrig.tree.LiteralCommandNode

class ChannelAliasCommandFactory(
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val channelSwitchFrontend: ChannelSwitchFrontend,
    private val channelAlias: String,
    private val channel: ChatChannel,
) {
    fun create(): LiteralCommandNode<CommandSource> =
        ArgumentBuilder.literal<CommandSource>(channelAlias)
            .requiresPermission("${registry[catalystKeys.PERMISSION_CHANNEL_PREFIX]}.${channel.id}")
            .executesScoped {
                channelSwitchFrontend.executeChannelSwitch(
                    channelProvider = { channel },
                )
            }.build()
}
