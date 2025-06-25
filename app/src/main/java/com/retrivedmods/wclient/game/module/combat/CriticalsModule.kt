package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Player
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.math.vector.Vector3f

class CriticalsModule : Module("Criticals", ModuleCategory.Combat) {

    private var autoJump by boolValue("AutoJump", true)
    private var clientTickCounter = 0
    private var lastY = 0f
    private var lastJumpTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val player = session.localPlayer
        val now = System.currentTimeMillis()

        if (autoJump && isOnGround(player) && isTargetNearby()) {

            if (now - lastJumpTime >= 500) {
                performJump(player)
                lastJumpTime = now
            }
        }

        lastY = player.vec3Position.y
    }

    private fun performJump(player: Player) {
        val jumpHeight = 0.42f
        val pos = player.vec3Position
        val newPos = Vector3f.from(pos.x, pos.y + jumpHeight, pos.z)

        clientTickCounter = (clientTickCounter + 1) and 0xFFFF

        val jumpPacket = MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = newPos
            rotation = player.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = false
            tick = clientTickCounter.toLong()
        }

        session.clientBound(jumpPacket)
    }

    private fun isOnGround(player: Player): Boolean {
        val deltaY = player.vec3Position.y - lastY
        return deltaY < 0.001f
    }

    private fun isTargetNearby(): Boolean {
        return session.level.entityMap.values
            .filterIsInstance<Player>()
            .any { it != session.localPlayer && !isBot(it) && it.distance(session.localPlayer) <= 6.0f }
    }

    private fun isBot(player: Player): Boolean {
        if (player == session.localPlayer) return false
        val info = session.level.playerMap[player.uuid]
        return info?.name.isNullOrBlank()
    }
}
