/*
 *     Copyright (C) 2020 STG_Allen
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.anvilpowered.catalyst.velocity.command;

import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.text.TextComponent;
import org.anvilpowered.anvil.api.data.registry.Registry;
import org.anvilpowered.catalyst.api.data.key.CatalystKeys;
import org.anvilpowered.catalyst.api.plugin.PluginMessages;
import org.anvilpowered.catalyst.common.command.CommonStaffChatCommand;
import org.checkerframework.checker.nullness.qual.NonNull;

public class StaffChatCommand extends CommonStaffChatCommand<
    TextComponent,
    Player,
    CommandSource,
    PermissionSubject,
    Object>
    implements Command {

    @Inject
    private PluginMessages<TextComponent> pluginMessages;

    @Inject
    private ProxyServer proxyServer;

    @Inject
    private Registry registry;

    @Override
    public void execute(CommandSource source, @NonNull String[] args) {

        if (source instanceof Player) {
            execute(source, source, args);
        } else {
            if (args.length == 0) {
                source.sendMessage(pluginMessages.getNotEnoughArgs());
            } else {
                String message = String.join(" ", args);
                proxyServer.getAllPlayers().stream().filter(target ->
                    target.hasPermission(registry.getOrDefault(CatalystKeys.STAFFCHAT_PERMISSION)))
                    .forEach(target ->
                        target.sendMessage(pluginMessages
                            .getStaffChatMessageFormattedConsole(TextComponent.of(message)))
                    );
            }
        }
    }
}
