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

    private var radius by floatValue("Radius", 6.0f, 2f..20f)
    private var yOffsetBase by floatValue("yOffset", 0f, -10f..10f)
    private var circleSpeed by floatValue("circleSpeed", 2.5f, 0.5f..10f)
    private var jitterPower by floatValue("jitterPower", 0.15f, 0f..0.5f)
    private var maxTargetRange by floatValue("targetRange", 25f, 10f..40f)

    private var currentAngle = 0.0
    private var lastMoveTime = 0L
    private var jitterState = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val now = System.currentTimeMillis()
            val delta = now - lastMoveTime
            if (delta < 35) return // adjust throttle rate for smoother flight

            val target = findClosestEntity() ?: return

            // Speed-based angle update
            val angleIncrement = circleSpeed * (delta / 1000.0) * 360.0
            currentAngle = (currentAngle + angleIncrement) % 360.0

            movePlayerInCircle(target, currentAngle, radius.toDouble(), yOffsetBase)
            lastMoveTime = now

            // Flip jitter state every tick group
            jitterState = !jitterState
        }
    }

    private fun movePlayerInCircle(target: Entity, angleDeg: Double, radius: Double, yOffsetBase: Float) {
        val rad = Math.toRadians(angleDeg)
        val offsetX = cos(rad) * radius
        val offsetZ = sin(rad) * radius

        // Add jitter to bypass anti-cheat (Lifeboat-safe)
        val jitterY = if (jitterState) yOffsetBase else -yOffsetBase
        val randJitterX = (Math.random().toFloat() - 0.5f) * jitterPower
        val randJitterZ = (Math.random().toFloat() - 0.5f) * jitterPower

        val newPos = Vector3f.from(
            (target.vec3Position.x + offsetX + randJitterX).toFloat(),
            (target.vec3Position.y + jitterY).toFloat(),
            (target.vec3Position.z + offsetZ + randJitterZ).toFloat()
        )

        val movePacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPos
            rotation = session.localPlayer.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = false
            ridingRuntimeEntityId = 0
            tick = session.localPlayer.tickExists
        }

        session.clientBound(movePacket)
    }

    private fun findClosestEntity(): Entity? {
        return session.level.entityMap.values
            .filter { it != session.localPlayer }
            .filter { it.distance(session.localPlayer) <= maxTargetRange }
            .minByOrNull { it.distance(session.localPlayer) }
    }
}
