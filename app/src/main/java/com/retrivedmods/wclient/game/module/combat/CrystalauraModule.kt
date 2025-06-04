package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class CrystalauraModule : Module("Crystal Aura", ModuleCategory.Combat) {

    private var rangeValue by floatValue("range", 6.0f, 3f..10f)  // Range to detect End Crystals
    private var attackInterval by intValue("delay", 5, 1..20)  // Attack delay in ticks
    private var cpsValue by intValue("cps", 10, 1..20)  // Clicks per second
    private var suicideValue by boolValue("Suicide", false)  // Whether the player can damage themselves with crystals

    private var lastAttackTime = 0L

    // This will check and attack End Crystals automatically in each tick
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        // We perform the attack logic at intervals based on the CPS value
        val currentTime = System.currentTimeMillis()
        val minAttackDelay = 1000L / cpsValue

        if ((currentTime - lastAttackTime) >= minAttackDelay) {
            val crystals = searchForCrystals()

            if (crystals.isNotEmpty()) {
                crystals.forEach { crystal ->
                    // Check if attacking this crystal will hurt the player too much
                    val damage = calculateCrystalDamage(crystal)

                    // Check if attacking the crystal will damage the player, and only attack if it doesn't
                    if (damage > 0f && (suicideValue || damage < session.localPlayer.health)) {
                        // Attack the crystal if damage is acceptable
                        session.localPlayer.attack(crystal)
                        lastAttackTime = currentTime
                    }
                }
            }
        }
    }

    // This function searches for all End Crystals within range of the player
    private fun searchForCrystals(): List<EntityUnknown> {
        return session.level.entityMap.values
            .filter { entity ->
                // Ensure that we're filtering for the right type of entity (End Crystal)
                entity is EntityUnknown && entity.identifier == "minecraft:end_crystal" && entity.distance(session.localPlayer) < rangeValue
            }
            .map { it as EntityUnknown } // Ensure the result is of type EntityUnknown
    }

    // This function calculates how much damage the player would take by attacking a crystal
    private fun calculateCrystalDamage(crystal: EntityUnknown): Float {
        var selfDamage = 0f

        // Custom damage simulation logic (assuming explosions cause 6 blocks of damage in a small radius)
        val explosionDamage = 6f
        // Calculate damage based on proximity (this could be further refined)
        if (crystal.distance(session.localPlayer) < rangeValue) {
            selfDamage = explosionDamage // You would replace this with actual logic for damage calculation
        }

        return selfDamage
    }
}
