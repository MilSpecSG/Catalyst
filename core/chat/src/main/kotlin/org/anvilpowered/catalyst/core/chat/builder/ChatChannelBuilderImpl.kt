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

package org.anvilpowered.catalyst.core.chat.builder

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.anvilpowered.catalyst.api.chat.placeholder.ChannelMessageFormat
import org.anvilpowered.catalyst.api.chat.placeholder.MessageContentFormat
import org.anvilpowered.catalyst.api.chat.placeholder.OnlineUserFormat
import org.anvilpowered.catalyst.api.config.ChatChannel

internal class ChatChannelBuilderImpl : ChatChannel.Builder {
    private var id: String = ""
    private var name: Component? = null
    private var nameFormat: OnlineUserFormat = OnlineUserFormat.build {
        Component.text()
            .append(Component.text("[${backend.name}]").color(NamedTextColor.GOLD))
            .append(Component.space())
            .append(Component.text(prefix))
            .append(Component.space())
            .append(Component.text(displayname))
            .build()
    }
    private var contentFormat: MessageContentFormat = MessageContentFormat.build {
        Component.text(content)
    }
    private var messageFormat: ChannelMessageFormat = ChannelMessageFormat.build {
        Component.text()
            .append(Component.text(channel.name))
            .append(Component.space())
            .append(Component.text(name))
            .append(Component.text(": "))
            .append(Component.text(content))
            .build()
    }
    private var commandAliases: List<String> = emptyList()
    private var alwaysVisible: Boolean = false
    private var availableByDefault: Boolean = false
    private var discordChannelId: String = ""

    override fun id(id: String): ChatChannel.Builder {
        this.id = id
        return this
    }

    override fun name(name: Component): ChatChannel.Builder {
        this.name = name
        return this
    }

    override fun nameFormat(nameFormat: OnlineUserFormat): ChatChannel.Builder {
        this.nameFormat = nameFormat
        return this
    }

    override fun contentFormat(contentFormat: MessageContentFormat): ChatChannel.Builder {
        this.contentFormat = contentFormat
        return this
    }

    override fun messageFormat(messageFormat: ChannelMessageFormat): ChatChannel.Builder {
        this.messageFormat = messageFormat
        return this
    }

    override fun commandAliases(commandAliases: List<String>): ChatChannel.Builder {
        this.commandAliases = commandAliases
        return this
    }

    override fun alwaysVisible(alwaysVisible: Boolean): ChatChannel.Builder {
        this.alwaysVisible = alwaysVisible
        return this
    }

    override fun availableByDefault(availableByDefault: Boolean): ChatChannel.Builder {
        this.availableByDefault = availableByDefault
        return this
    }

    override fun discordChannelId(discordChannelId: String): ChatChannel.Builder {
        this.discordChannelId = discordChannelId
        return this
    }

    override fun build(): ChatChannel = ChatChannel(
        id,
        name ?: Component.text(id),
        nameFormat,
        contentFormat,
        messageFormat,
        commandAliases,
        alwaysVisible,
        availableByDefault,
        discordChannelId,
    )

    class Factory : ChatChannel.Builder.Factory {
        override fun builder(): ChatChannel.Builder = ChatChannelBuilderImpl()
    }
}
