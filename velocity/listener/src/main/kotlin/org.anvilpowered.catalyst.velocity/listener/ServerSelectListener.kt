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
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import kotlinx.coroutines.runBlocking

class ServerSelectListener {
    @Subscribe
    fun onPlayerJoin(event: PlayerChooseInitialServerEvent) = runBlocking {
        // TODO: What is this doing?
//        val player = event.player
//        event.initialServer.map { rs: RegisteredServer ->
//            rs.ping().exceptionally { null }
//                .thenAcceptAsync { s: ServerPing? ->
//                    if (s == null || s.version == null) {
//                        event.player.disconnect(Component.text("Failed to connect."))
//                    } else {
//                        val virtualHost = player.virtualHost
//                        if (virtualHost.isPresent) {
//                            eventBus.post(JoinEvent(player.toAnvilPlayer(), virtualHost.get().hostName))
//                        }
//                    }
//                }.join()
//        }
    }
}
