package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.InterceptablePacket
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import kotlin.math.cos
import kotlin.math.sin

class BlinkModule : Module("Blink", ModuleCategory.Misc) {

    private var blinkDistance by floatValue("Blink Distance", 10.0f, 1.0f..50.0f)
    private var blinkCooldown by intValue("Cooldown", 500, 100..2000) // in milliseconds
    private var lastBlinkTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastBlinkTime) >= blinkCooldown) {
            // Check if the player presses the blink key
            if (playerPressedBlinkKey()) {
                performBlink()
                lastBlinkTime = currentTime
            }
        }
    }

    private fun performBlink() {
        val player = session.localPlayer
        val playerPosition = player.vec3Position
        val playerRotation = player.vec3Rotation

        // Calculate the direction the player is facing
        val yaw = Math.toRadians(playerRotation.y.toDouble()).toFloat() // Convert yaw to radians
        val direction = Vector3f.from(sin(yaw), 0f, -cos(yaw))

        // Calculate the target position after the blink
        val targetPosition = playerPosition.add(direction.x * blinkDistance, 0f, direction.z * blinkDistance)

        // Create a MovePlayerPacket to teleport the player to the target position
        val movePlayerPacket = MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = targetPosition
            rotation = playerRotation // Keep the player's rotation the same
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = true
            tick = player.tickExists
        }

        // Send the packet to move the player
        session.clientBound(movePlayerPacket)
    }

    private fun playerPressedBlinkKey(): Boolean {
        // Implement key detection (Example: Check for the 'B' key press)
        // This could be connected to an input handler or event system that listens for keypresses.

        // Assuming a function `isKeyPressed` exists in your framework:
        return isKeyPressed("B") // This should be implemented according to your input system
    }

    private fun isKeyPressed(key: String): Boolean {
        // Implement actual keypress logic here.
        // You would need to hook into the input events and check if the "B" key is pressed.
        // This is a placeholder and should be adapted to your input system.
        return false
    }
}
