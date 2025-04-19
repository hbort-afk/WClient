package com.mucheng.mucute.client.game.module.combat

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.entity.*
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class WAuraModule : Module("WAura", ModuleCategory.Combat) {

    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("mobs_only", false)

    private var rangeValue by floatValue("range", 50f, 2f..50f)
    private var attackInterval by intValue("delay", 1, 1..10) // 1ms delay for faster CPS
    private var cpsValue by intValue("cps", 20, 1..50) // Increased CPS for high attack speed
    private var boost by intValue("packets", 1, 1..10)

    private var lastAttackTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            val minAttackDelay = 1000L / cpsValue

            if ((currentTime - lastAttackTime) >= minAttackDelay) {
                val closestEntities = searchForClosestEntities()
                if (closestEntities.isEmpty()) return

                closestEntities.forEach { entity ->
                    // Attack without rotating towards the entity (pure fast attack)
                    repeat(boost) {
                        session.localPlayer.attack(entity)
                    }
                    lastAttackTime = currentTime
                }
            }
        }
    }

    private fun searchForClosestEntities(): List<Entity> {
        return session.level.entityMap.values
            .filter { entity -> entity.distance(session.localPlayer) < rangeValue && entity.isTarget() }
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (mobsOnly) {
                    false
                } else if (playersOnly) {
                    !this.isBot()
                } else {
                    !this.isBot()
                }
            }
            is EntityUnknown -> {
                if (mobsOnly) {
                    isMob()
                } else if (playersOnly) {
                    false
                } else {
                    true
                }
            }
            else -> false
        }
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return true
        return playerList.name.isBlank()
    }
}
