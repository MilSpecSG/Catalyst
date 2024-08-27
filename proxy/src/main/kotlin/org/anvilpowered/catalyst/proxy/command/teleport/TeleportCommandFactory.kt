package org.anvilpowered.catalyst.proxy.command.teleport

import com.velocitypowered.api.command.CommandSource
import org.anvilpowered.kbrig.builder.ArgumentBuilder
import org.anvilpowered.kbrig.tree.LiteralCommandNode

class TeleportCommandFactory {

    fun create(): LiteralCommandNode<CommandSource> {
        return ArgumentBuilder.literal<CommandSource>("teleport")
            .then(

            )
            .build()
    }
}
