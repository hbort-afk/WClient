package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.atan2
import kotlin.math.sqrt

class WAuraModule : Module("WAura", ModuleCategory.Combat) {

    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("mobs_only", false)
    private var derp by boolValue("Derp", true)

    private var rangeValue by floatValue("range", 50f, 2f..50f)
    private var cpsValue by intValue("cps", 25, 1..50)
    private var boost by intValue("packets", 2, 1..10)

    private var targetMode by intValue("Target Mode", 0, 0..2) // 0: Single, 1: Switch, 2: Multi
    private var switchDelay by intValue("Switch Delay", 100, 50..200)

    private var lastAttackNanoTime = 0L
    private var lastSwitchTime = 0L
    private var switchIndex = 0
    private var clientTickCounter = 0

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val nowNano = System.nanoTime()
            val nowMs = System.currentTimeMillis()
            val minAttackDelay = 1_000_000_000L / cpsValue

            if ((nowNano - lastAttackNanoTime) >= minAttackDelay) {
                val targets = searchForTargets()
                if (targets.isEmpty()) return

                val player = session.localPlayer

                when (targetMode) {
                    0 -> { // Single
                        val target = targets.first()
                        if (derp) spoofRotation(player, target)
                        repeat(boost) { player.attack(target) }
                    }

                    1 -> { // Switch
                        if ((nowMs - lastSwitchTime) >= switchDelay) {
                            if (switchIndex >= targets.size) switchIndex = 0
                            val target = targets[switchIndex++ % targets.size]
                            if (derp) spoofRotation(player, target)
                            repeat(boost) { player.attack(target) }
                            lastSwitchTime = nowMs
                        }
                    }

                    2 -> { // Multi
                        for (target in targets) {
                            if (derp) spoofRotation(player, target)
                            repeat(boost) { player.attack(target) }
                        }
                    }
                }

                lastAttackNanoTime = nowNano
            }
        }
    }

    private fun spoofRotation(player: LocalPlayer, target: Entity) {
        val dx = target.vec3Position.x - player.vec3Position.x
        val dz = target.vec3Position.z - player.vec3Position.z
        val dy = target.vec3Position.y - player.vec3Position.y

        val yaw = Math.toDegrees(atan2(-dx, dz).toDouble()).toFloat()
        val pitch = Math.toDegrees((-atan2(dy, sqrt(dx * dx + dz * dz))).toDouble()).toFloat()

        clientTickCounter = (clientTickCounter + 1) and 0xFFFF

        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = player.vec3Position
            rotation = Vector3f.from(yaw, pitch, yaw)
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = true
            tick = clientTickCounter.toLong()
        })

    }

    private fun searchForTargets(): List<Entity> {
        return session.level.entityMap.values
            .filter { it.isTarget() && it.distance(session.localPlayer) <= rangeValue }
            .sortedBy { it.distance(session.localPlayer) }
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> if (mobsOnly) false else !this.isBot()
            is EntityUnknown -> if (mobsOnly) this.identifier in MobList.mobTypes else !playersOnly
            else -> false
        }
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val info = session.level.playerMap[this.uuid]
        return info?.name.isNullOrBlank()
    }
}
