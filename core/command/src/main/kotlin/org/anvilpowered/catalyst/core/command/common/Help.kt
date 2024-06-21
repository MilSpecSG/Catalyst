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

package org.anvilpowered.catalyst.core.command.common

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.anvilpowered.catalyst.api.PluginMessages
import org.anvilpowered.kbrig.Command
import org.anvilpowered.kbrig.builder.ArgumentBuilder

fun <B : ArgumentBuilder<CommandSource, B>> B.addHelpChild(usage: String): B = then(
    ArgumentBuilder.literal<CommandSource>("help")
        .executes(usage(usage)),
)

private fun usage(usage: String): Command<CommandSource> = Command { context ->
    context.source.sendMessage(
        Component.text()
            .append(PluginMessages.pluginPrefix)
            .append(Component.text("Command Usage", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text(usage, NamedTextColor.AQUA)),
    )
    Command.SINGLE_SUCCESS
}
