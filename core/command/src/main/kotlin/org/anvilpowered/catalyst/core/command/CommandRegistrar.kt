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

package org.anvilpowered.catalyst.core.command

import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import org.anvilpowered.catalyst.core.Registrar
import org.anvilpowered.catalyst.proxy.command.broadcast.BroadcastCommandFactory
import org.anvilpowered.catalyst.proxy.command.channel.ChannelCommandFactory
import org.anvilpowered.catalyst.proxy.command.message.MessageCommandFactory
import org.anvilpowered.catalyst.proxy.command.message.ReplyCommandFactory
import org.anvilpowered.catalyst.proxy.command.nickname.NicknameCommandFactory
import org.anvilpowered.kbrig.brigadier.toBrigadier
import org.anvilpowered.kbrig.tree.LiteralCommandNode
import org.apache.logging.log4j.Logger

class CommandRegistrar(
    private val proxyServer: ProxyServer,
    private val logger: Logger,
    private val catalystCommandFactory: CatalystCommandFactory,
    private val channelCommandFactory: ChannelCommandFactory,
    private val broadcastCommandFactory: BroadcastCommandFactory,
    private val messageCommandFactory: MessageCommandFactory,
    private val nicknameCommandFactory: NicknameCommandFactory,
    private val replyCommandFactory: ReplyCommandFactory,
) : Registrar {
    private fun LiteralCommandNode<CommandSource>.register() = proxyServer.commandManager.register(BrigadierCommand(toBrigadier()))

    override fun register() {
        logger.info("Building command trees and registering commands...")
        broadcastCommandFactory.create().register()
        catalystCommandFactory.create().register()
        channelCommandFactory.create().register()
        messageCommandFactory.create().register()
        nicknameCommandFactory.create().register()
        replyCommandFactory.create().register()
        logger.info("Finished registering commands.")
    }
}
