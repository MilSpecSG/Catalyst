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

package org.anvilpowered.catalyst.api.user

import org.anvilpowered.anvil.core.db.MutableRepository
import org.anvilpowered.anvil.core.db.SizedIterable
import org.anvilpowered.anvil.core.user.Player
import java.util.UUID

interface MinecraftUserRepository : MutableRepository<MinecraftUser, MinecraftUser.CreateDto> {

    suspend fun getNickname(id: UUID): String?

    suspend fun updateNickname(id: UUID, nickname: String): Boolean

    suspend fun deleteNickname(id: UUID): Boolean

    suspend fun getAllUsernames(startWith: String = ""): SizedIterable<String>

    suspend fun getByUsername(username: String): MinecraftUser?
}

suspend fun MinecraftUserRepository.getOnlineUser(player: Player): MinecraftUser.Online =
    findById(player.id)?.let { MinecraftUser.Online(it, player) }
        ?: throw IllegalStateException("User ${player.username} with id ${player.id} is not in the database!")
