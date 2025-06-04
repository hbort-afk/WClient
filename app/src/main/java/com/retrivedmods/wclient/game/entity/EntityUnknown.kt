package com.retrivedmods.wclient.game.entity

class EntityUnknown(runtimeEntityId: Long, uniqueEntityId: Long, val identifier: String) :
<<<<<<< HEAD
    Entity(runtimeEntityId, uniqueEntityId) {
=======
    Entity(uniqueEntityId) {
>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c

    override fun toString(): String {
        return "EntityUnknown(entityId=$runtimeEntityId, uniqueId=$uniqueEntityId, identifier=$identifier, posX=$posX, posY=$posY, posZ=$posZ)"
    }
}