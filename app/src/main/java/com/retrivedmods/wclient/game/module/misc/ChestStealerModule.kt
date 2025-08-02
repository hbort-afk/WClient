package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.inventory.ContainerInventory
import com.retrivedmods.wclient.game.inventory.PlayerInventory
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.ContainerClosePacket
import org.cloudburstmc.protocol.bedrock.packet.ContainerOpenPacket

class ChestStealerModule : Module("ChestStealer", ModuleCategory.Misc) {

    private val delayMs = 150L
    private var lastStealTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        if (packet is ContainerOpenPacket && packet.id.toInt() != 0) {
            stealItems()
        }
    }

    private fun stealItems() {
        val player = session?.localPlayer ?: return
        val container = player.openContainer as? ContainerInventory ?: return
        val inventory = player.inventory as? PlayerInventory ?: return

        if (System.currentTimeMillis() - lastStealTime < delayMs) return

        for (slot in container.content.indices) {
            val item = container.content[slot]
            if (item != ItemData.AIR) {
                val toSlot = inventory.findEmptySlot()
                if (toSlot != null) {
                    container.moveItem(slot, toSlot, inventory, session)
                    lastStealTime = System.currentTimeMillis()
                    return
                }
            }
        }

        // Close chest after stealing all
        session?.clientBound(ContainerClosePacket().apply {
            id = container.containerId.toByte()
            isServerInitiated = true
        })
    }
}
