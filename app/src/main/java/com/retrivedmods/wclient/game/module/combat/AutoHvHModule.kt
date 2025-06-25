package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.*

class AutoHvHModule : Module("AutoHvH", ModuleCategory.Combat) {

    private var rangeValue by floatValue("Range", 20.0f, 5f..50f)
    private var cpsValue by intValue("CPS", 25, 10..30)
    private var teleportEnabled by boolValue("TeleportAttack", true)
    private var verticalTrack by boolValue("TrackY", true)
    private var keepDistance by floatValue("KeepDistance", 1.2f, 0f..5f)

    private var lastAttackTime = 0L
    private var lastTeleportTime = 0L
    private var tpCooldown by intValue("TP Delay", 80, 20..1000)
    private var tickCounter = 0

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val now = System.currentTimeMillis()
        val attackDelay = 1000L / cpsValue

        if ((now - lastAttackTime) >= attackDelay) {
            val target = findClosestEntity() ?: return

            if (teleportEnabled && (now - lastTeleportTime) >= tpCooldown) {
                teleportToTarget(target)
                lastTeleportTime = now
            }

            rotateDerpToTarget(target)
            session.localPlayer.attack(target)
            lastAttackTime = now
        }
    }

    private fun findClosestEntity(): Entity? {
        return session.level.entityMap.values
            .filter { it != session.localPlayer && isTarget(it) && it.distance(session.localPlayer) <= rangeValue }
            .minByOrNull { it.distance(session.localPlayer) }
    }

    private fun isTarget(entity: Entity): Boolean {
        return when (entity) {
            is Player -> !isBot(entity)
            is EntityUnknown -> entity.identifier in MobList.mobTypes
            else -> false
        }
    }

    private fun isBot(player: Player): Boolean {
        if (player is LocalPlayer) return true
        val info = session.level.playerMap[player.uuid]
        return info?.name.isNullOrBlank()
    }

    private fun teleportToTarget(target: Entity) {
        val player = session.localPlayer
        val direction = getDirectionTo(target)

        val targetPos = target.vec3Position
        val tpY = if (verticalTrack) targetPos.y else player.vec3Position.y

        val newPos = Vector3f.from(
            targetPos.x + direction.x * keepDistance,
            tpY,
            targetPos.z + direction.z * keepDistance
        )

        tickCounter++

        val teleportPacket = MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = newPos
            rotation = player.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = false
            tick = tickCounter.toLong()
        }

        session.clientBound(teleportPacket)
    }

    private fun rotateDerpToTarget(target: Entity) {
        val player = session.localPlayer
        val dx = target.vec3Position.x - player.vec3Position.x
        val dy = target.vec3Position.y - player.vec3Position.y
        val dz = target.vec3Position.z - player.vec3Position.z

        val dist = sqrt(dx * dx + dz * dz)
        val yaw = Math.toDegrees(atan2(-dx.toDouble(), dz.toDouble())).toFloat()
        val pitch = Math.toDegrees(-atan2(dy.toDouble(), dist.toDouble())).toFloat()

        tickCounter++

        val rotatePacket = MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = player.vec3Position
            rotation = Vector3f.from(yaw, pitch, 0f)
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = true
            tick = tickCounter.toLong()
        }

        session.clientBound(rotatePacket)
    }

    private fun getDirectionTo(target: Entity): Vector3f {
        val from = session.localPlayer.vec3Position
        val to = target.vec3Position
        val dx = to.x - from.x
        val dz = to.z - from.z
        val len = sqrt(dx * dx + dz * dz)
        return if (len != 0f) Vector3f.from(dx / len, 0f, dz / len) else Vector3f.ZERO
    }
}
