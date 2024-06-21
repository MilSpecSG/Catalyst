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

package org.anvilpowered.catalyst.core

import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.core.db.user.MinecraftUsers
import org.anvilpowered.catalyst.core.db.user.Users
import org.apache.logging.log4j.Logger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class CatalystPlugin(
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val logger: Logger,
    private val registrars: List<Registrar>,
) {

    fun initialize() {
        try {
            connectDatabase()
        } catch (e: Exception) {
            logger.error("Could not connect to the database, please check your config!", e)
        }
        registrars.forEach(Registrar::register)
    }

    private val dbDriver = mapOf(
        "postgresql" to "org.postgresql.Driver",
        "mariadb" to "org.mariadb.jdbc.Driver",
    )

    private fun connectDatabase() {
        logger.info("Connecting to database...")
        val dbDriver = checkNotNull(dbDriver[registry[catalystKeys.DB_TYPE]]) {
            "Unknown db type ${registry[catalystKeys.DB_TYPE]}. Available: postgresql, mariadb."
        }
        logger.info("Using database driver $dbDriver")
        Database.connect(
            registry[catalystKeys.DB_URL],
            driver = dbDriver,
            user = registry[catalystKeys.DB_USER],
            password = registry[catalystKeys.DB_PASSWORD],
        )
        logger.info("Finished connecting to database.")
        logger.info("Creating tables...")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                MinecraftUsers,
                Users,
            )
        }
        logger.info("Finished creating tables.")
    }
}
