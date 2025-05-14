package com.retrivedmods.wclient.game.module.misc

import android.util.Log
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import kotlinx.coroutines.*
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket.Type

class SpammerModule : Module("Spammer", ModuleCategory.Misc) {

    private val spamMessage = "I am using WClient"
    private var spamScope: CoroutineScope? = null

    override fun onEnabled() {
        super.onEnabled()
        startSpamming()
    }

    override fun onDisabled() {
        super.onDisabled()
        stopSpamming()
    }

    private fun startSpamming() {
        stopSpamming() // Cancel any existing scope
        spamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        spamScope?.launch {
            Log.d("SpammerModule", "Spammer started")
            while (isActive) {
                sendSpamMessage()
                delay(1000L) // Send message every second
            }
        }
    }

    private fun stopSpamming() {
        spamScope?.cancel() // Cancel the coroutine scope
        spamScope = null
        Log.d("SpammerModule", "Spammer stopped")
    }

    private fun sendSpamMessage() {
        try {
            // Create the packet with the spam message
            val packet = TextPacket().apply {
                type = Type.CHAT
                message = spamMessage
            }

            // Send the packet to both the server and client
            session.serverBound(packet)
            session.clientBound(packet)

            // Log the message that was sent
            Log.d("SpammerModule", "Sent spam message: $spamMessage")
        } catch (e: Exception) {
            // Catch any exception and log it
            Log.e("SpammerModule", "Error sending spam message: ${e.message}")
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        // Control the spammer based on packet and state
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is TextPacket && packet.type == Type.CHAT) {
            val message = packet.message.trim()
            // Automatically activate or deactivate the spammer based on the message condition
            if (!message.endsWith(spamMessage)) {
                packet.message = "$message $spamMessage"
                Log.d("SpammerModule", "Modified message: ${packet.message}")
            }

            // Check for spammer state: activate if not active, deactivate if message contains spam
            if (spamScope == null) {
                startSpamming()  // Start spamming if it's not already
            } else if (message.contains(spamMessage)) {
                stopSpamming()  // Stop spamming when the message has been sent
            }
        }
    }

    // Optional manual control
    fun activateSpammer() = startSpamming()
    fun deactivateSpammer() = stopSpamming()
}
