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

package org.anvilpowered.catalyst.core.command.message

import net.kyori.adventure.text.minimessage.MiniMessage
import org.anvilpowered.anvil.core.command.CommandSource
import org.anvilpowered.anvil.core.command.extractPlayerArgument
import org.anvilpowered.anvil.core.command.extractPlayerSource
import org.anvilpowered.anvil.core.command.requirePlayerArgumentScoped
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.core.user.PlayerService
import org.anvilpowered.anvil.core.user.requiresPermission
import org.anvilpowered.catalyst.api.PluginMessages
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.core.chat.PrivateMessageService
import org.anvilpowered.catalyst.core.command.CommandDefaults
import org.anvilpowered.kbrig.argument.StringArgumentType
import org.anvilpowered.kbrig.builder.ArgumentBuilder
import org.anvilpowered.kbrig.context.executesScoped
import org.anvilpowered.kbrig.context.get
import org.anvilpowered.kbrig.context.yieldError
import org.anvilpowered.kbrig.context.yieldSuccess
import org.anvilpowered.kbrig.tree.LiteralCommandNode

class MessageCommandFactory(
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val playerService: PlayerService,
    private val privateMessageService: PrivateMessageService,
) {

    fun create(): LiteralCommandNode<CommandSource> =
        ArgumentBuilder.literal<CommandSource>("msg").executes(CommandDefaults::notEnoughArgs)
            .requiresPermission(registry[catalystKeys.PERMISSION_MESSAGE])
            .then(
                ArgumentBuilder.requirePlayerArgumentScoped(playerService) {
                    context.source.sendMessage(PluginMessages.notEnoughArgs)
                    yieldError()
                }.then(
                    ArgumentBuilder.required<CommandSource, String>("message", StringArgumentType.GreedyPhrase)
                        .executesScoped {
                            // console messaging not supported yet
                            val sourcePlayer = extractPlayerSource()
                            val targetPlayer = extractPlayerArgument(playerService)

                            if (sourcePlayer.id == targetPlayer.id) {
                                context.source.sendMessage(PluginMessages.messageSelf)
                                yieldError()
                            }
                            privateMessageService.sendMessage(
                                sourcePlayer,
                                targetPlayer,
                                MiniMessage.miniMessage().deserialize(context.get<String>("message")),
                            )
                            yieldSuccess()
                        }.build(),
                ).build(),
            ).build()
}
