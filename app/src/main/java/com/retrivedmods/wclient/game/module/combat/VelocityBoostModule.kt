package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class VelocityBoostModule : Module("VelocityBoost", ModuleCategory.Combat) {


    private val horizontalBoost = 1.0f
    private val applyVertical = false
    private val verticalMultiplier = 1.0f
    private val scaleWithKnockback = false


    private var lastYaw = 0.0f
    private var wasMoving = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet


        if (packet is PlayerAuthInputPacket) {
            val motion = packet.motion
            lastYaw = packet.rotation.y
            wasMoving = motion.x != 0.0f || motion.y != 0.0f
            return
        }



        if (packet is SetEntityMotionPacket && packet.runtimeEntityId == session.localPlayer.runtimeEntityId) {
            if (!wasMoving) return

            val incomingVelocity = packet.motion
            val knockbackMagnitude = sqrt(incomingVelocity.x * incomingVelocity.x + incomingVelocity.z * incomingVelocity.z) * 10.0f

            val yawRadians = Math.toRadians(lastYaw.toDouble())
            val forwardX = -sin(yawRadians).toFloat()
            val forwardZ = cos(yawRadians).toFloat()

            var boost = horizontalBoost / 10.0f
            if (scaleWithKnockback && knockbackMagnitude > 1.0f) {
                boost += knockbackMagnitude / 10.0f
            }

            var yBoost = 0.0f
            if (applyVertical) {
                yBoost = incomingVelocity.y * verticalMultiplier
            }


            packet.motion = Vector3f.from(
                forwardX * boost,
                yBoost,
                forwardZ * boost
            )
        }
    }
}
