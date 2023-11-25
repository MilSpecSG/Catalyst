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

package org.anvilpowered.catalyst.api.chat.placeholder

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.anvilpowered.catalyst.api.chat.LuckpermsService
import org.apache.logging.log4j.Logger

@Serializable(with = PlayerFormat.Serializer::class)
open class PlayerFormat(
    override val format: Component,
    private val placeholders: ConcretePlaceholders = ConcretePlaceholders(),
) : MessageFormat {

    suspend fun resolve(
        proxyServer: ProxyServer,
        logger: Logger,
        luckpermsService: LuckpermsService,
        player: Player,
    ): Component = resolve(proxyServer, logger, luckpermsService, format, placeholders, player)

    companion object : MessageFormat.Builder<ConcretePlaceholders, PlayerFormat> {

        private val backendServerContext = NestedFormat(BackendServerFormat, ConcretePlaceholders::backendServer)
        private val proxyServerContext = NestedFormat(ProxyServerFormat, ConcretePlaceholders::proxyServer)

        suspend fun resolve(
            proxyServer: ProxyServer,
            logger: Logger,
            luckpermsService: LuckpermsService,
            format: Component,
            placeholders: ConcretePlaceholders,
            player: Player,
        ): Component {
            val backendFormat: (suspend Component.() -> Component)? = player.currentServer.orElse(null)?.server?.let {
                { backendServerContext.format.resolvePlaceholders(format, backendServerContext.placeholderResolver(placeholders), it) }
            }

            if (backendFormat == null) {
                logger.error("Could not resolve backend placeholders for ${player.username} because they are not connected to a server.")
            }

            return sequenceOf(
                backendFormat,
                { proxyServerContext.format.resolve(proxyServer, this, proxyServerContext.placeholderResolver(placeholders)) },
                { replaceText { it.match(placeholders.latency).replacement(player.ping.toString()) } },
                { replaceText { it.match(placeholders.username).replacement(player.username) } },
                { replaceText { it.match(placeholders.id).replacement(player.uniqueId.toString()) } },
                { replaceText { it.match(placeholders.prefix).replacement(luckpermsService.prefix(player.uniqueId)) } },
                { replaceText { it.match(placeholders.suffix).replacement(luckpermsService.suffix(player.uniqueId)) } },
            ).filterNotNull().fold(format) { acc, transform -> transform(acc) }
        }

        override fun build(block: ConcretePlaceholders.() -> Component): PlayerFormat {
            val placeholders = ConcretePlaceholders()
            return PlayerFormat(block(placeholders), placeholders)
        }
    }

    object Serializer : MessageFormat.Serializer<PlayerFormat>(::PlayerFormat)

    interface Placeholders {
        val backendServer: BackendServerFormat.Placeholders
        val proxyServer: ProxyServerFormat.Placeholders

        val latency: Placeholder
        val username: Placeholder
        val id: Placeholder
        val prefix: Placeholder
        val suffix: Placeholder
    }

    class ConcretePlaceholders internal constructor(path: List<String> = listOf()) :
        MessageFormat.Placeholders<PlayerFormat>, Placeholders {

        private val pathPrefix = path.joinToString { "$it." }

        override val backendServer = BackendServerFormat.Placeholders(path + listOf("backendServer"))
        override val proxyServer = ProxyServerFormat.Placeholders(path + listOf("proxyServer"))

        override val latency: Placeholder = "%${pathPrefix}ping%"
        override val username: Placeholder = "%${pathPrefix}username%"
        override val id: Placeholder = "%${pathPrefix}id%"
        override val prefix: Placeholder = "%${pathPrefix}prefix%"
        override val suffix: Placeholder = "%${pathPrefix}suffix%"
    }
}
