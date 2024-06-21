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

import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.proxy.chat.ChannelSwitchFrontend
import org.anvilpowered.catalyst.proxy.registrar.Registrar
import org.anvilpowered.kbrig.brigadier.toBrigadier
import org.anvilpowered.kbrig.tree.LiteralCommandNode
import org.apache.logging.log4j.Logger

class ChannelAliasCommandRegistrar(
    private val proxyServer: ProxyServer,
    private val logger: Logger,
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val channelSwitchFrontend: ChannelSwitchFrontend,
) : Registrar {
    private fun LiteralCommandNode<CommandSource>.register() = proxyServer.commandManager.register(BrigadierCommand(toBrigadier()))

    override fun register() {
        logger.info("Building channel alias command trees and registering commands...")
        registry[catalystKeys.CHAT_CHANNELS].values.forEach { channel ->
            channel.commandAliases.forEach { alias ->
                ChannelAliasCommandFactory(registry, catalystKeys, channelSwitchFrontend, alias, channel).create().register()
            }
        }
        logger.info("Finished registering channel alias commands.")
    }
}
