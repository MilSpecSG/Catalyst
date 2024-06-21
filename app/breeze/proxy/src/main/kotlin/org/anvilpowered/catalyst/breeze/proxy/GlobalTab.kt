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

package org.anvilpowered.catalyst.breeze.proxy

import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.player.TabList
import com.velocitypowered.api.proxy.player.TabListEntry
import com.velocitypowered.api.util.GameProfile
import kotlinx.coroutines.runBlocking
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.catalyst.api.chat.placeholder.PlayerFormat
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.apache.logging.log4j.Logger
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class GlobalTab(
    private val proxyServer: ProxyServer,
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val pluginContainer: PluginContainer,
    private val logger: Logger,
    private val playerFormatResolver: PlayerFormat.Resolver,
) {

    init {
        if (registry[catalystKeys.TAB_ENABLED]) {
            logger.info("Global tab enabled")
            schedule()
        } else {
            logger.info("Global tab disabled")
        }
    }

    private fun insertIntoTab(list: TabList, entry: TabListEntry) {
        val inUUID = entry.profile.id
        val contained: MutableList<UUID> = ArrayList()
        val cache: MutableMap<UUID, TabListEntry> = HashMap()
        for (e in list.entries) {
            contained.add(e.profile.id)
            cache[e.profile.id] = e
        }
        if (!contained.contains(inUUID)) {
            list.addEntry(entry)
        } else {
            val tabListEntry = cache[inUUID]
            if (tabListEntry?.displayNameComponent != entry.displayNameComponent) {
                list.removeEntry(inUUID)
                list.addEntry(entry)
            }
        }
    }

    private fun schedule() {
        proxyServer.scheduler.buildTask(pluginContainer, Consumer { runBlocking { action() } }).repeat(10, TimeUnit.SECONDS).schedule()
    }

    private suspend fun action() {
        val playerCount = proxyServer.playerCount
        if (playerCount == 0) {
            return
        }
        for (displayPlayer in proxyServer.allPlayers) {
            for (entry in displayPlayer.tabList.entries) {
                displayPlayer.tabList.removeEntry(entry.profile.id)
            }
            for (processPlayer in proxyServer.allPlayers) {
                val currentEntry = TabListEntry.builder()
                    .profile(GameProfile(processPlayer.uniqueId, processPlayer.username, processPlayer.gameProfileProperties))
                    .displayName(playerFormatResolver.resolve(registry[catalystKeys.TAB_FORMAT_PLAYER], processPlayer))
                    .tabList(displayPlayer.tabList)
                    .build()
                insertIntoTab(displayPlayer.tabList, currentEntry)
            }
            var x = 0
            for (customFormat in registry[catalystKeys.TAB_FORMAT_CUSTOM]) {
                x++
                val tabProfile = GameProfile.forOfflinePlayer("catalyst$x")
                val currentEntry = TabListEntry.builder()
                    .profile(tabProfile)
                    .displayName(
                        playerFormatResolver.resolve(customFormat, displayPlayer),
                    )
                    .tabList(displayPlayer.tabList)
                    .build()
                insertIntoTab(displayPlayer.tabList, currentEntry)
            }
            displayPlayer.sendPlayerListHeaderAndFooter(
                playerFormatResolver.resolve(registry[catalystKeys.TAB_HEADER], displayPlayer),
                playerFormatResolver.resolve(registry[catalystKeys.TAB_FOOTER], displayPlayer),
            )
        }
    }
}
