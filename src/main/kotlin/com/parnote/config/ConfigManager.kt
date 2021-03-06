package com.parnote.config

import com.parnote.config.migration.ConfigMigration_1_2
import com.parnote.config.migration.ConfigMigration_2_3
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.kotlin.config.getConfigAwait
import kotlinx.coroutines.runBlocking
import java.io.File

class ConfigManager(mLogger: Logger, mVertx: Vertx) {

    private val mMigrations = listOf(
        ConfigMigration_1_2(),
        ConfigMigration_2_3()
    )

    private val mConfig = com.beust.klaxon.JsonObject()

    private val mConfigFile = File("config.json")

    private val mIsFileConfig = when {
        mConfigFile.exists() -> true
        mConfigFile.createNewFile() -> {
            mConfigFile.writeText(DEFAULT_CONFIG.toJsonString(true))

            true
        }
        else -> false
    }

    companion object {
        private const val CONFIG_VERSION = 3

        private val DEFAULT_CONFIG by lazy {
            JsonObject(
                mapOf(
                    "config-version" to CONFIG_VERSION,

                    "recaptcha-secret" to System.getProperty("recaptcha.secret", ""),

                    "database" to mapOf(
                        "host" to System.getProperty("db.host", ""),
                        "name" to System.getProperty("db.name", ""),
                        "username" to System.getProperty("db.username", ""),
                        "password" to System.getProperty("db.password", ""),
                        "prefix" to System.getProperty("db.prefix", "parnote_")
                    ),

                    "email" to mapOf(
                        "address" to System.getProperty("email.address", ""),
                        "host" to System.getProperty("email.host", ""),
                        "port" to Integer.getInteger("email.port", 465),
                        "username" to System.getProperty("email.username", ""),
                        "password" to System.getProperty("email.password", ""),
                        "SSL" to (System.getProperty("email.ssl", "1") == "1")
                    ),

                    "resourcesDir" to "src/main/resources/",
                    "ui-address" to System.getProperty("ui.address", "http://localhost:5000")
                )
            )
        }
    }

    init {
        if (mIsFileConfig) {
            val fileStore = ConfigStoreOptions()
                .setType("file")
                .setConfig(JsonObject().put("path", "config.json"))

            val options = ConfigRetrieverOptions().addStore(fileStore)

            val retriever = ConfigRetriever.create(mVertx, options)

            loadConfigFromFile(retriever)

            migrate()

            loadConfigFromFile(retriever)

            retriever.listen { change ->
                mConfig.clear()

                mConfig.putAll(change.newConfiguration.map)
            }
        } else {
            mConfig.clear()

            mConfig.putAll(DEFAULT_CONFIG.map)
        }
    }

    private fun loadConfigFromFile(retriever: ConfigRetriever) {
        runBlocking {
            mConfig.clear()

            mConfig.putAll(retriever.getConfigAwait().map)
        }
    }

    private fun migrate() {
        if (getConfigVersion() != CONFIG_VERSION) {
            val listOfMigratableMigrations = mMigrations
                .filter { configMigration -> configMigration.isMigratable(getConfigVersion()) }

            listOfMigratableMigrations
                .forEach {
                    getConfig()["config-version"] = it.VERSION

                    it.migrate(this)
                }

            if (listOfMigratableMigrations.isNotEmpty())
                saveConfig()
        }
    }

    fun JsonObject.toJsonString(prettyPrint: Boolean = false, canonical: Boolean = false): String {
        val jsonObject = com.beust.klaxon.JsonObject()

        jsonObject.putAll(this.map)

        return jsonObject.toJsonString(prettyPrint, canonical)
    }

    fun saveConfig() {
        mConfigFile.writeText(mConfig.toJsonString(true))
    }

    fun getConfigVersion() = mConfig["config-version"] as Int

    fun getConfig() = mConfig
}