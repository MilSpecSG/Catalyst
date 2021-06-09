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

package org.anvilpowered.catalyst.bungee.command;

import com.google.inject.Inject;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.anvilpowered.anvil.api.registry.Registry;
import org.anvilpowered.catalyst.api.CommandSuggestionType;
import org.anvilpowered.catalyst.bungee.CatalystBungee;
import org.anvilpowered.catalyst.bungee.service.BungeeCommandDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.anvilpowered.catalyst.common.command.CommonCommandNode;

public class BungeeCommandNode
    extends CommonCommandNode<TextComponent, ProxiedPlayer, CommandSender> {

    @Inject
    private BungeeCommandDispatcher commandDispatcher;

    @Inject
    private CatalystBungee plugin;

    @Inject
    public BungeeCommandNode(Registry registry) {
        super(registry, ProxiedPlayer.class, ProxyServer.getInstance().getConsole().getClass());
    }

    @Override
    public void loadCommands() {
        List<BungeeCommand> bungeeCommands = new ArrayList<>();
        for (Map.Entry<List<String>, LiteralCommandNode<CommandSender>> entry : getCommands().entrySet()) {
            List<String> withoutFirst = new ArrayList<>(entry.getKey());
            withoutFirst.remove(0);

            commandDispatcher.register(entry.getKey().get(0), entry.getValue(), withoutFirst);
            BungeeCommand command = new BungeeCommand(
                entry.getKey().get(0),
                withoutFirst.toArray(new String[0]),
                entry.getValue(),
                super.getRegistry(),
                super.advancedServerInfo,
                super.locationService
            );
            ProxyServer.getInstance().getPluginManager().registerCommand(plugin, command);
            bungeeCommands.add(command);
        }

        for (Map.Entry<LiteralCommandNode<CommandSender>, Map<Integer, CommandSuggestionType>> entry : getSuggestionType().entrySet()) {
            Map<Integer, CommandSuggestionType> suggestionMap = entry.getValue();
            for (BungeeCommand command : bungeeCommands) {
                if (command.compareNode(entry.getKey())) {
                    command.setSuggestions(suggestionMap);
                }
            }
        }
    }
}
