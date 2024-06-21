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

package org.anvilpowered.catalyst.breeze.proxy

import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.proxy.ProxyServer
import org.anvilpowered.catalyst.core.Registrar
import org.anvilpowered.catalyst.proxy.listener.ChatListener
import org.anvilpowered.catalyst.proxy.listener.CommandListener
import org.anvilpowered.catalyst.proxy.listener.DiscordListener
import org.anvilpowered.catalyst.proxy.listener.JoinListener
import org.anvilpowered.catalyst.proxy.listener.LeaveListener
import org.apache.logging.log4j.Logger

class ListenerRegistrar(
    private val proxyServer: ProxyServer,
    private val pluginContainer: PluginContainer,
    private val logger: Logger,
    private val chatListener: ChatListener,
    private val commandListener: CommandListener,
    private val discordListener: DiscordListener,
    private val joinListener: JoinListener,
    private val leaveListener: LeaveListener,
) : Registrar {

    override fun register() {
        logger.info("Registering listeners...")
        proxyServer.eventManager.register(pluginContainer, chatListener)
        proxyServer.eventManager.register(pluginContainer, commandListener)
        proxyServer.eventManager.register(pluginContainer, discordListener)
        proxyServer.eventManager.register(pluginContainer, joinListener)
        proxyServer.eventManager.register(pluginContainer, leaveListener)
        logger.info("Finished registering listeners.")
    }
}
