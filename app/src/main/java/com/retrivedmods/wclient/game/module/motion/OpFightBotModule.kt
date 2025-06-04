package com.retrivedmods.wclient.game.module.motion

<<<<<<< HEAD
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
=======
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.*
import kotlin.random.Random

class OpFightBotModule : Module("OpFightBot", ModuleCategory.Motion) {

    private var playersOnly by boolValue("Players Only", false)
    private var filterInvisible by boolValue("Filter Invisible", true)

    private var mode by intValue("Mode", 1, 0..2)
    private var range by floatValue("Range", 2.5f, 1.5f..5.0f)
    private var passive by boolValue("Passive", false)

    private var hSpeed by floatValue("horizontalSpeed", 5.0f, 1.0f..7.0f)
    private var vSpeed by floatValue("verticalSpeed", 4.0f, 1.0f..7.0f)
    private var strafeSpeed by intValue("Strafe Speed", 20, 10..90)

    private var attack by boolValue("Attack", true)
    private var cps by intValue("CPS", 5, 1..20)
    private var packets by intValue("Packets", 1, 1..10)

    private var lastAttackTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        if (interceptablePacket.packet !is PlayerAuthInputPacket) return

        val currentTime = System.currentTimeMillis()
        val player = session.localPlayer
        val playerPos = player.vec3Position

        val target = session.level.entityMap.values
            .filter { it != player }
            .filter { !playersOnly || it is Player }
            .filter { !isEntityInvisible(it) }
            .minByOrNull { it.vec3Position.distanceSquared(playerPos) }
            ?: return

        val distance = playerPos.distance(target.vec3Position)
        val targetPos = target.vec3Position

        if (distance < range) {
            val angle = when (mode) {
                0 -> Random.nextDouble() * 360.0
                1 -> (player.tickExists * strafeSpeed) % 360.0
                2 -> target.vec3Rotation.y + 180.0
                else -> 0.0
            }

            val rad = Math.toRadians(angle)
            val newPos = Vector3f.from(
                targetPos.x - sin(rad) * range,
                (targetPos.y + 0.5f).toDouble(),
                targetPos.z + cos(rad) * range
            )

            val yaw = atan2(targetPos.z - playerPos.z, targetPos.x - playerPos.x).toFloat() + Math.toRadians(90.0).toFloat()
            val pitch = -atan2(targetPos.y - playerPos.y, playerPos.horizontalDistance(targetPos)).toFloat()

            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = player.runtimeEntityId
                position = newPos
                rotation = Vector3f.from(pitch, yaw, yaw)
                mode = MovePlayerPacket.Mode.NORMAL
                isOnGround = true
                tick = player.tickExists
            })

            if (attack && (currentTime - lastAttackTime) >= (1000L / cps)) {
                repeat(packets) {
                    player.attack(target)
                }
                lastAttackTime = currentTime
            }
        } else if (!passive) {
            val dir = atan2(targetPos.z - playerPos.z, targetPos.x - playerPos.x) - Math.toRadians(90.0).toFloat()
            val newPos = Vector3f.from(
                playerPos.x - sin(dir) * hSpeed,
                targetPos.y.coerceIn(playerPos.y - vSpeed, playerPos.y + vSpeed),
                playerPos.z + cos(dir) * hSpeed
            )

            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = player.runtimeEntityId
                position = newPos
                rotation = player.vec3Rotation
                mode = MovePlayerPacket.Mode.NORMAL
                isOnGround = true
                tick = player.tickExists
            })
        }
    }

    private fun isEntityInvisible(entity: Entity): Boolean {
        if (!filterInvisible) return false

        if (entity.vec3Position.y < -30) return true

        val flags = entity.metadata[EntityDataTypes.FLAGS] as? Long
        if (flags != null && (flags and (1L shl 5)) != 0L) return true

        val name = entity.metadata[EntityDataTypes.NAME] as? String ?: ""
        return name.contains("invisible", ignoreCase = true) || name.isEmpty()
    }

    private fun Vector3f.distanceSquared(other: Vector3f): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return dx * dx + dy * dy + dz * dz
    }

    private fun Vector3f.horizontalDistance(other: Vector3f): Float {
        val dx = x - other.x
        val dz = z - other.z
        return sqrt(dx * dx + dz * dz)
>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c
    }
}
