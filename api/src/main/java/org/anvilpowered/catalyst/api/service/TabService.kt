/*
 *   Catalyst - AnvilPowered
 *   Copyright (C) 2021
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package org.anvilpowered.catalyst.api.service

import net.kyori.adventure.text.Component
import org.anvilpowered.anvil.core.user.Player

interface TabService {
    fun format(player: Player, ping: Int, playerCount: Int): Component?
    fun formatCustom(format: Component, player: Player, ping: Int, playerCount: Int): Component?
    fun formatHeader(player: Player, ping: Int, playerCount: Int): Component?
    fun formatFooter(player: Player, ping: Int, playerCount: Int): Component?
}
