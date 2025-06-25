package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.InterceptablePacket
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket

class FastStopModule : Module("FastStop", ModuleCategory.Motion) {


    private val enabled by boolValue("enabled", true)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet

        if (enabled && packet is MovePlayerPacket) {

            val stopPosition = packet.position


            packet.position = stopPosition



        }
    }
}
