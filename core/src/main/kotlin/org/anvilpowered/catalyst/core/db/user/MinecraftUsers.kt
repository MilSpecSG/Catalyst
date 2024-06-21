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

import org.anvilpowered.catalyst.api.user.MinecraftUser
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

internal object MinecraftUsers : UUIDTable("catalyst_minecraft_users") {
    val username = varchar("username", 255).uniqueIndex()
    val ipAddress = varchar("ip_address", 255)
    val nickname = varchar("nickname", 255).nullable()
}

internal class DBMinecraftUser(id: EntityID<UUID>) : UUIDEntity(id), MinecraftUser {
    override val uuid: UUID = id.value
    override var username: String by MinecraftUsers.username
    override var ipAddress: String by MinecraftUsers.ipAddress
    override var nickname: String? by MinecraftUsers.nickname

    companion object : UUIDEntityClass<DBMinecraftUser>(MinecraftUsers, DBMinecraftUser::class.java, ::DBMinecraftUser)
}
