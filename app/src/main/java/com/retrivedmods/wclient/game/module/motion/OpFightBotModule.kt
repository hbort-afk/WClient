package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Entity
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.*

class OpFightBotModule : Module("OpFightBot", ModuleCategory.Motion) {

    private var radius by floatValue("Radius", 5.0f, 1f..15f)
    private var circleSpeed by floatValue("Circle Speed", 6.0f, 1f..15f)
    private var jitterPower by floatValue("Jitter", 0.25f, 0f..1f)
    private var maxTargetRange by floatValue("Target Range", 30f, 5f..50f)
    private var verticalJitter by floatValue("Y-Jitter", 0.5f, 0f..2f)

    private var angle = 0.0
    private var lastMoveTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val now = System.currentTimeMillis()
        if (now - lastMoveTime < 25) return  // Fast update rate
        lastMoveTime = now

        val player = session.localPlayer
        val target = findClosestEntity() ?: return

        // Increase angle rapidly for fast spin
        angle = (angle + circleSpeed * 10) % 360
        val rad = Math.toRadians(angle)

        val offsetX = cos(rad) * radius
        val offsetZ = sin(rad) * radius

        val jitterX = ((Math.random() - 0.5) * 2 * jitterPower).toFloat()
        val jitterY = ((Math.random() - 0.5) * 2 * verticalJitter).toFloat()
        val jitterZ = ((Math.random() - 0.5) * 2 * jitterPower).toFloat()

        val followPos = target.vec3Position
        val moveTo = Vector3f.from(
            (followPos.x + offsetX + jitterX).toFloat(),
            (followPos.y + 1.2f + jitterY).toFloat(), // Stay slightly above ground for "critical hits"
            (followPos.z + offsetZ + jitterZ).toFloat()
        )

        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = moveTo
            rotation = player.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = false
            ridingRuntimeEntityId = 0
            tick = player.tickExists
        })
    }

    private fun findClosestEntity(): Entity? {
        return session.level.entityMap.values
            .filter { it != session.localPlayer }
            .filter { it.distance(session.localPlayer) <= maxTargetRange }
            .minByOrNull { it.distance(session.localPlayer) }
    }
}
