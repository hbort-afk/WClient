package com.retrivedmods.wclient.game.entity

import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAttributesPacket
import java.util.UUID

@Suppress("MayBeConstant")
open class Player(
    runtimeEntityId: Long,
    uniqueEntityId: Long,
    open val uuid: UUID,
    open val username: String
) : Entity(uniqueEntityId) {

    // Health property with `open` modifier to allow subclassing
    open var health: Float = 20.0f // Default to 20 (full health in Minecraft)

    val vec3PositionFeet: Vector3f
        get() = Vector3f.from(posX, posY, posZ)

    val displayName: String
        get() = (metadata[EntityDataTypes.NAME] as? String?)?.ifEmpty { username } ?: username

    override fun onPacketBound(packet: BedrockPacket) {
        super.onPacketBound(packet)

        // Handle movement packets
        if (packet is MovePlayerPacket && packet.runtimeEntityId == runtimeEntityId) {
            move(packet.position.x, packet.position.y, packet.position.z)
            rotate(packet.rotation)
            tickExists++
        }

        // Handle health attribute updates
        if (packet is UpdateAttributesPacket && packet.runtimeEntityId == runtimeEntityId) {
            packet.attributes.forEach { attribute ->
                if (attribute.name == "minecraft:health") {
                    health = attribute.value
                }
            }
        }
    }

    override fun toString(): String {
        return "EntityPlayer(entityId=$runtimeEntityId, uniqueId=$uniqueEntityId, username=$username, uuid=$uuid, posX=$posX, posY=$posY, posZ=$posZ, health=$health)"
    }
}