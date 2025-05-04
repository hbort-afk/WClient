package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket

class PlayerJoinNotifierModule : Module("Player Logs", ModuleCategory.Visual) {


    // Map of entity ID to username
    private val trackedPlayers = mutableMapOf<Long, String>()

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet

        when (packet) {
            is AddPlayerPacket -> {
                if (isEnabled && packet.uniqueEntityId != session.localPlayer.uniqueEntityId) {
                    val username = packet.username
                    if (trackedPlayers.put(packet.uniqueEntityId, username) == null) {
                        session.displayClientMessage("§a[+] §f$username §ajoined")
                    }
                }
            }

            is RemoveEntityPacket -> {
                if (isEnabled) {
                    val username = trackedPlayers.remove(packet.uniqueEntityId)
                    if (username != null) {
                        session.displayClientMessage("§c[-] §f$username §cleft")
                    } else {
                        session.displayClientMessage("§c[-] §fA player §cleft")
                    }
                }
            }
        }
    }

    fun onDisable() {
        trackedPlayers.clear()
    }


}
