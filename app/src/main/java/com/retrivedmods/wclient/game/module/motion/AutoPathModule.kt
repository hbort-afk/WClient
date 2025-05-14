package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.min
import kotlin.math.sqrt

class AutoPathModule : Module("PlayerTP", ModuleCategory.Motion) {

    private var maxRange by floatValue("Range", 500f, 10f..500f)
    private var grabSpeed by floatValue("Speed", 8.0f, 1f..50f)
    private var yOffset by floatValue("YOffset", 1.0f, -5f..5f)

    private var lastMoveTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val player = session.localPlayer
        val target = findNearestEnemy(player) ?: return

        val now = System.currentTimeMillis()
        if (now - lastMoveTime < 20L) return
        lastMoveTime = now

        val playerPos = player.vec3Position
        val targetPos = target.vec3Position.add(0f, yOffset, 0f)

        val dx = targetPos.x - playerPos.x
        val dy = targetPos.y - playerPos.y
        val dz = targetPos.z - playerPos.z
        val distance = sqrt(dx * dx + dy * dy + dz * dz)

        if (distance > maxRange) return

        val maxStep = grabSpeed // blocks per tick
        val ratio = min(1.0f, maxStep / distance)
        val newPosition = Vector3f.from(
            playerPos.x + dx * ratio,
            playerPos.y + dy * ratio,
            playerPos.z + dz * ratio
        )

        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = newPosition
            rotation = player.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = false
            ridingRuntimeEntityId = 0
            tick = player.tickExists
        })
    }

    private fun findNearestEnemy(player: LocalPlayer): Entity? {
        return session.level.entityMap.values
            .filter { it != player && it is Player && !isBot(it) }
            .filter { it.vec3Position.distance(player.vec3Position) <= maxRange }
            .minByOrNull { it.vec3Position.distance(player.vec3Position) }
    }

    private fun isBot(entity: Entity): Boolean {
        if (entity !is Player || entity is LocalPlayer) return false
        val data = session.level.playerMap[entity.uuid]
        return data?.name.isNullOrEmpty()
    }
}
