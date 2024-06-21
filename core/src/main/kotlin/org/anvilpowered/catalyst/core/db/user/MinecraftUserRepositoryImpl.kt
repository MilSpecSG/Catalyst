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

package org.anvilpowered.catalyst.core.db.user

import org.anvilpowered.anvil.core.db.MutableRepository
import org.anvilpowered.anvil.core.db.SizedIterable
import org.anvilpowered.catalyst.api.user.MinecraftUser
import org.anvilpowered.catalyst.api.user.MinecraftUserRepository
import org.anvilpowered.catalyst.core.db.wrap
import org.apache.logging.log4j.Logger
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.mapLazy
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class MinecraftUserRepositoryImpl(private val logger: Logger) : MinecraftUserRepository {

    override suspend fun create(item: MinecraftUser.CreateDto): MinecraftUser = newSuspendedTransaction {
        val user = DBMinecraftUser.new(item.id) {
            username = item.username
            ipAddress = item.ipAddress
        }

        logger.info("Created new MinecraftUser ${item.id} with data $item")

        user
    }

    override suspend fun put(item: MinecraftUser.CreateDto): MutableRepository.PutResult<MinecraftUser> = newSuspendedTransaction {
        val existingUser = findById(item.id)
        if (existingUser == null) {
            // create a new MinecraftUser and accompanying User
            val newUser = DBMinecraftUser.new(item.id) {
                username = item.username
                ipAddress = item.ipAddress
            }

            // TODO: What if the user already exists?

            val user = DBUser.new {
                username = item.username
                minecraftUser = newUser
            }

            logger.info("Created new MinecraftUser ${item.id} with data $item and accompanying User ${user.uuid}")

            MutableRepository.PutResult(newUser, created = true)
        } else {
            logger.info("Found existing MinecraftUser ${item.id} with username ${item.username}")
            // update the existing MinecraftUser
            if (existingUser.ipAddress != item.ipAddress) {
                MinecraftUsers.update({ MinecraftUsers.id eq item.id }) {
                    logger.info("Updating IP address of MinecraftUser ${item.id} from ${existingUser.ipAddress} to ${item.ipAddress}")
                    it[ipAddress] = item.ipAddress
                }
            }

            if (existingUser.username != item.username) {
                MinecraftUsers.update({ MinecraftUsers.id eq item.id }) {
                    logger.info("Updating username of MinecraftUser ${item.id} from ${existingUser.username} to ${item.username}")
                    it[username] = item.username
                }
            }

            MutableRepository.PutResult(existingUser, created = false)
        }
    }

    override suspend fun getNickname(id: UUID): String? = newSuspendedTransaction {
        MinecraftUsers.selectAll().where { MinecraftUsers.id eq id }
            .firstOrNull()
            ?.getOrNull(MinecraftUsers.nickname)
    }

    override suspend fun updateNickname(id: UUID, nickname: String): Boolean = newSuspendedTransaction {
        MinecraftUsers.update({ MinecraftUsers.id eq id }) {
            it[MinecraftUsers.nickname] = nickname
        } > 0
    }

    override suspend fun deleteNickname(id: UUID): Boolean = newSuspendedTransaction {
        MinecraftUsers.update({ MinecraftUsers.id eq id }) {
            it[nickname] = null
        } > 0
    }

    override suspend fun getAllUsernames(startWith: String): SizedIterable<String> = newSuspendedTransaction {
        DBMinecraftUser.find { MinecraftUsers.username like "$startWith%" }.mapLazy { it.username }
    }.wrap()

    override suspend fun getByUsername(username: String): MinecraftUser? = newSuspendedTransaction {
        DBMinecraftUser.find { MinecraftUsers.username eq username }.firstOrNull()
    }

    override suspend fun countAll(): Long = newSuspendedTransaction {
        DBMinecraftUser.all().count()
    }

    override suspend fun exists(id: UUID): Boolean =
        newSuspendedTransaction { DBMinecraftUser.findById(id) != null }

    override suspend fun findById(id: UUID): MinecraftUser? = newSuspendedTransaction {
        DBMinecraftUser.findById(id)
    }

    override suspend fun deleteById(id: UUID): Boolean = newSuspendedTransaction {
        MinecraftUsers.deleteWhere { MinecraftUsers.id eq id } > 0
    }
}
