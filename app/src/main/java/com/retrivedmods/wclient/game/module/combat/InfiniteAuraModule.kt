package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class InfiniteAuraModule : Module("InfiniteAura", ModuleCategory.Combat) {

    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("Mobs", false)

    private var cpsValue by intValue("cps", 50, 1..100) // Max CPS
    private var burst by intValue("Burst Packets", 5, 1..20) // Multihit per tick

    private var lastAttackTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val now = System.nanoTime()
            val minDelay = 1_000_000_000L / cpsValue

            if ((now - lastAttackTime) >= minDelay) {
                val targets = getAllTargets()

                for (entity in targets) {
                    repeat(burst) {
                        session.localPlayer.attack(entity)
                    }
                }

                lastAttackTime = now
            }
        }
    }

    private fun getAllTargets(): List<Entity> {
        return session.level.entityMap.values
            .filter { it.isTarget() }
            .sortedBy { it.distance(session.localPlayer) }
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (mobsOnly) false else !this.isBot()
            }
            is EntityUnknown -> {
                if (mobsOnly) this.identifier in MobList.mobTypes
                else if (playersOnly) false
                else true
            }
            else -> false
        }
    }

    private fun Player.isBot(): Boolean {
        return this !is LocalPlayer && session.level.playerMap[this.uuid]?.name.isNullOrBlank()
    }
}
