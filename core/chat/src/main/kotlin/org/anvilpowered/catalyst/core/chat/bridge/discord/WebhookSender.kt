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

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.dv8tion.jda.api.Permission
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.core.config.SimpleKey
import org.anvilpowered.catalyst.api.chat.placeholder.OnlineUserFormat
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.api.user.MinecraftUser

class WebhookSender(
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
    private val jdaService: JDAService,
    private val onlineUserFormatResolver: OnlineUserFormat.Resolver,
) {

    suspend fun sendChannelMessage(user: MinecraftUser.Online, content: Component, discordChannelId: String) {
        val client = getWebhookClient(discordChannelId)
        val builder = WebhookMessageBuilder()
        builder.setAvatarUrl(registry[catalystKeys.CHAT_DISCORD_BOT_AVATAR].replace("%uuid%", user.player.id.toString()))
        builder.setUsername(
            PlainTextComponentSerializer.plainText()
                .serialize(onlineUserFormatResolver.resolve(registry[catalystKeys.CHAT_DISCORD_USERNAME_FORMAT], user)),
        )
        builder.setContent(PlainTextComponentSerializer.plainText().serialize(content))
        val message = builder.build()
        client.send(message)
    }

    suspend fun sendSpecialMessage(
        user: MinecraftUser.Online,
        discordChannelId: String,
        messageKey: SimpleKey<OnlineUserFormat>,
    ) {
        val client = getWebhookClient(discordChannelId)
        val builder = WebhookMessageBuilder()
        builder.setAvatarUrl(registry[catalystKeys.CHAT_DISCORD_BOT_AVATAR].replace("%uuid%", user.player.id.toString()))
        builder.setUsername("System")
        builder.setContent(
            PlainTextComponentSerializer.plainText()
                .serialize(onlineUserFormatResolver.resolve(registry[messageKey], user)),
        )
        val message = builder.build()
        client.send(message)
    }

    private fun getWebhookClient(discordChannelId: String): WebhookClient {
        val channel = jdaService.jda?.getTextChannelById(discordChannelId) ?: throw AssertionError("Discord channel may not be null!")
        if (!channel.guild.selfMember.hasPermission(Permission.MANAGE_WEBHOOKS)) {
            throw IllegalStateException("Please allow the discord bot to handle webhooks!")
        }
        val result = channel.guild
            .retrieveWebhooks().complete().stream().filter { it.name.equals("Catalyst " + channel.name, ignoreCase = true) }
            .findFirst().orElse(null) ?: channel.createWebhook("Catalyst " + channel.name).complete()
        return WebhookClientBuilder.fromJDA(result).build()
    }
}
