package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket
import org.cloudburstmc.protocol.bedrock.packet.SetHealthPacket

class AutoDisconnectModule : Module("AutoDisconnect", ModuleCategory.Misc) {

    private val threshold by intValue("Health Threshold", 4, 1..20)
    private var hasDisconnected = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || hasDisconnected) return

        val packet = interceptablePacket.packet
        if (packet is SetHealthPacket) {
            if (packet.health <= threshold) {
                disconnectPlayer(packet.health)
            }
        }
    }

    private fun disconnectPlayer(health: Int) {
        val message = "§cAutoDisconnected at $health HP"
        val disconnectPacket = DisconnectPacket().apply {
            kickMessage = message
        }

        // Send the disconnect packet both ways
        session.clientBound(disconnectPacket)
        session.serverBound(disconnectPacket)

        hasDisconnected = true
        isEnabled = false
        println("AutoDisconnect: Disconnected at $health HP.")
    }

    override fun onDisabled() {
        hasDisconnected = false
    }
}
