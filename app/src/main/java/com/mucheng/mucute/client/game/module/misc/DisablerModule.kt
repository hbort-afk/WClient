package com.mucheng.mucute.client.game.module.misc

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket

class DisablerModule : Module("Disabler", ModuleCategory.Misc) {

    private var lifeboatBypass by boolValue("LBSG Cheats", true)
    private var packetFlicker by boolValue("Packet Flicker", true)
    private var flickerDelayMs by intValue("Flicker Delay", 200, 50..1000)
    private var yDesyncAmount by floatValue("Desync", 0.25f, 0.05f..1.0f)

    private var lastFlickerTime = 0L
    private var toggleState = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet

        if (packet is MovePlayerPacket && lifeboatBypass && packetFlicker) {
            val now = System.currentTimeMillis()
            if (now - lastFlickerTime > flickerDelayMs) {
                lastFlickerTime = now

                // Alternate Y-level rapidly
                toggleState = !toggleState
                val original = packet.position
                val flickeredY = if (toggleState) original.y + yDesyncAmount else original.y - yDesyncAmount

                val flickerPacket = MovePlayerPacket().apply {
                    runtimeEntityId = packet.runtimeEntityId
                    position = Vector3f.from(original.x, flickeredY, original.z)
                    rotation = packet.rotation
                    mode = MovePlayerPacket.Mode.NORMAL
                    isOnGround = false
                    ridingRuntimeEntityId = 0
                    tick = packet.tick
                }

                // Send fake packet first (confuses anti-fly)
                session.clientBound(flickerPacket)

                // Modify the original to force server accept original Y too
                packet.position = Vector3f.from(original.x, original.y, original.z)
                packet.isOnGround = true
            }
        }
    }
}
