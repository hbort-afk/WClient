package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.inventory.PlayerInventory
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.MobEquipmentPacket

class SlotSwitcherModule : Module("SlotSwitcher", ModuleCategory.Combat) {

    private var switchDelay by intValue("SwitchDelay", 300, 100..1000)  // Delay between slot switches (in milliseconds)
    private var lastSwitchTime = 0L  // To track the last switch time
    private var toggle = false  // To toggle between two slots

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val now = System.currentTimeMillis()
        val player = session.localPlayer as? LocalPlayer ?: return
        val inventory = player.inventory as? PlayerInventory ?: return

        // Check if enough time has passed to switch the slot
        if (now - lastSwitchTime >= switchDelay) {
            val targetSlot = if (toggle) 1 else 0  // Toggle between slot 0 and 1
            toggle = !toggle  // Change the toggle state
            lastSwitchTime = now  // Update the last switch time

            // If the current slot is different from the target, switch to the target slot
            if (inventory.heldItemSlot != targetSlot) {
                switchToSlot(player, inventory, targetSlot)
            }
        }
    }

    // Switch the player's held item slot
    private fun switchToSlot(player: LocalPlayer, inventory: PlayerInventory, slot: Int) {
        if (player.movementServerAuthoritative) return  // Don't send unnecessary packets if server-authoritative mode is enabled

        val item = inventory.content.getOrNull(slot) ?: ItemData.AIR  // Get the item from the inventory, default to AIR if slot is out of range

        val packet = MobEquipmentPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            hotbarSlot = slot  // Set the hotbar slot
            inventorySlot = slot + 9  // Adjust for the hotbar slot offset
            containerId = ContainerId.INVENTORY  // Specify that we're modifying the inventory
            this.item = item  // Set the item to the one in the target slot
        }

        session.serverBound(packet)  // Send the packet to the server

        // Update the inventory to reflect the slot change
        inventory.heldItemSlot = slot  // Update the heldItemSlot, assuming it's publicly accessible or has a setter
    }
}
