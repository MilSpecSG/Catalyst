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

package org.anvilpowered.catalyst.core.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.anvilpowered.anvil.core.command.CommandSource
import org.anvilpowered.anvil.core.command.extractPlayerSource
import org.anvilpowered.catalyst.api.user.MinecraftUser
import org.anvilpowered.catalyst.api.user.MinecraftUserRepository
import org.anvilpowered.kbrig.argument.StringArgumentType
import org.anvilpowered.kbrig.builder.ArgumentBuilder
import org.anvilpowered.kbrig.builder.RequiredArgumentBuilder
import org.anvilpowered.kbrig.context.CommandContext
import org.anvilpowered.kbrig.context.CommandContextScopeDsl
import org.anvilpowered.kbrig.context.CommandExecutionScope
import org.anvilpowered.kbrig.context.executesScoped
import org.anvilpowered.kbrig.context.get
import org.anvilpowered.kbrig.context.yieldError
import org.anvilpowered.kbrig.suggestion.suggestsScoped

fun ArgumentBuilder.Companion.requireMinecraftUserArgument(
    minecraftUserRepository: MinecraftUserRepository,
    argumentName: String = "minecraftUser",
    command: suspend (context: CommandContext<CommandSource>, minecraftUser: MinecraftUser) -> Int,
): RequiredArgumentBuilder<CommandSource, String> =
    required<CommandSource, String>(argumentName, StringArgumentType.SingleWord)
        .suggestMinecraftUserArgument(minecraftUserRepository)
        .executesScoped { yield(command(context, extractMinecraftUserArgument(minecraftUserRepository, argumentName))) }

fun ArgumentBuilder.Companion.requireMinecraftUserArgumentScoped(
    minecraftUserRepository: MinecraftUserRepository,
    argumentName: String = "minecraftUser",
    command: suspend CommandExecutionScope<CommandSource>.(minecraftUser: MinecraftUser) -> Unit,
): RequiredArgumentBuilder<CommandSource, String> =
    required<CommandSource, String>(argumentName, StringArgumentType.SingleWord)
        .suggestMinecraftUserArgument(minecraftUserRepository)
        .executesScoped { command(extractMinecraftUserArgument(minecraftUserRepository, argumentName)) }

fun RequiredArgumentBuilder<CommandSource, String>.suggestMinecraftUserArgument(
    minecraftUserRepository: MinecraftUserRepository,
): RequiredArgumentBuilder<CommandSource, String> =
    suggestsScoped { minecraftUserRepository.getAllUsernames(startWith = remainingLowerCase).suggestAll() }

@CommandContextScopeDsl
suspend fun CommandExecutionScope<CommandSource>.extractMinecraftUserArgument(
    minecraftUserRepository: MinecraftUserRepository,
    argumentName: String = "minecraftUser",
): MinecraftUser {
    val minecraftUsername = context.get<String>(argumentName)
    val user = minecraftUserRepository.getByUsername(minecraftUsername)
    if (user == null) {
        context.source.sendMessage(
            Component.text()
                .append(Component.text("MinecraftUser with name ", NamedTextColor.RED))
                .append(Component.text(minecraftUsername, NamedTextColor.GOLD))
                .append(Component.text(" not found!", NamedTextColor.RED))
                .build(),
        )
        yieldError()
    }
    return user
}

@CommandContextScopeDsl
suspend fun CommandExecutionScope<CommandSource>.extractMinecraftUserSource(
    minecraftUserRepository: MinecraftUserRepository,
): MinecraftUser {
    val player = extractPlayerSource()
    val user = minecraftUserRepository.findById(player.id)
    if (user == null) {
        context.source.sendMessage(
            Component.text()
                .append(Component.text("MinecraftUser with name ", NamedTextColor.RED))
                .append(Component.text(player.username, NamedTextColor.GOLD))
                .append(Component.text(" not found!", NamedTextColor.RED))
                .build(),
        )
        yieldError()
    }
    return user
}
