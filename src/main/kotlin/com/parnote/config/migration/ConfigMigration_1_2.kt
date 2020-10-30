package com.parnote.config.migration

import com.parnote.config.ConfigManager
import com.parnote.config.ConfigMigration

@Suppress("ClassName")
class ConfigMigration_1_2(
    override val FROM_VERSION: Int = 1,
    override val VERSION: Int = 2,
    override val VERSION_INFO: String = "Add mail config"
) : ConfigMigration() {
    override fun migrate(configManager: ConfigManager) {
        configManager.getConfig().putAll(
            mapOf(
                "email" to mapOf(
                    "address" to "",
                    "host" to "",
                    "port" to 465,
                    "username" to "",
                    "password" to "",
                    "SSL" to true
                )
            )
        )
    }
}