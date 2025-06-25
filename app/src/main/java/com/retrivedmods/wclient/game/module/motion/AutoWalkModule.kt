package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.concurrent.timer
import kotlin.math.cos
import kotlin.math.sin

class AutoWalkModule : Module("auto_walk", ModuleCategory.Motion) {

    private val speed = 0.5f
    private var lastJumpTime = System.currentTimeMillis()
    private var isJumping = false
    private var disableYAxis = false

    init {

        timer(period = 2000) {
            if (isEnabled) {

                if (System.currentTimeMillis() - lastJumpTime >= 2000) {
                    lastJumpTime = System.currentTimeMillis()
                    applyJump()
                }
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            controlXZMovement(packet)


            if (!disableYAxis) {
                controlYMovement()
            }
        }
    }


    private fun controlXZMovement(packet: PlayerAuthInputPacket) {

        val yaw = Math.toRadians(packet.rotation.y.toDouble())
            .toFloat()
        val pitch =
            Math.toRadians(packet.rotation.x.toDouble()).toFloat()


        val motionX = -sin(yaw) * cos(pitch) * speed
        val motionZ = cos(yaw) * cos(pitch) * speed

        // Send the updated packet for movement input
        val motionPacket = SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(
                motionX.toFloat(),
                0f,  // We are not manually controlling the Y-axis yet
                motionZ.toFloat()
            )
        }
        session.clientBound(motionPacket)
    }

    // Function to control Y axis (jumping)
    private fun controlYMovement() {
        if (!isJumping) {  // Ensure we're not already jumping
            isJumping = true  // Mark as jumping

            // Apply a slight upward motion for the jump
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(0f, 1.5f, 0f)  // 0.5f is the jump height (adjust as needed)
            }
            session.clientBound(motionPacket)

            // Disable Y-axis control for a brief period and allow gravity to take over
            disableYAxis = true

            // Reset the disableYAxis flag after 1 second (time to fall back down)
            timer(period = 1000, initialDelay = 1000) {
                disableYAxis = false  // Enable Y-axis control again after the jump
            }
        }
    }

    // Apply a slight jump every 2 seconds
    private fun applyJump() {
        if (!isSessionCreated) {
            return
        }

        if (!isJumping) {  // Ensure we're not already jumping
            isJumping = true  // Mark as jumping

            // Apply a slight upward motion for the jump
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(0f, 1.5f, 0f)  // 0.5f is the jump height (adjust as needed)
            }
            session.clientBound(motionPacket)

            // Disable Y-axis control for a brief period and allow gravity to take over
            disableYAxis = true

            // Reset the disableYAxis flag after 1 second (time to fall back down)
            timer(period = 1000, initialDelay = 1000) {
                disableYAxis = false  // Enable Y-axis control again after the jump
            }
        }
    }
}