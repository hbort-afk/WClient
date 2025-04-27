package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.random.Random

class AdvanceDisablerModule : Module("AdvancedDisabler", ModuleCategory.Misc) {

    private val lifeboatBypass by boolValue("Lifeboat Bypass", true)
    private val spoofTeleportLag by boolValue("TP Lag Spoof", true)
    private val packetFlood by boolValue("Packet Flood", false)
    private val floodRate by intValue("Flood Rate", 5, 1..20)

    private var packetCounter = 0

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet

        // Lifeboat-specific spoofing
        if (lifeboatBypass && packet is MovePlayerPacket) {
            if (Random.nextInt(3) == 0) {
                packet.isOnGround = true
                packet.position = packet.position.add(0f, 0.015f, 0f) // subtle y-offset
            }
        }

        // TP Lag spoof
        if (spoofTeleportLag && packet is MovePlayerPacket) {
            if (Random.nextBoolean()) {
                try {
                    val field = packet.javaClass.getDeclaredField("teleportationCause")
                    field.isAccessible = true
                    field.set(packet, 0) // UNKNOWN cause
                } catch (e: Exception) {
                    // Ignore if not supported
                }
            }
        }

        // Packet flood logic
        if (packetFlood && packet is PlayerAuthInputPacket) {
            packetCounter++
            if (packetCounter >= floodRate) {
                packetCounter = 0
                for (i in 0 until 3) {
                    val spoof = PlayerAuthInputPacket().apply {
                        position = packet.position
                        delta = packet.delta
                        inputMode = packet.inputMode
                        tick = packet.tick + Random.nextInt(1, 5)
                    }
                    // Send spoofed packet via session.clientBound() method
                    session.clientBound(spoof)
                }
            }
        }
    }
}
