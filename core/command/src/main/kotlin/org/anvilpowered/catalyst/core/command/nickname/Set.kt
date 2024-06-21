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

package org.anvilpowered.catalyst.core.command.nickname

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.anvilpowered.anvil.core.command.CommandSource
import org.anvilpowered.anvil.core.user.Player
import org.anvilpowered.anvil.core.user.hasPermissionNotSet
import org.anvilpowered.catalyst.api.PluginMessages
import org.anvilpowered.catalyst.core.command.common.addHelpChild
import org.anvilpowered.catalyst.core.command.requireMinecraftUserArgumentScoped
import org.anvilpowered.kbrig.Command
import org.anvilpowered.kbrig.argument.StringArgumentType
import org.anvilpowered.kbrig.builder.ArgumentBuilder
import org.anvilpowered.kbrig.builder.executesSuspending
import org.anvilpowered.kbrig.context.get
import org.anvilpowered.kbrig.context.yieldError
import org.anvilpowered.kbrig.context.yieldSuccess
import org.anvilpowered.kbrig.tree.LiteralCommandNode

fun NicknameCommandFactory.createSet(): LiteralCommandNode<CommandSource> =
    ArgumentBuilder.literal<CommandSource>("set")
        .addHelpChild("nickname set <nickname> [<player>]")
        .then(
            ArgumentBuilder.required<CommandSource, String>("nickname", StringArgumentType.quotablePhrase())
                .executesSuspending { context ->
                    (context.source as? Player)?.let { player ->
                        if (player.hasPermissionNotSet("catalyst.nickname.set.base")) {
                            player.sendMessage(
                                PluginMessages.pluginPrefix
                                    .append(Component.text("You don't have permission to set your nickname!", NamedTextColor.RED)),
                            )
                        } else if (minecraftUserRepository.updateNickname(player.id, context["nickname"])) {
                            player.sendMessage(
                                PluginMessages.pluginPrefix
                                    .append(Component.text("Your nickname has been set to '", NamedTextColor.GRAY))
                                    .append(MiniMessage.miniMessage().deserialize(context["nickname"]))
                                    .append(Component.text("'!", NamedTextColor.GRAY)),
                            )
                        } else {
                            player.sendMessage(
                                PluginMessages.pluginPrefix
                                    .append(Component.text("Your nickname could not be set!", NamedTextColor.RED)),
                            )
                        }
                    } ?: run {
                        context.source.sendMessage(
                            PluginMessages.pluginPrefix
                                .append(Component.text("You must be a player to use this command without arguments!", NamedTextColor.RED)),
                        )
                        0
                    }
                    Command.SINGLE_SUCCESS
                },
        )
        .then(
            ArgumentBuilder.requireMinecraftUserArgumentScoped(minecraftUserRepository) { minecraftUser ->
                if (context.source.hasPermissionNotSet("catalyst.nickname.set.other")) {
                    context.source.sendMessage(
                        PluginMessages.pluginPrefix
                            .append(Component.text("You don't have permission to set other players' nicknames!", NamedTextColor.RED)),
                    )
                    yieldError()
                } else if (minecraftUserRepository.updateNickname(minecraftUser.uuid, context["nickname"])) {
                    context.source.sendMessage(
                        PluginMessages.pluginPrefix
                            .append(Component.text("The nickname of '", NamedTextColor.GRAY))
                            .append(Component.text(minecraftUser.username, NamedTextColor.GOLD))
                            .append(Component.text("' has been set from '", NamedTextColor.GRAY))
                            .append(MiniMessage.miniMessage().deserialize(minecraftUser.nickname ?: "[none]"))
                            .append(Component.text("' to '", NamedTextColor.GRAY))
                            .append(MiniMessage.miniMessage().deserialize(context["nickname"]))
                            .append(Component.text("'!", NamedTextColor.GRAY)),
                    )
                    yieldSuccess()
                } else {
                    context.source.sendMessage(
                        PluginMessages.pluginPrefix
                            .append(Component.text("The nickname of '", NamedTextColor.GRAY))
                            .append(Component.text(minecraftUser.username, NamedTextColor.GOLD))
                            .append(Component.text("' could not be set!", NamedTextColor.RED)),
                    )
                    yieldError()
                }
            },
        )
        .build()
