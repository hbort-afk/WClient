package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class AntiKnockbackModule : Module("AntiKnockback", ModuleCategory.Combat) {

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is SetEntityMotionPacket) {
            if (packet.runtimeEntityId == session.localPlayer.runtimeEntityId) {
                packet.motion = Vector3f.ZERO
            }
        }
    }
}
