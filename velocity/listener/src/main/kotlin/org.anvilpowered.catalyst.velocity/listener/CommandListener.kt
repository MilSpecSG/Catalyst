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

package org.anvilpowered.catalyst.velocity.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.proxy.Player
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.apache.logging.log4j.Logger

class CommandListener(
    private val logger: Logger,
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
) {

    @Subscribe
    fun onCommandExecution(event: CommandExecuteEvent) {
        if (registry[catalystKeys.COMMAND_LOGGING_ENABLED]) {
            val commandList = registry[catalystKeys.COMMAND_LOGGING_FILTER]
            if (commandList.size == 1 && commandList[0] == "*" || commandList.contains(event.command)) {
                logger.info((event.commandSource as? Player)?.username + " executed command : " + event.command)
            }
        }
    }
}
