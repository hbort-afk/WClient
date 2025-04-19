package com.mucheng.mucute.client.game


import android.content.Context
import android.net.Uri
import com.mucheng.mucute.client.application.AppContext
import com.mucheng.mucute.client.game.module.combat.AdvanceCombatAuraModule
import com.mucheng.mucute.client.game.module.combat.AntiCrystalModule
import com.mucheng.mucute.client.game.module.combat.AntiKnockbackModule

import com.mucheng.mucute.client.game.module.combat.HitAndRunModule
import com.mucheng.mucute.client.game.module.combat.HitboxModule
import com.mucheng.mucute.client.game.module.combat.KillauraModule
import com.mucheng.mucute.client.game.module.combat.TriggerBotModule
import com.mucheng.mucute.client.game.module.combat.WAuraModule
import com.mucheng.mucute.client.game.module.misc.CrasherModule


import com.mucheng.mucute.client.game.module.misc.DesyncModule
import com.mucheng.mucute.client.game.module.misc.DisablerModule
import com.mucheng.mucute.client.game.module.misc.HasteModule

import com.mucheng.mucute.client.game.module.misc.PositionLoggerModule
import com.mucheng.mucute.client.game.module.misc.RegenerationModule
import com.mucheng.mucute.client.game.module.misc.StrengthModule

import com.mucheng.mucute.client.game.module.motion.AirJumpModule
import com.mucheng.mucute.client.game.module.motion.AntiAFKModule
import com.mucheng.mucute.client.game.module.motion.AutoWalkModule
import com.mucheng.mucute.client.game.module.motion.BhopModule
import com.mucheng.mucute.client.game.module.motion.FlyModule
import com.mucheng.mucute.client.game.module.motion.HighJumpModule
import com.mucheng.mucute.client.game.module.motion.JetPackModule
import com.mucheng.mucute.client.game.module.motion.MotionFlyModule
import com.mucheng.mucute.client.game.module.motion.SpeedModule
import com.mucheng.mucute.client.game.module.motion.SprintModule
import com.mucheng.mucute.client.game.module.visual.DamageTextModule
import com.mucheng.mucute.client.game.module.visual.ESPModule
import com.mucheng.mucute.client.game.module.visual.FreeCameraModule
import com.mucheng.mucute.client.game.module.visual.NightVisionModule

import com.mucheng.mucute.client.game.module.visual.NoHurtCameraModule

import com.mucheng.mucute.client.game.module.visual.ZoomModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File

object ModuleManager {

    private val _modules: MutableList<Module> = ArrayList()

    val modules: List<Module> = _modules

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    init {
        with(_modules) {
            add(FlyModule())
            add(ZoomModule())
            add(AirJumpModule())
add(AdvanceCombatAuraModule())
            add(SpeedModule())
            add(JetPackModule())
add(HitboxModule())
            add(TriggerBotModule())
            add(HitboxModule())
            add(HighJumpModule())
add(WAuraModule())
            add(AntiKnockbackModule())
            add(BhopModule())
            add(SprintModule())
            add(NoHurtCameraModule())
            add(AutoWalkModule())
            add(AntiAFKModule())
            add(DesyncModule())
            add(PositionLoggerModule())
            add(MotionFlyModule())
            add(FreeCameraModule())
            add(KillauraModule())
add(CrasherModule())
            add(DisablerModule())
            add(AntiCrystalModule())
add(HasteModule())
add(RegenerationModule())
            add(StrengthModule())
            add(HitAndRunModule())
add(DamageTextModule())
            add(NightVisionModule())
            add(TriggerBotModule())
            add(ESPModule())
        }
    }

    fun saveConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    if (it.private) {
                        return@forEach
                    }
                    put(it.name, it.toJson())
                }
            })
        }

        config.writeText(json.encodeToString(jsonObject))
    }

    fun loadConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        if (!config.exists()) {
            return
        }

        val jsonString = config.readText()
        if (jsonString.isEmpty()) {
            return
        }

        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val modules = jsonObject["modules"]!!.jsonObject
        _modules.forEach { module ->
            (modules[module.name] as? JsonObject)?.let {
                module.fromJson(it)
            }
        }
    }

    fun exportConfig(): String {
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    if (it.private) {
                        return@forEach
                    }
                    put(it.name, it.toJson())
                }
            })
        }
        return json.encodeToString(jsonObject)
    }

    fun importConfig(configStr: String) {
        try {
            val jsonObject = json.parseToJsonElement(configStr).jsonObject
            val modules = jsonObject["modules"]?.jsonObject ?: return

            _modules.forEach { module ->
                modules[module.name]?.let {
                    if (it is JsonObject) {
                        module.fromJson(it)
                    }
                }
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid config format")
        }
    }

    fun exportConfigToFile(context: Context, fileName: String): Boolean {
        return try {
            val configsDir = context.getExternalFilesDir("configs")
            configsDir?.mkdirs()

            val configFile = File(configsDir, "$fileName.json")
            configFile.writeText(exportConfig())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importConfigFromFile(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val configStr = input.bufferedReader().readText()
                importConfig(configStr)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}