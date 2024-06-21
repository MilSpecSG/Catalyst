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

package org.anvilpowered.catalyst.core.chat

import org.anvilpowered.catalyst.api.chat.ChannelMessage
import org.anvilpowered.catalyst.api.chat.ChannelService
import org.anvilpowered.catalyst.api.config.ChatChannel
import org.anvilpowered.catalyst.core.chat.builder.ChannelMessageBuilderImpl
import org.anvilpowered.catalyst.core.chat.builder.ChatChannelBuilderImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val ChatModule = module {
    singleOf(::ChatFilter)
    singleOf(ChannelMessageBuilderImpl::Factory) { bind<ChannelMessage.Builder.Factory>() }
    singleOf(ChatChannelBuilderImpl::Factory) { bind<ChatChannel.Builder.Factory>() }
    singleOf(::ChannelServiceImpl) { bind<ChannelService>() }
    singleOf(::ChatServiceImpl) { bind<ChatService>() }
    singleOf(::PrivateMessageService)
    singleOf(::StaffListService)
}
