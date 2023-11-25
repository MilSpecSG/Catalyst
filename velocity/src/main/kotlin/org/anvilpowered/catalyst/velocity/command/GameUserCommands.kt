/*
 *   Catalyst - AnvilPowered.org
 *   Copyright (C) 2020-2023 Contributors
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

package org.anvilpowered.catalyst.velocity.command

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.anvilpowered.catalyst.api.user.MinecraftUser
import org.anvilpowered.catalyst.api.user.MinecraftUserRepository
import org.anvilpowered.kbrig.argument.StringArgumentType
import org.anvilpowered.kbrig.builder.ArgumentBuilder
import org.anvilpowered.kbrig.builder.RequiredArgumentBuilder
import org.anvilpowered.kbrig.builder.executesSuspending
import org.anvilpowered.kbrig.context.CommandContext
import org.anvilpowered.kbrig.context.get

fun MinecraftUserRepository.argument(
    argumentName: String = "minecraftUser",
    command: suspend (context: CommandContext<CommandSource>, minecraftUser: MinecraftUser) -> Int,
): RequiredArgumentBuilder<CommandSource, String> =
    ArgumentBuilder.required<CommandSource, String>(argumentName, StringArgumentType.SingleWord)
        .suggests { _, builder ->
            getAllUsernames(startWith = builder.input).forEach { name -> builder.suggest(name) }
            builder.build()
        }
        .executesSuspending { context ->
            val minecraftUsername = context.get<String>(argumentName)
            getByUsername(minecraftUsername)?.let { minecraftUser ->
                command(context, minecraftUser)
            } ?: run {
                context.source.sendMessage(
                    Component.text()
                        .append(Component.text("MinecraftUser with name ", NamedTextColor.RED))
                        .append(Component.text(minecraftUsername, NamedTextColor.GOLD))
                        .append(Component.text(" not found!", NamedTextColor.RED)),
                )
                0
            }
        }
