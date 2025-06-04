package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class AntiKnockbackModule : Module("anti_knockback", ModuleCategory.Combat) {

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is SetEntityMotionPacket) {
<<<<<<< HEAD
            // Reset horizontal motion to prevent knockback
            packet.motion = Vector3f.from(
                0f,  // Reset horizontal motion
                packet.motion.y,  // Maintain vertical motion
                0f   // Reset horizontal motion
            )
        }
    }
}
=======

            packet.motion = Vector3f.from(
                0f,
                packet.motion.y,
                0f
            )
        }
    }
}
>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c
