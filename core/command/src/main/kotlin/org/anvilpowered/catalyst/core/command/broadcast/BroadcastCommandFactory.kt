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

package org.anvilpowered.catalyst.core.command.broadcast

import org.anvilpowered.anvil.core.command.CommandSource
import org.anvilpowered.anvil.core.user.requiresPermission
import org.anvilpowered.catalyst.core.command.CommandDefaults
import org.anvilpowered.kbrig.argument.StringArgumentType
import org.anvilpowered.kbrig.builder.ArgumentBuilder
import org.anvilpowered.kbrig.builder.executesSingleSuccess
import org.anvilpowered.kbrig.tree.LiteralCommandNode

class BroadcastCommandFactory() {
    fun create(): LiteralCommandNode<CommandSource> =
        ArgumentBuilder.literal<CommandSource>("broadcast").executes(CommandDefaults::notEnoughArgs)
            .requiresPermission("catalyst.chat.broadcast").then(
                ArgumentBuilder.required<CommandSource, String>("message", StringArgumentType.GreedyPhrase)
                    .executesSingleSuccess { context ->
                        TODO() // proxyServer.sendMessage(PluginMessages.getBroadcast(Component.text(context.get<String>("message"))))
                    }.build(),
            ).build()
}
