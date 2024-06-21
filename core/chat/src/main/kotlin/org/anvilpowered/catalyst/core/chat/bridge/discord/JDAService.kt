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
package org.anvilpowered.catalyst.core.chat.bridge.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.Compression
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.anvilpowered.anvil.core.command.CommandExecutor
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.core.user.PlayerService
import org.anvilpowered.catalyst.api.chat.ChannelService
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException

class JDAService(
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val logger: Logger,
    private val playerService: PlayerService,
    private val channelService: ChannelService,
    private val commandExecutor: CommandExecutor,
) {

    private var isLoaded = false
    var jda: JDA? = null

    init {
        enableDiscordBot()
    }

    private fun enableDiscordBot() {
        if (!registry[catalystKeys.CHAT_DISCORD_ENABLED]) {
            logger.warn("The discord bot is disabled! Chat will not be transmitted from in-game to discord.")
            return
        } else {
            logger.info("The discord bot is enabled!")
        }
        if (isLoaded) {
            jda?.shutdownNow()
        }
        try {
            val builder = JDABuilder.createDefault(registry[catalystKeys.CHAT_DISCORD_BOT_TOKEN])
            builder.setCompression(Compression.NONE)
            builder.setBulkDeleteSplittingEnabled(false)
            builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
            builder.enableIntents(GatewayIntent.MESSAGE_CONTENT)
            jda = builder.build()
        } catch (e: LoginException) {
            e.printStackTrace()
        }
        val nowPlaying = registry[catalystKeys.CHAT_DISCORD_BOT_STATUS]
        val playerCount = playerService.getAll().count().let { count ->
            if (count == 0) {
                registry[catalystKeys.CHAT_DISCORD_TOPIC_NOPLAYERS]
            } else {
                count.toString()
            }
        }
        jda!!.presence.activity = Activity.playing(nowPlaying.replace("%players%".toRegex(), playerCount))
        jda!!.addEventListener(createListener())
        isLoaded = true
        if (registry[catalystKeys.CHAT_DISCORD_TOPIC_ENABLED]) {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                updateTopic(),
                1,
                registry[catalystKeys.CHAT_DISCORD_TOPIC_REFRESHRATE].toLong(),
                TimeUnit.MINUTES,
            )
        }
        try {
            jda?.awaitReady()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        logger.info("Successfully connected to discord")
    }

    private fun updateTopic(): Runnable {
        val channelId = registry[catalystKeys.CHAT_DEFAULT_CHANNEL]
        if (channelId.isEmpty()) {
            throw IllegalStateException("Default chat channel must not be empty!")
        }
        // TODO: Clean up the entire class
        return Runnable {
            val channel = jda!!.getTextChannelById(channelService.get(channelId)?.discordChannelId ?: "")
            val nowPlaying = registry[catalystKeys.CHAT_DISCORD_BOT_STATUS]
            val playerCount = playerService.count().let { count ->
                if (count == 0) {
                    registry[catalystKeys.CHAT_DISCORD_TOPIC_NOPLAYERS]
                } else {
                    count.toString()
                }
            }
            if (nowPlaying.contains("%players%")) {
                jda!!.presence.activity = Activity.playing(nowPlaying.replace("%players%".toRegex(), playerCount))
            }
            if (channel == null) {
                logger.error("Could not update the main channel topic!")
            } else {
                channel.manager.setTopic(
                    registry[catalystKeys.CHAT_DISCORD_TOPIC_FORMAT]
                        .replace("%players%", playerCount),
                ).queue()
            }
        }
    }

    private fun createListener(): DiscordListener {
        return DiscordListener(
            registry,
            catalystKeys,
            logger,
            playerService,
            channelService,
            this,
            commandExecutor,
        )
    }
}
