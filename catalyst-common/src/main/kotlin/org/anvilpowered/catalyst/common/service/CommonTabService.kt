/*
 * Catalyst - AnvilPowered
 *   Copyright (C) 2020
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

package org.anvilpowered.catalyst.common.service

import com.google.inject.Inject
import com.google.inject.Singleton
import org.anvilpowered.anvil.api.misc.Named
import org.anvilpowered.anvil.api.registry.Registry
import org.anvilpowered.anvil.api.server.LocationService
import org.anvilpowered.anvil.api.util.TextService
import org.anvilpowered.anvil.api.util.UserService
import org.anvilpowered.catalyst.api.registry.CatalystKeys.ADVANCED_SERVER_INFO_ENABLED
import org.anvilpowered.catalyst.api.registry.CatalystKeys.TAB_FOOTER
import org.anvilpowered.catalyst.api.registry.CatalystKeys.TAB_FORMAT
import org.anvilpowered.catalyst.api.registry.CatalystKeys.TAB_HEADER
import org.anvilpowered.catalyst.api.service.AdvancedServerInfoService
import org.anvilpowered.catalyst.api.service.LuckpermsService
import org.anvilpowered.catalyst.api.service.TabService
import java.util.HashMap

@Singleton
class CommonTabService<TString, TPlayer, TCommandSource> @Inject constructor(
  private val registry: Registry,
  private val textService: TextService<TString, TCommandSource>,
  private val locationService: LocationService,
  private val advancedServerInfoService: AdvancedServerInfoService,
  private val userService: UserService<TPlayer, TPlayer>,
  private val luckpermsService: LuckpermsService
) : TabService<TString, TPlayer> {

  var playerBalances: MutableMap<String, Double> = HashMap()

  override fun format(player: TPlayer, ping: Int, playerCount: Int): TString {
    return textService.deserialize(replacePlaceholders(registry.getOrDefault(TAB_FORMAT), player, ping, playerCount))
  }

  override fun formatCustom(format: String, player: TPlayer, ping: Int, playerCount: Int): TString {
    return textService.deserialize(replacePlaceholders(format, player, ping, playerCount))
  }

  override fun formatHeader(player: TPlayer, ping: Int, playerCount: Int): TString {
    return textService.deserialize(replacePlaceholders(registry.getOrDefault(TAB_HEADER), player, ping, playerCount))
  }

  override fun formatFooter(player: TPlayer, ping: Int, playerCount: Int): TString {
    return textService.deserialize(replacePlaceholders(registry.getOrDefault(TAB_FOOTER), player, ping, playerCount))
  }

  override fun getBalance(userName: String): String {
    return playerBalances[userName].toString()
  }

  override fun setBalance(userName: String, balance: Double) {
    if (containsKey(userName)) {
      playerBalances.replace(userName, balance)
    } else {
      playerBalances[userName] = balance
    }
  }

  private fun containsKey(userName: String): Boolean {
    return playerBalances.containsKey(userName)
  }

  private fun replacePlaceholders(format: String, player: TPlayer, ping: Int, playerCount: Int): String {
    val userName = userService.getUserName(player)
    return format.replace("%player%", userName)
      .replace("%prefix%", luckpermsService.getPrefix(player))
      .replace("%suffix%", luckpermsService.getSuffix(player))
      .replace("%server%",
        if (registry.getOrDefault(ADVANCED_SERVER_INFO_ENABLED)) locationService.getServer(userName).map { obj: Named -> obj.name }
          .orElse("null")
          .replace(advancedServerInfoService.getPrefixForPlayer(userName), "") else locationService.getServer(userName)
          .map { obj: Named -> obj.name }
          .orElse("null"))
      .replace("%ping%", ping.toString())
      .replace("%playercount%", playerCount.toString())
      .replace("%balance%", getBalance(userName))
  }
}
