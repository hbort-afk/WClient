package com.retrivedmods.wclient.game.module.player

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.data.Effect // Adjust if your Effect enum is elsewhere
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class StrengthModule : Module("Strength", ModuleCategory.Player) {


    private val amplifier = 1

    override fun onDisabled() {
        if (!isSessionCreated) return


        val removePacket = MobEffectPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            event = MobEffectPacket.Event.REMOVE
            effectId = Effect.STRENGTH
        }
        session.clientBound(removePacket)
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            if (session.localPlayer.tickExists % 20 == 0L) {
                val effectPacket = MobEffectPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    event = MobEffectPacket.Event.ADD
                    effectId = Effect.STRENGTH
                    amplifier = amplifier - 1
                    isParticles = false
                    duration = 360000
                }
                session.clientBound(effectPacket)
            }
        }
    }
}
