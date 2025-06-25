package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.*

class AdvanceCombatAuraModule : Module("AdvanceCombatAura", ModuleCategory.Combat) {

    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("mobs_only", true)
    private var tpAuraEnabled by boolValue("tp_aura", true)

    private var rangeValue by floatValue("range", 15.0f, 2f..25f)
    private var cpsValue by intValue("cps", 40, 10..50)

    private var tpSpeed by intValue("tp_speed", 75, 10..2000)
    private var tpYLevel by intValue("yOffset", 0, -10..10)
    private var distanceToKeep by floatValue("keep_distance", 2.0f, 1f..5f)

    private var lastAttackTime = 0L
    private var tpCooldown = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            val attackDelay = 1000L / cpsValue.coerceAtLeast(1)

            if ((currentTime - lastAttackTime) >= attackDelay) {
                val targets = searchForClosestEntities().take(3)
                if (targets.isEmpty()) return

                targets.forEach { entity ->
                    rotateToward(entity)

                    if (tpAuraEnabled && (currentTime - tpCooldown) >= tpSpeed) {
                        teleportTo(entity, distanceToKeep, tpYLevel)
                        tpCooldown = currentTime
                    }

                    session.localPlayer.attack(entity)
                    lastAttackTime = currentTime
                }
            }
        }
    }

    private fun rotateToward(entity: Entity) {
        val player = session.localPlayer
        val playerPos = player.vec3Position
        val targetPos = entity.vec3Position

        val dx = targetPos.x - playerPos.x
        val dy = (targetPos.y + 1.5f) - (playerPos.y + 1.62f)
        val dz = targetPos.z - playerPos.z

        val distXZ = sqrt(dx * dx + dz * dz)
        val yaw = Math.toDegrees(atan2(-dx, dz).toDouble()).toFloat()
        val pitch = Math.toDegrees((-atan2(dy, distXZ)).toDouble()).toFloat()

        val rotPacket = MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = player.vec3Position
            rotation = Vector3f.from(pitch, yaw, 0f)
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = true
            tick = player.tickExists
        }

        session.clientBound(rotPacket)
    }

    private fun teleportTo(entity: Entity, distance: Float, yOffset: Int) {
        val player = session.localPlayer
        val target = entity.vec3Position

        val yaw = Math.toRadians(entity.vec3Rotation.y.toDouble()).toFloat()
        val direction = Vector3f.from(sin(yaw), 0f, -cos(yaw))
        val length = direction.length()
        val normalized = if (length != 0f) Vector3f.from(direction.x / length, 0f, direction.z / length) else direction

        val newPos = Vector3f.from(
            target.x + normalized.x * distance,
            target.y + yOffset,
            target.z + normalized.z * distance
        )

        val move = MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = newPos
            rotation = entity.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = false
            tick = player.tickExists
        }

        session.clientBound(move)
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> playersOnly && !this.isBot()
            is EntityUnknown -> mobsOnly && isMob()
            else -> false
        }
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val info = session.level.playerMap[this.uuid] ?: return true
        return info.name.isBlank()
    }

    private fun searchForClosestEntities(): List<Entity> {
        return session.level.entityMap.values
            .filter { it.distance(session.localPlayer) < rangeValue && it.isTarget() }
            .sortedBy { it.distance(session.localPlayer) }
    }
}
