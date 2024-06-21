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

import org.anvilpowered.anvil.core.AnvilApi
import org.anvilpowered.anvil.core.config.KeyNamespace
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.anvil.core.config.configureDefaults
import org.anvilpowered.catalyst.api.chat.LuckpermsService
import org.anvilpowered.catalyst.api.chat.placeholder.BackendFormat
import org.anvilpowered.catalyst.api.chat.placeholder.ChannelMessageFormat
import org.anvilpowered.catalyst.api.chat.placeholder.ChatChannelFormat
import org.anvilpowered.catalyst.api.chat.placeholder.MessageContentFormat
import org.anvilpowered.catalyst.api.chat.placeholder.MiniMessageSerializer
import org.anvilpowered.catalyst.api.chat.placeholder.OnlineUserFormat
import org.anvilpowered.catalyst.api.chat.placeholder.PlayerFormat
import org.anvilpowered.catalyst.api.chat.placeholder.PrivateMessageFormat
import org.anvilpowered.catalyst.api.chat.placeholder.ProxyFormat
import org.anvilpowered.catalyst.api.chat.placeholder.register
import org.anvilpowered.catalyst.api.config.CatalystKeys
import org.anvilpowered.catalyst.api.user.MinecraftUserRepository
import org.anvilpowered.catalyst.api.user.UserRepository
import org.anvilpowered.catalyst.core.Registrar
import org.anvilpowered.catalyst.core.chat.bridge.discord.JDAService
import org.anvilpowered.catalyst.core.chat.bridge.discord.WebhookSender
import org.anvilpowered.catalyst.core.command.channel.ChannelAliasCommandRegistrar
import org.anvilpowered.catalyst.core.db.user.MinecraftUserRepositoryImpl
import org.anvilpowered.catalyst.core.db.user.UserRepositoryImpl
import org.anvilpowered.catalyst.proxy.registrar.CommandRegistrar
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

interface CatalystApi {
    val module: Module

    companion object
}

fun CatalystApi.Companion.create(anvilApi: AnvilApi): CatalystApi {
    val velocityModule = module {
        val serializers = TypeSerializerCollection.defaults().childBuilder()
            .register(BackendFormat.Serializer)
            .register(ChannelMessageFormat.Serializer)
            .register(ChatChannelFormat.Serializer)
            .register(MessageContentFormat.Serializer)
            .register(OnlineUserFormat.Serializer)
            .register(PlayerFormat.Serializer)
            .register(PrivateMessageFormat.Serializer)
            .register(ProxyFormat.Serializer)
            .register(MiniMessageSerializer)
            .build()

        Registry.configureDefaults(anvilApi, serializers)
        singleOf(::LuckpermsService)
        singleOf(::WebhookSender)

        singleOf(::CatalystKeys).withOptions {
            bind<KeyNamespace>()
        }

        singleOf(::JDAService)
        singleOf(::MinecraftUserRepositoryImpl) { bind<MinecraftUserRepository>() }
        singleOf(::UserRepositoryImpl) { bind<UserRepository>() }

        singleOf(BackendFormat::Resolver)
        singleOf(ChannelMessageFormat::Resolver)
        singleOf(ChatChannelFormat::Resolver)
        singleOf(MessageContentFormat::Resolver)
        singleOf(OnlineUserFormat::Resolver)
        singleOf(PlayerFormat::Resolver)
        singleOf(PrivateMessageFormat::Resolver)
        singleOf(ProxyFormat::Resolver)

        singleOf(::GlobalTab).withOptions {
            createdAtStart()
        }

        singleOf(::CommandRegistrar) { bind<Registrar>() }
        singleOf(::ListenerRegistrar) { bind<Registrar>() }
        singleOf(::ChannelAliasCommandRegistrar) { bind<Registrar>() }
    }

    return object : CatalystApi {
        override val module: Module = velocityModule
    }
}
