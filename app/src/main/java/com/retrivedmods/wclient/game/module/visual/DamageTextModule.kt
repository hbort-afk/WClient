package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import com.retrivedmods.wclient.game.entity.Player

class DamageTextModule : Module("DamageText", ModuleCategory.Visual) {

<<<<<<< HEAD
    // Intercepting the packet to track damage events
=======

>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        if (packet is EntityEventPacket && packet.type == EntityEventType.HURT) {
            val entityId = packet.runtimeEntityId

<<<<<<< HEAD
            // Ignore self-damage (don't show message for local player)
            if (entityId == session.localPlayer.runtimeEntityId) return

            // Retrieve the player entity by runtimeEntityId from the entity map
            val entity = session.level.entityMap[entityId]

            // If the entity exists and is a player, get their username
            if (entity is Player) {
                val playerName = entity.username  // Assuming `username` exists

                val stateText = "$playerName was damaged!"
                val status = "§c$stateText"  // You can use colors like §a for green
                val message = "§l§c[WClient] §8»§r $status"


                // Send the formatted damage message to the chat
=======

            if (entityId == session.localPlayer.runtimeEntityId) return


            val entity = session.level.entityMap[entityId]


            if (entity is Player) {
                val playerName = entity.username

                val stateText = "$playerName§r §cEnemy Damaged"
                val status = "§f$stateText"
                val message = " $status"



>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c
                session.displayClientMessage(message)
            }
        }
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c
