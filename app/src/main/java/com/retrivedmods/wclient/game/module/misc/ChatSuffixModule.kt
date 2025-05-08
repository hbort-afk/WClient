package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket.Type

class ChatSuffixModule : Module("ChatSuffix", ModuleCategory.Misc) {

    private val suffix = "→ WClient"

    // Only works if this is called for outbound packets
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is TextPacket && packet.type == Type.CHAT) {
            val message = packet.message.trim()

            if (!message.startsWith("/") && !message.endsWith(suffix)) {
                packet.message = "$message $suffix"
            }
        }
    }
}
