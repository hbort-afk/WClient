package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket

class ModAlertModule : Module("Mod Alert", ModuleCategory.Visual) {

    private val trackedPlayers = mutableMapOf<Long, String>()

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        when (packet) {
            is AddPlayerPacket -> {
                if (packet.uniqueEntityId != session.localPlayer.uniqueEntityId) {
                    val username = packet.username

                    if (username.isNotBlank() && trackedPlayers.put(packet.uniqueEntityId, username) == null) {
                        when {
                            isMod(username) -> {
                                session.displayClientMessage("§c[!] §aMOD ALERT: §f$username §ahas joined the server!")
                                session.displayClientMessage("§e[!] Disable hacks immediately to avoid bans.")
                            }

                            isVip(username) -> {
                                session.displayClientMessage("§d[!] §6VIP ALERT: §f$username §dhas joined the server!")
                            }
                        }
                    }
                }
            }

            is RemoveEntityPacket -> {
                trackedPlayers.remove(packet.uniqueEntityId)
            }
        }
    }

    private fun isMod(username: String): Boolean {
        // Green-colored mod tags or names like §a[Mod] or §aMod
        return username.contains("§aMod", ignoreCase = true) ||
                username.contains("§2Mod", ignoreCase = true) ||
                username.contains("[Mod]", ignoreCase = true)
    }

    private fun isVip(username: String): Boolean {
        // Yellow or purple-colored VIP tags like §eVIP or §dVIP
        return username.contains("§eVIP", ignoreCase = true) ||
                username.contains("§dVIP", ignoreCase = true) ||
                username.contains("§5VIP", ignoreCase = true) ||
                username.contains("[VIP]", ignoreCase = true)
    }

     fun onDisable() {
        trackedPlayers.clear()
    }
}
