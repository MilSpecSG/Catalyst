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
import org.anvilpowered.anvil.core.command.CommandSource
import org.anvilpowered.anvil.core.user.Player
import org.anvilpowered.anvil.core.user.hasPermissionSet
import org.anvilpowered.catalyst.api.PluginMessages
import org.anvilpowered.catalyst.core.command.common.addHelpChild
import org.anvilpowered.catalyst.core.command.requireMinecraftUserArgumentScoped
import org.anvilpowered.kbrig.Command
import org.anvilpowered.kbrig.builder.ArgumentBuilder
import org.anvilpowered.kbrig.builder.executesSuspending
import org.anvilpowered.kbrig.context.yieldError
import org.anvilpowered.kbrig.context.yieldSuccess
import org.anvilpowered.kbrig.tree.LiteralCommandNode

fun NicknameCommandFactory.createDelete(): LiteralCommandNode<CommandSource> =
    ArgumentBuilder.literal<CommandSource>("delete")
        .addHelpChild("nickname|nick delete [<player>]")
        .executesSuspending { context ->
            // without explicit target player
            (context.source as? Player)?.let { player ->
                if (!player.hasPermissionSet("catalyst.nickname.delete.base")) {
                    player.sendMessage(
                        PluginMessages.pluginPrefix
                            .append(Component.text("You don't have permission to delete your nickname!", NamedTextColor.RED)),
                    )
                    0
                } else if (minecraftUserRepository.deleteNickname(player.id)) {
                    player.sendMessage(
                        PluginMessages.pluginPrefix
                            .append(Component.text("Your nickname has been deleted!", NamedTextColor.RED)),
                    )
                    Command.SINGLE_SUCCESS
                } else {
                    player.sendMessage(
                        PluginMessages.pluginPrefix
                            .append(Component.text("You don't have a nickname!", NamedTextColor.RED)),
                    )
                    0
                }
            } ?: run {
                context.source.sendMessage(
                    PluginMessages.pluginPrefix
                        .append(Component.text("You must be a player to use this command without arguments!", NamedTextColor.RED)),
                )
                0
            }
        }
        .then(
            ArgumentBuilder.requireMinecraftUserArgumentScoped(minecraftUserRepository) { minecraftUser ->
                if (!context.source.hasPermissionSet("catalyst.nickname.delete.other")) {
                    context.source.sendMessage(
                        PluginMessages.pluginPrefix
                            .append(Component.text("You don't have permission to delete other players' nicknames!", NamedTextColor.RED)),
                    )
                    yieldError()
                } else if (minecraftUserRepository.deleteNickname(minecraftUser.uuid)) {
                    context.source.sendMessage(
                        PluginMessages.pluginPrefix
                            .append(Component.text("The nickname of ", NamedTextColor.RED))
                            .append(Component.text(minecraftUser.username, NamedTextColor.GOLD))
                            .append(Component.text(" has been deleted!", NamedTextColor.RED)),
                    )
                    yieldSuccess()
                } else {
                    context.source.sendMessage(
                        PluginMessages.pluginPrefix
                            .append(Component.text("The player ", NamedTextColor.RED))
                            .append(Component.text(minecraftUser.username, NamedTextColor.GOLD))
                            .append(Component.text(" doesn't have a nickname!", NamedTextColor.RED)),
                    )
                    yieldError()
                }
            },
        )
        .build()
