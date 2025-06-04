package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
<<<<<<< HEAD
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.EntityUnknown
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.entity.MobList
import com.retrivedmods.wclient.game.entity.Player
=======
import com.retrivedmods.wclient.game.entity.*
>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.cos
import kotlin.math.sin

class KillauraModule : Module("killaura", ModuleCategory.Combat) {

    private var playersOnly by boolValue("players_only", true)
<<<<<<< HEAD
    private var mobsOnly by boolValue("mobs_only", true)
    private var tpAuraEnabled by boolValue("tp_aura", true)
    private var strafe by boolValue("strafe", true)
    private var teleportBehind by boolValue("tp_behind", false)
    private var criticalHits by boolValue("Critical Hit", true)

    private var rangeValue by floatValue("range", 7.0f, 2f..10f)
    private var attackInterval by intValue("delay", 3, 1..10)
    private var cpsValue by intValue("cps", 12, 5..20)
    private var tpSpeed by intValue("tp_speed", 150, 50..2000)
    private var tpYLevel by intValue("yOffset", 0, -10..10)

    private var distanceToKeep by floatValue("keep_distance", 2.0f, 1f..5f)
    private var strafeAngle = 0.0f
    private val strafeSpeed by floatValue("strafe_speed", 2.0f, 1f..3f)
    private val strafeRadius by floatValue("strafe_radius", 3.0f, 2f..6f)
=======
    private var mobsOnly by boolValue("mobs_only", false)
    private var tpAuraEnabled by boolValue("tp_aura", true)
    private var teleportBehind by boolValue("tp_behind", true)
    private var criticalHits by boolValue("critical_hit", true)
    private var strafe by boolValue("strafe", false)

    private var rangeValue by floatValue("range", 9.5f, 2f..16f)
    private var cpsValue by intValue("cps", 20, 10..25)
    private var tpSpeed by intValue("tp_speed", 100, 10..500)
    private var tpYOffset by intValue("tp_y_offset", 1, -10..10)
    private var distanceToKeep by floatValue("keep_distance", 1.0f, 0.5f..5f)

    private var strafeAngle = 0.0f
    private val strafeSpeed by floatValue("strafe_speed", 2.5f, 1f..4f)
    private val strafeRadius by floatValue("strafe_radius", 2.5f, 1f..6f)

>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c
    private var lastAttackTime = 0L
    private var tpCooldown = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
<<<<<<< HEAD
            val currentTime = System.currentTimeMillis()
            val minAttackDelay = 1000L / cpsValue

            if (packet.tick % attackInterval == 0L && (currentTime - lastAttackTime) >= minAttackDelay) {
                val closestEntities = searchForClosestEntities()
                if (closestEntities.isEmpty()) return

                closestEntities.forEach { entity ->
                    if (tpAuraEnabled && (currentTime - tpCooldown) >= tpSpeed) {
                        teleportTo(entity, distanceToKeep, tpYLevel)
                        tpCooldown = currentTime
                    }

                    repeat(attackInterval) {
                        if (criticalHits) {
                            triggerCriticalHit()
                        }
                        session.localPlayer.attack(entity)
                    }

                    if (strafe) {
                        strafeAroundTarget(entity)
                    }

                    lastAttackTime = currentTime
=======
            val now = System.currentTimeMillis()
            val delay = 1000L / cpsValue

            if ((now - lastAttackTime) >= delay) {
                val targets = searchForTargets()
                if (targets.isEmpty()) return

                for (target in targets) {
                    if (tpAuraEnabled && now - tpCooldown >= tpSpeed) {
                        teleportTo(target, distanceToKeep, tpYOffset)
                        tpCooldown = now
                    }

                    if (criticalHits) triggerCriticalHit()

                    session.localPlayer.attack(target)

                    if (strafe) strafeAroundTarget(target)

                    lastAttackTime = now
>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c
                }
            }
        }
    }

<<<<<<< HEAD
    private fun triggerCriticalHit() {
        val player = session.localPlayer
        val pos = player.vec3Position

        // Fake a small Y jump to trigger crit animation/damage (Minecraft behavior)
        val offsetY = 0.06f
        val downY = -0.01f

        val jump = MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = Vector3f.from(pos.x, pos.y + offsetY, pos.z)
            rotation = player.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = false
            tick = player.tickExists
        }

        val land = MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = Vector3f.from(pos.x, pos.y + downY, pos.z)
            rotation = player.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = true
            tick = player.tickExists
        }

        session.clientBound(jump)
        session.clientBound(land)
    }

    private fun strafeAroundTarget(entity: Entity) {
        val targetPos = entity.vec3Position
        strafeAngle += strafeSpeed
        if (strafeAngle >= 360.0f) {
            strafeAngle -= 360.0f
        }
=======
    private fun teleportTo(entity: Entity, keepDistance: Float, yOffset: Int) {
        val pos = entity.vec3Position
        val player = session.localPlayer.vec3Position

        val targetYawRad = Math.toRadians(entity.vec3Rotation.y.toDouble()).toFloat()
        val behindDir = Vector3f.from(
            sin(targetYawRad),
            0f,
            -cos(targetYawRad)
        ).normalize()

        val tpPos = if (teleportBehind) {
            Vector3f.from(
                pos.x + behindDir.x * keepDistance,
                pos.y + yOffset,
                pos.z + behindDir.z * keepDistance
            )
        } else {
            val direct = Vector3f.from(pos.x - player.x, 0f, pos.z - player.z).normalize()
            Vector3f.from(
                pos.x - direct.x * keepDistance,
                pos.y + yOffset,
                pos.z - direct.z * keepDistance
            )
        }

        val movePacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = tpPos
            rotation = entity.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = false
            tick = session.localPlayer.tickExists
        }

        session.clientBound(movePacket)
    }

    private fun strafeAroundTarget(entity: Entity) {
        val pos = entity.vec3Position
        strafeAngle += strafeSpeed
        if (strafeAngle >= 360f) strafeAngle -= 360f
>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c

        val offsetX = strafeRadius * cos(strafeAngle)
        val offsetZ = strafeRadius * sin(strafeAngle)

<<<<<<< HEAD
        val newPosition = targetPos.add(offsetX.toFloat(), 0f, offsetZ.toFloat())

        val movePlayerPacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPosition
            rotation = Vector3f.from(0f, 0f, 0f)
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = true
            ridingRuntimeEntityId = 0
            tick = session.localPlayer.tickExists
        }

        session.clientBound(movePlayerPacket)
    }

    private fun teleportTo(entity: Entity, distance: Float, yOffset: Int) {
        val targetPosition = entity.vec3Position
        val playerPosition = session.localPlayer.vec3Position

        val newPosition = if (teleportBehind) {
            val targetYaw = Math.toRadians(entity.vec3Rotation.y.toDouble()).toFloat()
            val direction = Vector3f.from(sin(targetYaw), 0f, -cos(targetYaw))
            val length = direction.length()
            val normalizedDirection = if (length != 0f) Vector3f.from(direction.x / length, 0f, direction.z / length) else direction

            Vector3f.from(
                targetPosition.x + normalizedDirection.x * distance,
                targetPosition.y + yOffset,
                targetPosition.z + normalizedDirection.z * distance
            )
        } else {
            val direction = Vector3f.from(
                targetPosition.x - playerPosition.x,
                0f,
                targetPosition.z - playerPosition.z
            )
            val length = direction.length()
            val normalizedDirection = if (length != 0f)
                Vector3f.from(direction.x / length, 0f, direction.z / length)
            else direction

            Vector3f.from(
                targetPosition.x - normalizedDirection.x * distance,
                targetPosition.y + yOffset,
                targetPosition.z - normalizedDirection.z * distance
            )
        }

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
=======
        val newPos = pos.add(offsetX.toFloat(), 0f, offsetZ.toFloat())

        val strafePacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPos
            rotation = Vector3f.ZERO
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = true
            tick = session.localPlayer.tickExists
        }

        session.clientBound(strafePacket)
    }

    private fun triggerCriticalHit() {
        val currentY = session.localPlayer.vec3Position.y
        val upPacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = session.localPlayer.vec3Position.add(0f, 0.1f, 0f)
            rotation = session.localPlayer.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = false
            tick = session.localPlayer.tickExists
        }
        session.clientBound(upPacket)
    }

    private fun searchForTargets(): List<Entity> {
        return session.level.entityMap.values.filter {
            it.distance(session.localPlayer) < rangeValue && it.isTarget()
        }
>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
<<<<<<< HEAD
            is Player -> (playersOnly || (playersOnly && mobsOnly)) && !this.isBot()
            is EntityUnknown -> (mobsOnly || (playersOnly && mobsOnly)) && isMob()
=======
            is Player -> playersOnly && !isBot()
            is EntityUnknown -> mobsOnly && isMob()
>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c
            else -> false
        }
    }

<<<<<<< HEAD
    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return true
        return playerList.name.isBlank()
    }

    private fun searchForClosestEntities(): List<Entity> {
        return session.level.entityMap.values
            .filter { it.distance(session.localPlayer) < rangeValue && it.isTarget() }
            .sortedBy { it.distance(session.localPlayer) } // Sort by closest
    }
}
=======
    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        return session.level.playerMap[this.uuid]?.name.isNullOrBlank()
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }
}
>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c
