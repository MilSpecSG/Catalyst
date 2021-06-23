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

package org.anvilpowered.catalyst.api;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.anvilpowered.anvil.api.Environment;
import org.anvilpowered.catalyst.api.registry.AdvancedServerInfo;
import org.anvilpowered.catalyst.api.service.EventRegistrationService;
import org.anvilpowered.catalyst.api.discord.JDAService;
import org.anvilpowered.catalyst.api.service.LuckpermsService;
import org.anvilpowered.catalyst.common.plugin.CatalystPluginInfo;

public class CatalystImpl extends Catalyst {

    protected CatalystImpl(Injector rootInjector, Module module) {
        super(CatalystPluginInfo.id, rootInjector, module);
    }

    @Override
    protected void whenLoaded(Environment environment) {
        super.whenLoaded(environment);
    }

    @Override
    protected void whenReady(Environment environment) {
        super.whenReady(environment);
    }

    @Override
    protected void applyToBuilder(Environment.Builder builder) {
        super.applyToBuilder(builder);
        builder.withRootCommand();
        builder.addEarlyServices(
            LuckpermsService.class,
            AdvancedServerInfo.class,
            JDAService.class,
            EventRegistrationService.class
        );
    }
}
