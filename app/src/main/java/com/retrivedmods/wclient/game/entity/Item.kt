package com.retrivedmods.wclient.game.entity

class Item(runtimeEntityId: Long, uniqueEntityId: Long) :
<<<<<<< HEAD
    Entity(runtimeEntityId, uniqueEntityId) {
=======
    Entity(uniqueEntityId) {
>>>>>>> 9796d3532c2f1fd11b3767244b027d90deb1284c

    override fun toString(): String {
        return "EntityItem(entityId=$runtimeEntityId, uniqueId=$uniqueEntityId, posX=$posX, posY=$posY, posZ=$posZ)"
    }
}