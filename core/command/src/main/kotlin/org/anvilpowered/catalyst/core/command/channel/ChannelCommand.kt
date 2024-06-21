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
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.anvilpowered.catalyst.api.chat.ChannelService
import org.anvilpowered.catalyst.api.config.ChatChannel
import org.anvilpowered.kbrig.builder.RequiredArgumentBuilder
import org.anvilpowered.kbrig.context.CommandContextScopeDsl
import org.anvilpowered.kbrig.context.CommandExecutionScope
import org.anvilpowered.kbrig.context.get
import org.anvilpowered.kbrig.context.yieldError
import org.anvilpowered.kbrig.suggestion.CommandSuggestionScope
import org.anvilpowered.kbrig.suggestion.suggestsScoped

fun <P : Player?> RequiredArgumentBuilder<CommandSource, String>.suggestChannelArgument(
    channelService: ChannelService,
    playerExtractor: suspend CommandSuggestionScope<CommandSource>.() -> P,
): RequiredArgumentBuilder<CommandSource, String> =
    suggestsScoped { channelService.getAvailable(playerExtractor()).suggestAllFiltered { it.id } }

@CommandContextScopeDsl
suspend fun CommandExecutionScope<CommandSource>.extractChannelArgument(
    channelService: ChannelService,
    argumentName: String = "channel",
    playerProvider: suspend CommandExecutionScope<CommandSource>.() -> Player?,
): ChatChannel {
    val channelId = context.get<String>(argumentName)
    val channel = channelService[channelId]
    if (channel == null) {
        context.source.sendMessage(
            Component.text()
                .append(Component.text("Channel with id ", NamedTextColor.RED))
                .append(Component.text(channelId, NamedTextColor.GOLD))
                .append(Component.text(" not found!", NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.text("Available channels: ", NamedTextColor.GRAY, TextDecoration.BOLD))
                .append(
                    Component.join(
                        JoinConfiguration.commas(true),
                        channelService.getAvailable(playerProvider())
                            .map { Component.text(it.id, NamedTextColor.GOLD) },
                    ),
                )
                .build(),
        )
        yieldError()
    }
    return channel
}
