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

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.anvilpowered.kbrig.Command
import org.anvilpowered.kbrig.context.CommandContext

internal object CommandDefaults {
    fun notEnoughArgs(context: CommandContext<CommandSource>): Int {
        context.source.sendMessage(
            Component.text()
                .append(Component.text("Invalid command usage!\n").color(NamedTextColor.RED))
                .append(
                    Component.text("Not enough arguments in: ").color(NamedTextColor.RED)
                        .append(Component.text(context.input).color(NamedTextColor.YELLOW)),
                ),
        )
        return 1
    }

    fun syntaxError(usage: String): Command<CommandSource> = Command { context ->
        context.source.sendMessage(
            Component.text()
                .append(Component.text("Invalid command usage '${context.input}'").color(NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.text("Usage: ").color(NamedTextColor.RED))
                .append(Component.text(usage).color(NamedTextColor.YELLOW)),
        )
        Command.SINGLE_SUCCESS
    }
}
