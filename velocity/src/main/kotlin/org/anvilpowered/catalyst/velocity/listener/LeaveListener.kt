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
package org.anvilpowered.catalyst.velocity.listener

import com.google.common.eventbus.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.anvilpowered.anvil.core.LoggerScope
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.velocity.ProxyServerScope
import org.anvilpowered.catalyst.api.chat.LuckpermsService
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.velocity.chat.StaffListService

context(ProxyServerScope, Registry.Scope, StaffListService.Scope, LoggerScope, LuckpermsService.Scope)
class LeaveListener {
    @Subscribe
    fun onPlayerLeave(event: DisconnectEvent) = runBlocking {
        if (event.loginStatus == DisconnectEvent.LoginStatus.PRE_SERVER_JOIN) {
            return@runBlocking
        }
        val player = event.player
        staffListService.removeStaffNames(player.username)
        val leaveMessage = registry[CatalystKeys.LEAVE_MESSAGE].resolvePlaceholders(player)

        if (registry[CatalystKeys.LEAVE_LISTENER_ENABLED]) {
            proxyServer.sendMessage(leaveMessage)
            logger.info(PlainTextComponentSerializer.plainText().serialize(leaveMessage))
        }
    }
}
