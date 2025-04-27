package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.LocalPlayer
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.cos
import kotlin.math.sin

class OpFightBotModule : Module("OpFightBot", ModuleCategory.Motion) {

    private var tpAuraEnabled by boolValue("tp_aura", true)
    private var teleportBehind by boolValue("TP Behind", false)
    private var rangeValue by floatValue("range", 7.0f, 2f..10f)
    private var tpSpeed by intValue("tp_speed", 150, 50..2000)
    private var tpYLevel by intValue("yOffset", 0, -10..10)
    private var distanceToKeep by floatValue("keep_distance", 2.0f, 1f..5f)
    private var tpCooldown = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !tpAuraEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            // Search for closest entities within range
            val closestEntities = searchForClosestEntities()
            if (closestEntities.isEmpty()) return

            closestEntities.forEach { entity ->
                // Teleport to the closest entity if the cooldown is passed
                if ((currentTime - tpCooldown) >= tpSpeed) {
                    teleportTo(entity, distanceToKeep, tpYLevel)
                    tpCooldown = currentTime
                }
            }
        }
    }

    private fun teleportTo(entity: Entity, distance: Float, yOffset: Int) {
        val targetPosition = entity.vec3Position
        val playerPosition = session.localPlayer.vec3Position

        // Calculate direction using sin and cos without converting to radians manually
        val targetYaw = entity.vec3Rotation.y.toFloat()  // Angle in degrees
        val direction = Vector3f.from(
            sin(Math.toRadians(targetYaw.toDouble())).toFloat(),  // Convert to radians and then calculate sin
            0f,
            -cos(Math.toRadians(targetYaw.toDouble())).toFloat()  // Convert to radians and then calculate cos
        )
        val length = direction.length()
        val normalizedDirection = if (length != 0f) Vector3f.from(direction.x / length, 0f, direction.z / length) else direction

        // Handle teleportation direction based on whether we want to teleport behind or not
        val newPosition = if (teleportBehind) {
            Vector3f.from(
                targetPosition.x + normalizedDirection.x * distance,
                targetPosition.y + yOffset,
                targetPosition.z + normalizedDirection.z * distance
            )
        } else {
            Vector3f.from(
                targetPosition.x - normalizedDirection.x * distance,
                targetPosition.y + yOffset,
                targetPosition.z - normalizedDirection.z * distance
            )
        }

        // Send the movement packet to teleport the player
        val movePlayerPacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPosition
            rotation = entity.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = false
            ridingRuntimeEntityId = 0
            tick = session.localPlayer.tickExists
        }

        session.clientBound(movePlayerPacket)
    }

    private fun searchForClosestEntities(): List<Entity> {
        return session.level.entityMap.values
            .filter { it.distance(session.localPlayer) < rangeValue && it.isTarget() }
            .sortedBy { it.distance(session.localPlayer) } // Sort by closest
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            else -> true // Any entity can be targeted now
        }
    }
}
