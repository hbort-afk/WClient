package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes
import org.cloudburstmc.math.vector.Vector3f
import java.util.Locale
import kotlin.math.sqrt

class NameTagModule : Module("NameTag", ModuleCategory.Visual) {

    private val showDistance by boolValue("Show Distance", true)
    private val range by floatValue("Range", 100f, 10f..200f)

    private val originalNames = mutableMapOf<Long, String>()

    override fun onEnabled() {
        super.onEnabled()
        originalNames.clear()
    }

    override fun onDisabled() {
        super.onDisabled()
        session.level.entityMap.values.forEach { entity ->
            val original = originalNames[entity.runtimeEntityId] ?: return@forEach
            val meta = EntityDataMap()
            meta.put(EntityDataTypes.NAME, original)
            session.clientBound(SetEntityDataPacket().apply {
                runtimeEntityId = entity.runtimeEntityId
                metadata = meta
            })
        }
        originalNames.clear()
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || interceptablePacket.packet !is PlayerAuthInputPacket) return
        if (session.localPlayer.tickExists % 20 != 0L) return

        val self = session.localPlayer

        session.level.entityMap.values
            .filter { it != self && it.isTarget() }
            .filter { it.vec3Position.distance(self.vec3Position) < range }
            .forEach { entity ->
                if (!originalNames.containsKey(entity.runtimeEntityId)) {
                    val currentName = entity.metadata[EntityDataTypes.NAME] as? String ?: ""
                    originalNames[entity.runtimeEntityId] = currentName
                }

                val newName = formatName(entity)
                val metadata = EntityDataMap().apply {
                    put(EntityDataTypes.NAME, newName)
                }

                session.clientBound(SetEntityDataPacket().apply {
                    runtimeEntityId = entity.runtimeEntityId
                    this.metadata = metadata
                })
            }
    }

    private fun formatName(entity: Entity): String {
        val baseName = when (entity) {
            is Player -> session.level.playerMap[entity.uuid]?.name?.takeIf { it.isNotBlank() } ?: "Player"
            is EntityUnknown -> entity.identifier.substringAfterLast(":")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            else -> entity.javaClass.simpleName
        }

        val dist = if (showDistance) " [%.1fm]".format(entity.vec3Position.distance(session.localPlayer.vec3Position)) else ""


        return "§0§l§c$baseName$dist"
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> !isBot()
            is EntityUnknown -> isMob()
            else -> false
        }
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val info = session.level.playerMap[this.uuid]
        return info?.name.isNullOrBlank()
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in listOf(
            "minecraft:zombie", "minecraft:skeleton", "minecraft:creeper"
        )
    }

    private fun Vector3f.distance(other: Vector3f): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
