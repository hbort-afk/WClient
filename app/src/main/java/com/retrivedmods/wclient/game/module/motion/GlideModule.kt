package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class GlideModule : Module("Glide", ModuleCategory.Motion) {

    private val glideSpeed by floatValue("Glide Speed", 0.2f, 0.05f..1.0f)
    private val horizontalControl by boolValue("Allow Movement", true)
    private val antiKickJitter by boolValue("AntiKick Jitter", true)

    private var jitterToggle = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val downwardMotion = -glideSpeed + if (antiKickJitter) {
                if (jitterToggle) 0.015f else -0.015f
            } else 0f
            jitterToggle = !jitterToggle

            val motionX = if (horizontalControl) packet.motion.x else 0f
            val motionZ = if (horizontalControl) packet.motion.y else 0f

            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(motionX, downwardMotion, motionZ)
            }

            session.clientBound(motionPacket)
        }
    }
}
