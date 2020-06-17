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

package org.anvilpowered.catalyst.common.command;

import com.google.inject.Inject;
import org.anvilpowered.anvil.api.data.registry.Registry;
import org.anvilpowered.anvil.api.util.PermissionService;
import org.anvilpowered.anvil.api.util.TextService;
import org.anvilpowered.anvil.api.util.UserService;
import org.anvilpowered.catalyst.api.data.key.CatalystKeys;
import org.anvilpowered.catalyst.api.plugin.PluginMessages;
import org.anvilpowered.catalyst.api.service.CrossServerTeleportationHelper;

import java.util.Optional;

public class CommonTeleportAcceptCommand<
    TString,
    TUser,
    TPlayer,
    TCommandSource,
    TSubject> {

    @Inject
    private TextService<TString, TCommandSource> textService;

    @Inject
    private PluginMessages<TString> pluginMessages;

    @Inject
    private PermissionService<TSubject> permissionService;

    @Inject
    private Registry registry;

    @Inject
    private CrossServerTeleportationHelper teleportationHelper;

    @Inject
    private UserService<TUser, TPlayer> userService;

    public void execute(TCommandSource source, TSubject subject) {
        if (permissionService.hasPermission(subject, registry.getOrDefault(CatalystKeys.TELEPORT_REQUEST_PERMISSION))) {
            if (teleportationHelper.getRequestingPlayerName(userService.getUserName((TUser) source)).isPresent()) {
                Optional<String> requestingPlayerName = teleportationHelper.getRequestingPlayerName(userService.getUserName((TUser) source));
                if (userService.get(requestingPlayerName.get()).isPresent()) {
                    TUser requesting = userService.get(requestingPlayerName.get()).get();
                    textService.send(
                        pluginMessages.getTeleportRequestAccepted(userService.getUserName((TUser) source)),
                        (TCommandSource) requesting);
                    textService.send(
                        pluginMessages.getSourceAcceptedTeleport(requestingPlayerName.get()),
                        source
                    );
                    teleportationHelper.teleport(requestingPlayerName.get(), userService.getUserName((TUser) source));
                } else {
                    textService.send(pluginMessages.offlineOrInvalidPlayer(), source);
                }
            } else {
                textService.send(pluginMessages.getNoPendingRequests(), source);
            }
        } else {
            textService.send(pluginMessages.getNoPermission(), source);
        }
    }
}
