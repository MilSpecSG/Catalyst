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

package org.anvilpowered.catalyst.common.module;

import com.google.common.reflect.TypeToken;
import com.google.inject.AbstractModule;
import org.anvilpowered.anvil.api.Anvil;
import org.anvilpowered.anvil.api.misc.BindingExtensions;
import org.anvilpowered.anvil.api.plugin.BasicPluginInfo;
import org.anvilpowered.anvil.api.plugin.PluginInfo;
import org.anvilpowered.anvil.api.registry.ConfigurationService;
import org.anvilpowered.anvil.api.registry.Registry;
import org.anvilpowered.catalyst.api.discord.JDAService;
import org.anvilpowered.catalyst.api.discord.WebhookSender;
import org.anvilpowered.catalyst.api.member.MemberManager;
import org.anvilpowered.catalyst.api.plugin.PluginMessages;
import org.anvilpowered.catalyst.api.service.ChatFilter;
import org.anvilpowered.catalyst.api.service.ChatService;
import org.anvilpowered.catalyst.api.service.EmojiService;
import org.anvilpowered.catalyst.api.service.EventRegistrationService;
import org.anvilpowered.catalyst.api.service.LuckpermsService;
import org.anvilpowered.catalyst.api.service.PrivateMessageService;
import org.anvilpowered.catalyst.api.service.StaffListService;
import org.anvilpowered.catalyst.api.service.TabService;
import org.anvilpowered.catalyst.common.discord.CommonJDAService;
import org.anvilpowered.catalyst.common.discord.CommonWebhookSender;
import org.anvilpowered.catalyst.common.member.CommonMemberManager;
import org.anvilpowered.catalyst.common.plugin.CatalystPluginInfo;
import org.anvilpowered.catalyst.common.plugin.CatalystPluginMessages;
import org.anvilpowered.catalyst.common.plugin.CommonStaffListService;
import org.anvilpowered.catalyst.common.registry.CommonConfigurationService;
import org.anvilpowered.catalyst.common.service.CommonChatFilter;
import org.anvilpowered.catalyst.common.service.CommonChatService;
import org.anvilpowered.catalyst.common.service.CommonEmojiService;
import org.anvilpowered.catalyst.common.service.CommonEventRegistrationService;
import org.anvilpowered.catalyst.common.service.CommonLuckpermsService;
import org.anvilpowered.catalyst.common.service.CommonPrivateMessageService;
import org.anvilpowered.catalyst.common.service.CommonTabService;

@SuppressWarnings({"UnstableApiUsage"})
public class CommonModule<
    TPlayer,
    TString,
    TCommandSource>
    extends AbstractModule {

    @Override
    protected void configure() {

        BindingExtensions be = Anvil.getBindingExtensions(binder());

        be.bind(
            new TypeToken<PluginInfo<TString>>(getClass()) {
            },
            new TypeToken<CatalystPluginInfo<TString, TCommandSource>>(getClass()) {
            });
        be.bind(
            new TypeToken<BasicPluginInfo>(getClass()) {
            },
            new TypeToken<CatalystPluginInfo<TString, TCommandSource>>(getClass()) {
            });
        be.bind(
            new TypeToken<PluginMessages<TString>>(getClass()) {
            },
            new TypeToken<CatalystPluginMessages<TString, TCommandSource>>(getClass()) {
            }
        );
        be.bind(
            new TypeToken<MemberManager<TString>>(getClass()) {
            },
            new TypeToken<CommonMemberManager<TPlayer, TString, TCommandSource>>(getClass()) {
            }
        );
        be.bind(
            new TypeToken<ChatService<TString, TPlayer, TCommandSource>>(getClass()) {
            },
            new TypeToken<CommonChatService<TPlayer, TString, TCommandSource>>(getClass()) {
            }
        );

        be.bind(
            new TypeToken<PrivateMessageService<TString>>(getClass()) {
            },
            new TypeToken<CommonPrivateMessageService<TPlayer, TString, TCommandSource>>(getClass()) {
            }
        );

        be.bind(
            new TypeToken<StaffListService<TString>>(getClass()) {
            },
            new TypeToken<CommonStaffListService<TString, TPlayer, TCommandSource>>(getClass()) {
            }
        );
        be.bind(
            new TypeToken<TabService<TString, TPlayer>>(getClass()) {
            },
            new TypeToken<CommonTabService<TString, TPlayer, TCommandSource>>(getClass()) {
            }
        );
        be.bind(
            new TypeToken<LuckpermsService>(getClass()) {
            },
            new TypeToken<CommonLuckpermsService<TPlayer>>(getClass()) {
            }
        );
        be.bind(
            new TypeToken<JDAService>(getClass()) {
            },
            new TypeToken<CommonJDAService<TPlayer, TString, TCommandSource>>(getClass()) {
            }
        );
        be.bind(
            new TypeToken<WebhookSender>(getClass()) {
            },
            new TypeToken<CommonWebhookSender<TPlayer>>(getClass()) {
            }
        );
        be.bind(
            new TypeToken<EventRegistrationService>(getClass()) {
            },
            new TypeToken<CommonEventRegistrationService<TString, TPlayer, TCommandSource>>(getClass()) {
            }
        );

        bind(ChatFilter.class).to(CommonChatFilter.class);
        bind(ConfigurationService.class).to(CommonConfigurationService.class);
        bind(Registry.class).to(CommonConfigurationService.class);
        bind(EmojiService.class).to(CommonEmojiService.class);
    }
}
