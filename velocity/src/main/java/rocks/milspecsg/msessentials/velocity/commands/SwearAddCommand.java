/*
 *     MSEssentials - MilSpecSG
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

package rocks.milspecsg.msessentials.velocity.commands;

import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import rocks.milspecsg.msessentials.api.data.key.MSEssentialsKeys;
import rocks.milspecsg.msessentials.api.plugin.PluginMessages;
import rocks.milspecsg.msessentials.velocity.messages.CommandUsageMessages;
import rocks.milspecsg.msessentials.velocity.plugin.MSEssentials;
import rocks.milspecsg.msessentials.velocity.utils.PluginPermissions;
import rocks.milspecsg.msrepository.api.data.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class SwearAddCommand implements Command {

    @Inject
    private Registry registry;

    @Inject
    private PluginMessages<TextComponent> pluginMessages;

    @Inject
    private CommandUsageMessages commandUsage;

    @Override
    public void execute(CommandSource source, @NonNull String[] args) {
        if (!(source.hasPermission(PluginPermissions.LANGUAGE_ADMIN) || source.hasPermission(PluginPermissions.LANGUAGE_SWEAR_ADD))) {
            source.sendMessage(pluginMessages.getNoPermission());
            return;
        }

        if (args.length < 1) {
            source.sendMessage(pluginMessages.getNotEnoughArgs());
            source.sendMessage(commandUsage.swearAddCommandUsage);
            return;
        }

        List<String> swearList = new ArrayList<>(registry.getOrDefault(MSEssentialsKeys.CHAT_FILTER_SWEARS));

        if (swearList.contains(args[0])) {
            source.sendMessage(pluginMessages.getExistingSwear(args[0]));
        } else {
            registry.getOrDefault(MSEssentialsKeys.CHAT_FILTER_SWEARS).add(args[0]);
            registry.load(MSEssentials.plugin);
            source.sendMessage(pluginMessages.getNewSwear(args[0]));
        }
    }
}