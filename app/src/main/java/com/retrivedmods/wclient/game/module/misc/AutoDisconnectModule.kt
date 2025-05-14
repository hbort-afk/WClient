package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.LocalPlayer
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket
import org.cloudburstmc.protocol.bedrock.packet.SetHealthPacket

class AutoDisconnectModule : Module("AutoDisconnect", ModuleCategory.Misc) {

    private var hasDisconnected = false  // Flag to prevent multiple disconnections

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || hasDisconnected) return

        val packet = interceptablePacket.packet

        // If the packet is a SetHealthPacket, check the player's health
        if (packet is SetHealthPacket) {
            // If the player's health is 1 or lower, disconnect them
            if (packet.health <= 1) {
                disconnectPlayer()
            }
        }
    }

    // Function to disconnect the player
    private fun disconnectPlayer() {
        // Send the disconnect packet to the server and client
        val disconnectPacket = DisconnectPacket().apply {
            kickMessage = "Disconnected by §cWClient§r AutoDisconnect Module due to low health"
        }

        // Send the disconnect packet to the server and client
        session.clientBound(disconnectPacket)  // Disconnect client-side
        session.serverBound(disconnectPacket)  // Disconnect server-side

        // Mark the module as disabled after disconnecting
        hasDisconnected = true
        isEnabled = false  // Disable the module after disconnect

        // Optionally, log the disconnection
        println("Player health is low, disconnecting.")
    }
}
