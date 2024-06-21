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
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.velocity.command.extractPlayerSource
import org.anvilpowered.anvil.velocity.user.requiresPermission
import org.anvilpowered.catalyst.api.chat.ChannelService
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.proxy.chat.ChannelSwitchFrontend
import org.anvilpowered.kbrig.builder.ArgumentBuilder
import org.anvilpowered.kbrig.context.executesScoped
import org.anvilpowered.kbrig.tree.LiteralCommandNode

class ChannelCommandFactory(
    val registry: Registry,
    val catalystKeys: CatalystKeys,
    val channelService: ChannelService,
    val channelSwitchFrontend: ChannelSwitchFrontend,
) {
    fun create(): LiteralCommandNode<CommandSource> =
        ArgumentBuilder.literal<CommandSource>("channel")
            .requiresPermission(registry[catalystKeys.PERMISSION_CHANNEL_BASE])
            .then(createSwitch())
            .executesScoped {
                val player = extractPlayerSource()
                val currentChannel = channelService.getForPlayer(player.uniqueId)
                player.sendMessage(
                    Component.text()
                        .append(Component.text("You are currently in channel '").color(NamedTextColor.GREEN))
                        .append(currentChannel.name)
                        .append(Component.text("'.").color(NamedTextColor.GREEN))
                        .append(Component.newline())
                        .append(Component.text("Available channels: ").color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD))
                        .append(Component.newline())
                        .append(
                            Component.join(
                                JoinConfiguration.newlines(),
                                channelService.getAvailable(player).map { channel ->
                                    Component.text()
                                        .append(channel.name)
                                        .append(Component.space())
                                        .append(
                                            Component.text()
                                                .append(Component.text("Aliases: ").color(NamedTextColor.GRAY))
                                                .append(
                                                    Component.join(
                                                        JoinConfiguration.commas(true),
                                                        channel.commandAliases.map { Component.text("/$it") },
                                                    ),
                                                ),
                                        )
                                        .clickEvent(ClickEvent.runCommand("/channel ${channel.id}"))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                Component.text()
                                                    .append(Component.text("Switch to channel ").color(NamedTextColor.GRAY))
                                                    .append(channel.name)
                                                    .build(),
                                            ),
                                        ).build()
                                },
                            ),
                        ).build(),
                )
            }.build()
}
