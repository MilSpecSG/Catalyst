/*
 *   Anvil - AnvilPowered.org
 *   Copyright (C) 2019-2023 Contributors
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

package org.anvilpowered.anvil.db.user

import org.anvilpowered.anvil.user.GameUser
import org.anvilpowered.anvil.user.GameUserRepository
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.mapLazy
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.sourcegrade.kontour.SizedIterable
import org.sourcegrade.kontour.UUID

object GameUserRepositoryImpl : GameUserRepository {
    override suspend fun getAllUserNames(startWith: String): SizedIterable<String> = newSuspendedTransaction {
        GameUserEntity.find { GameUserTable.username like "$startWith%" }.mapLazy { it.username }
    }

    override suspend fun findByUsername(username: String): GameUser? = newSuspendedTransaction {
        GameUserTable.select { GameUserTable.username eq username }
            .firstOrNull()
            ?.toGameUser()
    }

    override suspend fun countAll(): Long = newSuspendedTransaction {
        GameUserEntity.all().count()
    }

    override suspend fun create(item: GameUser): GameUser = newSuspendedTransaction {
        GameUserEntity.new(item.id) {
            user = UserEntity[item.userId]
            gameType = item.gameType
            username = item.username
        }
        item
    }

    override suspend fun exists(id: UUID): Boolean =
        newSuspendedTransaction { GameUserEntity.findById(id) != null }

    override suspend fun deleteById(id: UUID): Boolean = newSuspendedTransaction {
        GameUserTable.deleteWhere { GameUserTable.id eq id } > 0
    }
}
