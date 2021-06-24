/*
 *   Catalyst - AnvilPowered
 *   Copyright (C) 2021
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package org.anvilpowered.catalyst.bungee.module;

import com.google.inject.TypeLiteral;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.anvilpowered.anvil.api.command.CommandNode;
import org.anvilpowered.anvil.api.registry.ConfigurationService;
import org.anvilpowered.catalyst.api.service.BroadcastService;
import org.anvilpowered.catalyst.api.discord.DiscordCommandService;
import org.anvilpowered.catalyst.bungee.command.BungeeCommandNode;
import org.anvilpowered.catalyst.bungee.service.BungeeBroadcastService;
import org.anvilpowered.catalyst.bungee.service.BungeeDiscordCommandService;
import org.anvilpowered.catalyst.common.module.CommonModule;
import org.anvilpowered.catalyst.common.plugin.CatalystPluginInfo;

import java.io.File;
import java.nio.file.Paths;
import org.anvilpowered.catalyst.common.registry.CommonConfigurationService;

@SuppressWarnings({"unchecked", "UnstableApiUsage"})
public class BungeeModule extends CommonModule<
    ProxiedPlayer,
    TextComponent,
    CommandSender> {

    @Override
    protected void configure() {
        super.configure();

        bind(new TypeLiteral<CommandNode<CommandSender>>() {
        }).to(BungeeCommandNode.class);
        File configFilesLocation = Paths.get("plugins/" + CatalystPluginInfo.id).toFile();
        if (!configFilesLocation.exists()) {
            if (!configFilesLocation.mkdirs()) {
                throw new IllegalStateException("Unable to create config directory");
            }
        }

        bind(new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {
        }).toInstance(HoconConfigurationLoader.builder()
            .setPath(Paths.get(configFilesLocation + "/catalyst.conf"))
            .build());

        bind(new TypeLiteral<BroadcastService<TextComponent>>() {
        }).to(BungeeBroadcastService.class);
        bind(DiscordCommandService.class).to(BungeeDiscordCommandService.class);
    }
}
