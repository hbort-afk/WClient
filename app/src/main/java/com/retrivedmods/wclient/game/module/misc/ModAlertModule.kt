package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket

class ModAlertModule : Module("Mod Alert", ModuleCategory.Visual) {

    // Track entity ID to username
    private val trackedPlayers = mutableMapOf<Long, String>()

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet

        when (packet) {
            is AddPlayerPacket -> {
                if (isEnabled && packet.uniqueEntityId != session.localPlayer.uniqueEntityId) {
                    val username = packet.username

                    if (trackedPlayers.put(packet.uniqueEntityId, username) == null) {
                        session.displayClientMessage("§a[+] §f$username §ajoined")

                        // Check for Lifeboat Mod rank prefix
                        if (isMod(username)) {
                            session.displayClientMessage("§c[!] §4MOD ALERT: §f$username §chas joined the server!")
                            session.displayClientMessage("§e[!] Disable hacks immediately to avoid bans.")
                        }
                    }
                }
            }

            is RemoveEntityPacket -> {
                if (isEnabled) {
                    val username = trackedPlayers.remove(packet.uniqueEntityId)
                    if (username != null) {
                        session.displayClientMessage("§c[-] §f$username §cleft")
                    }
                }
            }
        }
    }

    private fun isMod(username: String): Boolean {
        // Lifeboat uses §6[Mod] prefix or §6Mod username formats
        return username.contains("§6Mod") || username.contains("§6[Mod]")
    }

    fun onDisable() {
        trackedPlayers.clear()
    }
}
