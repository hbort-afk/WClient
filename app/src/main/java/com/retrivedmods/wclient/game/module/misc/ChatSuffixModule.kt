package com.retrivedmods.wclient.game.module.misc

import android.util.Log
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Player // Ensure this is imported
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket.Type

class ChatSuffixModule : Module("ChatSuffix", ModuleCategory.Misc) {

    private val suffix = "→ WClient"

    // Only works if this is called for outbound packets
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        // Ensure the packet is a chat message
        if (packet is TextPacket && packet.type == Type.CHAT) {
            val message = packet.message.trim()

            // Log to check if the message is being captured
            Log.d("ChatSuffixModule", "Original message: $message")

            // Check if it's not a command and doesn't already have the suffix
            if (!message.startsWith("/") && !message.endsWith(suffix)) {
                // Modify the message with suffix
                packet.message = "$message $suffix"

                // Log the modified message for debugging
                Log.d("ChatSuffixModule", "Modified message: ${packet.message}")

                // Call the sendMessage function here to send the modified message
                sendMessage(packet.message) // Call to sendMessage
            } else {
                Log.d("ChatSuffixModule", "No modification needed for message: $message")
            }
        } else {
            Log.d("ChatSuffixModule", "Packet is not a chat message or is not of type CHAT.")
        }
    }

    // Helper function to send the message after adding the suffix
    private fun sendMessage(modifiedMessage: String) {
        // Ensure we access the correct player object (session.localPlayer)
        val player = session.localPlayer // Or session.localPlayer or any correct reference for the player

        // Send the modified message using the correct sendPacket method
        val textPacket = TextPacket().apply {
            type = Type.CHAT
            sourceName = player.username // Assuming 'username' is part of the player object
            platformChatId = "" // This can be customized if necessary
            message = modifiedMessage // Pass the modified message with suffix
        }

        // Assuming `sendPacket` is a valid method on `session`
        session.localPlayer.displayName

        // Log the message that was sent
        Log.d("ChatSuffixModule", "Sent chat message: $modifiedMessage")
    }
}
