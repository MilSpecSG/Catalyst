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

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.anvilpowered.anvil.core.command.CommandSource
import org.anvilpowered.anvil.core.command.extractPlayerSource
import org.anvilpowered.anvil.core.command.extractPlayerSourceOrNull
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.core.user.Player
import org.anvilpowered.anvil.core.user.hasPermissionNotSet
import org.anvilpowered.catalyst.api.chat.ChannelService
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.api.config.ChatChannel
import org.anvilpowered.kbrig.context.CommandContextScopeDsl
import org.anvilpowered.kbrig.context.CommandExecutionScope
import org.anvilpowered.kbrig.context.yieldError
import org.anvilpowered.kbrig.context.yieldSuccess

class ChannelSwitchFrontend(
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val channelService: ChannelService,
) {
    context(CommandExecutionScope<CommandSource>)
    @CommandContextScopeDsl
    suspend fun executeChannelSwitch(
        playerProvider: suspend () -> Player = { extractPlayerSource() },
        channelProvider: suspend (Player) -> ChatChannel = { player -> extractChannelArgument(channelService) { player } },
    ) {
        val targetPlayer = playerProvider()
        val targetChannel = channelProvider(targetPlayer)
        val targetSelf = extractPlayerSourceOrNull()?.id == targetPlayer.id

        if (channelService.getForPlayer(targetPlayer.id).id == targetChannel.id) {
            if (targetSelf) {
                context.source.sendMessage(
                    Component.text()
                        .append(Component.text("You are already in channel '").color(NamedTextColor.RED))
                        .append(targetChannel.name)
                        .append(Component.text("'.").color(NamedTextColor.RED))
                        .build(),
                )
            } else {
                context.source.sendMessage(
                    Component.text()
                        .append(Component.text("Player ").color(NamedTextColor.RED))
                        .append(Component.text(targetPlayer.username).color(NamedTextColor.GOLD))
                        .append(Component.text(" is already in channel '").color(NamedTextColor.RED))
                        .append(targetChannel.name)
                        .append(Component.text("'.").color(NamedTextColor.RED))
                        .build(),
                )
            }
            yieldError()
        }

        if (targetPlayer.hasPermissionNotSet("${registry[catalystKeys.PERMISSION_CHANNEL_PREFIX]}.${targetChannel.id}")) {
            if (targetSelf) {
                context.source.sendMessage(
                    Component.text()
                        .append(Component.text("You don't have permission to join channel '").color(NamedTextColor.RED))
                        .append(targetChannel.name)
                        .append(Component.text("'.").color(NamedTextColor.RED))
                        .build(),
                )
            } else {
                context.source.sendMessage(
                    Component.text()
                        .append(Component.text("Player ").color(NamedTextColor.RED))
                        .append(Component.text(targetPlayer.username).color(NamedTextColor.GOLD))
                        .append(Component.text(" doesn't have permission to join channel '").color(NamedTextColor.RED))
                        .append(targetChannel.name)
                        .append(Component.text("'.").color(NamedTextColor.RED))
                        .build(),
                )
            }
            yieldError()
        }

        if (!targetSelf) {
            context.source.sendMessage(
                Component.text()
                    .append(Component.text("Switching ").color(NamedTextColor.GREEN))
                    .append(Component.text(targetPlayer.username).color(NamedTextColor.GOLD))
                    .append(Component.text(" from channel '").color(NamedTextColor.GREEN))
                    .append(channelService.getForPlayer(targetPlayer.id).name)
                    .append(Component.text("' -> '").color(NamedTextColor.GREEN))
                    .append(targetChannel.name)
                    .append(Component.text("'.").color(NamedTextColor.GREEN))
                    .build(),
            )
        }

        targetPlayer.sendMessage(
            Component.text()
                .append(Component.text("You have switched from channel '").color(NamedTextColor.GREEN))
                .append(channelService.getForPlayer(targetPlayer.id).name)
                .append(Component.text("' -> '").color(NamedTextColor.GREEN))
                .append(targetChannel.name)
                .append(Component.text("'.").color(NamedTextColor.GREEN))
                .build(),
        )

        channelService.switch(targetPlayer.id, targetChannel.id)
        yieldSuccess()
    }
}
