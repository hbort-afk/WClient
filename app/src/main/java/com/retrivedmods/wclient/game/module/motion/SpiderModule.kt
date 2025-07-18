package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class SpiderModule : Module("Spider", ModuleCategory.Motion) {

    private var climbSpeed by floatValue("Climb Speed", 0.6f, 0.1f..2.5f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val player = session.localPlayer


            if (packet.inputData.contains(PlayerAuthInputData.HORIZONTAL_COLLISION)) {

                val shouldClimb = packet.inputData.contains(PlayerAuthInputData.JUMPING)

                val motionY = if (shouldClimb) climbSpeed else 0.0f

                session.clientBound(SetEntityMotionPacket().apply {
                    runtimeEntityId = player.runtimeEntityId
                    motion = Vector3f.from(
                        player.motionX,
                        motionY,
                        player.motionZ
                    )
                })
            }
        }
    }
}