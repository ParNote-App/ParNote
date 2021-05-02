package com.parnote.config.migration

import com.parnote.config.ConfigManager
import com.parnote.config.ConfigMigration

@Suppress("ClassName")
class ConfigMigration_2_3(
    override val FROM_VERSION: Int = 2,
    override val VERSION: Int = 3,
    override val VERSION_INFO: String = "Add resources dir and UI address"
) : ConfigMigration() {
    override fun migrate(configManager: ConfigManager) {
        configManager.getConfig()["resourcesDir"] = "src/main/resources/"
        configManager.getConfig()["ui-address"] = "http://localhost:5000"
    }
}