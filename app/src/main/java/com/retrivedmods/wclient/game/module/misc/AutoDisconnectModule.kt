package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket


class AutoDisconnectModule : Module("AutoDisconnect", ModuleCategory.Misc) {

    private var hasDisconnected = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || hasDisconnected) return

        // Get the player's health
        val playerHealth = session.localPlayer.health

        // Check if player's health is 4 or below
        if (playerHealth <= 4) {
            // Send disconnect packet to the server with a custom message
            val disconnectPacket = DisconnectPacket().apply {
                kickMessage = "Disconnected by §cWClient§r AutoDisconnect Module: Low Health" // Custom message
            }
            session.clientBound(disconnectPacket)

            // Mark as disconnected and disable the module
            hasDisconnected = true
            isEnabled = false  // Disable the module manually
        }
    }
}
