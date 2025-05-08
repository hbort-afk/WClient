package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class InfiniteAuraModule : Module("InfiniteAura", ModuleCategory.Combat) {

    private var playersOnly by boolValue("Players Only", true)
    private var mobsOnly by boolValue("Mobs Only", false)
    private var cpsValue by intValue("CPS", 25, 1..100) // Lowered for better stealth
    private var burst by intValue("Burst Packets", 3, 1..10) // Reasonable multi-hit

    private var lastAttackTime = 0L
    private val entityCooldowns = mutableMapOf<Long, Long>()

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val now = System.nanoTime()
        val minDelay = 1_000_000_000L / cpsValue

        if ((now - lastAttackTime) >= minDelay) {
            val targets = getAllTargets()
            for (target in targets) {
                val runtimeId = target.runtimeEntityId
                val lastHit = entityCooldowns[runtimeId] ?: 0L
                if ((now - lastHit) < minDelay) continue

                repeat(burst) {
                    session.localPlayer.attack(target)
                }
                entityCooldowns[runtimeId] = now
            }
            lastAttackTime = now
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
            is Player -> if (mobsOnly) false else !this.isBot()
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
