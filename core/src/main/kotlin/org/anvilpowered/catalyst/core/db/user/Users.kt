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

import org.anvilpowered.catalyst.api.user.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

internal object Users : UUIDTable("catalyst_users") {
    val username = varchar("username", 255).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex().nullable()
    val discordUserId = long("discord_user_id").uniqueIndex().nullable()
    val minecraftUserId = optReference("minecraft_user_id", MinecraftUsers)
}

internal class DBUser(id: EntityID<UUID>) : UUIDEntity(id), User {
    override val uuid: UUID = id.value
    override var username: String by Users.username
    override var email: String? by Users.email
    override var discordUserId: Long? by Users.discordUserId
    override var minecraftUser: DBMinecraftUser? by DBMinecraftUser optionalReferencedOn Users.minecraftUserId

    companion object : UUIDEntityClass<DBUser>(Users, DBUser::class.java, ::DBUser)
}
