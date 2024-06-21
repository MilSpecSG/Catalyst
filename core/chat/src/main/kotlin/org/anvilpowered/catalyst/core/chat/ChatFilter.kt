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

import net.kyori.adventure.text.Component
import org.anvilpowered.anvil.core.config.Registry
import org.anvilpowered.catalyst.api.config.CatalystKeys
import java.util.Locale

class ChatFilter(
    private val registry: Registry,
    private val catalystKeys: CatalystKeys,
) {

    private fun stripMessage(checkMessage: String): String {
        return checkMessage.lowercase(Locale.getDefault())
            .replace("[*()/.,;'#~^+\\-]".toRegex(), " ").replace("[0@]".toRegex(), "o")
            .replace("1".toRegex(), "i").replace("\\$".toRegex(), "s")
    }

    private fun findSpacePositions(message: String, noSpaces: String): List<Int> {
        val spacePositions: MutableList<Int> = ArrayList()
        var regularIndex = 0
        for (noSpacesIndex in noSpaces.indices) {
            if (message[regularIndex] == ' ') {
                spacePositions.add(noSpacesIndex - 1)
                regularIndex++
            }
            regularIndex++
        }
        return spacePositions
    }

    private fun findSwears(message: String, spacePositions: List<Int>): List<Pair<Int, Int>> {
        val swearList: MutableList<Pair<Int, Int>> = ArrayList()
        val exceptions = registry[catalystKeys.CHAT_FILTER_EXCEPTIONS].map { it.lowercase(Locale.getDefault()) }
        for (bannedWord in registry[catalystKeys.CHAT_FILTER_SWEARS]) {
            if (message.contains(bannedWord) && !exceptions.contains(bannedWord)) {
                var startIndex = message.indexOf(bannedWord)
                while (startIndex != -1) {
                    val endIndex = startIndex + bannedWord.length - 1
                    val extraStartSpace = spacePositions.indexOf(startIndex - 1) + 1
                    var extraEndSpace = spacePositions.indexOf(endIndex)
                    if (spacePositions.containsAll(listOf(startIndex - 1, endIndex))) {
                        swearList.add(startIndex + extraStartSpace to endIndex + extraEndSpace + 1)
                    } else if ((spacePositions.contains(startIndex - 1) || startIndex == 0) &&
                        (endIndex == message.length - 1 || spacePositions.contains(endIndex))
                    ) {
                        if (endIndex == message.length - 1) {
                            extraEndSpace = spacePositions.size
                        }
                        swearList.add(startIndex + extraStartSpace to endIndex + extraEndSpace + 1)
                    }
                    startIndex = message.indexOf(bannedWord, startIndex + 1)
                }
            }
        }
        return swearList
    }

    fun replaceSwears(message: Component): Component {
        // TODO: Exceptions
//        val rawMessage = PlainTextComponentSerializer.plainText().serialize(message)
//        val strippedMessage = stripMessage(rawMessage)
//        val noSpacesMessage = strippedMessage.replace(" ".toRegex(), "")
//        val spacePositions = findSpacePositions(strippedMessage, noSpacesMessage)
//        val swearPositions = findSwears(noSpacesMessage, spacePositions)

        message.replaceText {
            it.match(registry[catalystKeys.CHAT_FILTER_SWEARS].joinToString("|"))
            it.replacement { matchResult, builder ->
                builder.append(Component.text("*".repeat(matchResult.end() - matchResult.start())))
                builder.build()
            }
        }
        return message
    }
}
