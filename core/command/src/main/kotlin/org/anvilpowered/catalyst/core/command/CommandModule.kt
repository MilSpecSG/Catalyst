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

import org.anvilpowered.catalyst.core.command.broadcast.BroadcastCommandFactory
import org.anvilpowered.catalyst.core.command.channel.ChannelCommandFactory
import org.anvilpowered.catalyst.core.command.channel.ChannelSwitchFrontend
import org.anvilpowered.catalyst.core.command.message.MessageCommandFactory
import org.anvilpowered.catalyst.core.command.message.ReplyCommandFactory
import org.anvilpowered.catalyst.core.command.nickname.NicknameCommandFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val CommandModule = module {
    singleOf(::ChannelSwitchFrontend)
    singleOf(::BroadcastCommandFactory)
    singleOf(::CatalystCommandFactory)
    singleOf(::ChannelCommandFactory)
    singleOf(::MessageCommandFactory)
    singleOf(::NicknameCommandFactory)
    singleOf(::ReplyCommandFactory)
}
