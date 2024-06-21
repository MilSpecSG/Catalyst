package org.anvilpowered.catalyst.core.command

import com.velocitypowered.api.command.CommandSource
import org.anvilpowered.anvil.core.command.config.ConfigCommandFactory
import org.anvilpowered.anvil.velocity.command.toAnvilCommandSource
import org.anvilpowered.kbrig.builder.ArgumentBuilder
import org.anvilpowered.kbrig.tree.LiteralCommandNode
import org.anvilpowered.kbrig.tree.mapSource

class CatalystCommandFactory(private val configCommandFactory: ConfigCommandFactory) {
    fun create(): LiteralCommandNode<CommandSource> {
        return ArgumentBuilder.literal<CommandSource>("catalyst")
            .then(configCommandFactory.create().mapSource { it.toAnvilCommandSource() })
            .build()
    }
}
