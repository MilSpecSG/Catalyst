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

import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.anvilpowered.anvil.core.command.CommandExecutor
import org.anvilpowered.anvil.core.command.withLogging
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.core.user.PlayerService
import org.anvilpowered.catalyst.api.chat.ChannelService
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.apache.logging.log4j.Logger

// No DI
class DiscordListener(
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val logger: Logger,
    private val playerService: PlayerService,
    private val channelService: ChannelService,
    private val jdaService: JDAService,
    commandExecutor: CommandExecutor,
) : ListenerAdapter() {

    private val loggingCommandExecutor = commandExecutor.withLogging(logger, "discord")

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isWebhookMessage || event.author.isBot) {
            return
        }
        val messageRaw = event.message.contentRaw
        val member = event.member ?: run {
            logger.error("Discord member is null in chat listener, ignoring message $messageRaw")
            return
        }
        if (member.hasPermission(Permission.ADMINISTRATOR) && messageRaw.contains("!cmd")) {
            val command = event.message.contentRaw.replace("!cmd ", "")
            // TODO: Use coroutines properly
            runBlocking {
                loggingCommandExecutor.execute(
                    DiscordCommandSource(
                        checkNotNull(jdaService.jda) { "JDA is not initialized" },
                        event.channel.id,
                    ),
                    command,
                )
            }
            return
        } else if (messageRaw.startsWith("!list")) {
            val onlinePlayers = playerService.getAll().toList()
            val playerNames: String = if (onlinePlayers.isEmpty()) {
                "```There are currently no players online!```"
            } else {
                onlinePlayers.joinToString(
                    separator = ", ",
                    prefix = "**Online Players:**```",
                    postfix = "```",
                ) { it.username }
            }
            event.channel.sendMessage(playerNames).queue()
            return
        }
        sendMessage(event.channel.id, messageRaw, event.member!!.effectiveName)
        logger.info("[Discord] " + event.member!!.effectiveName + " : " + event.message.contentDisplay)
    }

    private fun sendMessage(channelId: String, content: String, username: String) {
        val targetChannel = registry[catalystKeys.CHAT_CHANNELS].values.firstOrNull { it.discordChannelId == channelId }
            ?: return

        // TODO: Get userId for discord user
        val finalMessage = Component.text()
            .append(
                registry[catalystKeys.CHAT_DISCORD_MESSAGE_FORMAT].replaceText {
                    it.matchLiteral("%channel.name%").replacement(targetChannel.name)
                }.replaceText {
                    it.matchLiteral("%content%").replacement(content)
                }.replaceText {
                    it.matchLiteral("%name%").replacement(username)
                },
            )
            .clickEvent(ClickEvent.openUrl(registry[catalystKeys.CHAT_DISCORD_INVITE]))
            .hoverEvent(HoverEvent.showText(registry[catalystKeys.CHAT_DISCORD_HOVER_MESSAGE]))
            .build()

        channelService.getReceivers(targetChannel.id).forEach { it.sendMessage(finalMessage) }
    }
}
