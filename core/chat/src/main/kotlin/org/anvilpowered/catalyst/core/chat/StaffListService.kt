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

package org.anvilpowered.catalyst.core.chat

import net.kyori.adventure.text.Component
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.core.user.PlayerService
import org.anvilpowered.anvil.core.user.hasPermissionSet
import org.anvilpowered.catalyst.api.config.CatalystKeys
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class StaffListService(
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val playerService: PlayerService,
) {

    init {
        val executor = Executors.newSingleThreadScheduledExecutor()
        executor.schedule<Unit>({ update() }, 10, TimeUnit.MINUTES)
    }

    private var staffNames: MutableList<Component> = mutableListOf()
    private var adminNames: MutableList<Component> = mutableListOf()
    private var ownerNames: MutableList<Component> = mutableListOf()

    fun staffNames(): List<Component> = staffNames
    fun adminNames(): List<Component> = adminNames
    fun ownerNames(): List<Component> = ownerNames

    fun getStaffNames(player: String, adminPermission: Boolean, staffPermission: Boolean, ownerPermission: Boolean) {
        if (ownerPermission) {
            ownerNames.add(Component.text(player))
        } else if (staffPermission) {
            if (adminPermission) {
                adminNames.add(Component.text(player))
            } else {
                staffNames.add(Component.text(player))
            }
        }
    }

    private fun update() {
        // TODO: Revise and make sure no duplicates occur
        playerService.getAll().forEach { player ->
            getStaffNames(
                player.username,
                player.hasPermissionSet(registry[catalystKeys.PERMISSION_STAFFLIST_ADMIN]),
                player.hasPermissionSet(registry[catalystKeys.PERMISSION_STAFFLIST_STAFF]),
                player.hasPermissionSet(registry[catalystKeys.PERMISSION_STAFFLIST_OWNER]),
            )
        }
    }

    fun removeStaffNames(player: String) {
        staffNames.remove(Component.text(player))
        adminNames.remove(Component.text(player))
        ownerNames.remove(Component.text(player))
    }

    interface Scope {
        val staffListService: StaffListService
    }
}
