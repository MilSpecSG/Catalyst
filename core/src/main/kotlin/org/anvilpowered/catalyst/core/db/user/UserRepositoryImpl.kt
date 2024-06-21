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
import org.anvilpowered.anvil.core.db.Pagination
import org.anvilpowered.catalyst.api.user.User
import org.anvilpowered.catalyst.api.user.UserRepository
import org.apache.logging.log4j.Logger
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class UserRepositoryImpl(private val logger: Logger) : UserRepository {
    override suspend fun paginate(): Pagination<User> {
        TODO("Not yet implemented")
    }

    override suspend fun create(item: User.CreateDto): User = newSuspendedTransaction {
        val itemMinecraftUser = item.minecraftUserId?.let { id -> DBMinecraftUser.findById(id) }

        val user = DBUser.new {
            username = item.username
            email = item.email
            discordUserId = item.discordUserId
            minecraftUser = itemMinecraftUser
        }

        logger.info("Created new User ${user.uuid} with data $item")

        user
    }

    override suspend fun put(item: User.CreateDto): MutableRepository.PutResult<User> = newSuspendedTransaction {
        val existingUser = getByUsername(item.username)
        if (existingUser == null) {
            MutableRepository.PutResult(create(item), created = true)
        } else {
            // update the existing User
            logger.info("Found existing User ${existingUser.uuid} with username ${existingUser.username}")

            if (existingUser.email != item.email) {
                Users.update({ Users.id eq existingUser.uuid }) {
                    logger.info("Updating email for User ${existingUser.uuid} from ${existingUser.email} to ${item.email}")
                    it[email] = item.email
                }
            }

            if (existingUser.discordUserId != item.discordUserId) {
                Users.update({ Users.id eq existingUser.uuid }) {
                    logger.info("Updating discordUserId for User ${existingUser.uuid} from ${existingUser.discordUserId} to ${item.discordUserId}")
                    it[discordUserId] = item.discordUserId
                }
            }

            if (existingUser.minecraftUser?.uuid != item.minecraftUserId) {
                Users.update({ Users.id eq existingUser.uuid }) {
                    logger.info("Updating minecraftUserId for User ${existingUser.uuid} from ${existingUser.minecraftUser?.uuid} to ${item.minecraftUserId}")
                    it[minecraftUserId] = item.minecraftUserId
                }
            }

            MutableRepository.PutResult(existingUser, created = false)
        }
    }

    override suspend fun findById(id: UUID): User? = newSuspendedTransaction {
        DBUser.findById(id)
    }

    override suspend fun getByUsername(username: String): User? = newSuspendedTransaction {
        DBUser.find { Users.username eq username }.firstOrNull()
    }

    override suspend fun getByEmail(email: String): User? = newSuspendedTransaction {
        DBUser.find { Users.email eq email }.firstOrNull()
    }

    override suspend fun getByDiscordUserId(id: Long): User? = newSuspendedTransaction {
        DBUser.find { Users.discordUserId eq id }.firstOrNull()
    }

    override suspend fun getByMinecraftUserId(id: UUID): User? = newSuspendedTransaction {
        DBUser.find { Users.minecraftUserId eq id }.firstOrNull()
    }

    override suspend fun countAll(): Long = newSuspendedTransaction { DBUser.all().count() }

    override suspend fun exists(id: UUID): Boolean = newSuspendedTransaction {
        DBUser.findById(id) != null
    }

    override suspend fun deleteById(id: UUID): Boolean = newSuspendedTransaction {
        Users.deleteWhere { Users.id eq id } > 0
    }
}
