package com.cesoft.cesrunner

import com.cesoft.cesrunner.data.prefs.SECRETS_DATASTORE_NAME
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/// The API keys must never reach cloud backup or device transfer, while the rest of the settings must
class BackupRulesTest {
    private val secretsPath = "datastore/$SECRETS_DATASTORE_NAME.preferences_pb"
    private val settingsPath = "datastore/CesRunnerDataStore.preferences_pb"

    private fun readRules(name: String) = File("src/main/res/xml/$name").readText()

    @Test
    fun `backup rules exclude the secrets datastore only`() {
        val rules = readRules("backup_rules.xml")
        assertTrue(rules.contains("<exclude domain=\"file\" path=\"$secretsPath\"/>"))
        assertFalse(rules.contains(settingsPath))
    }

    @Test
    fun `data extraction rules exclude the secrets datastore from cloud backup and device transfer`() {
        val rules = readRules("data_extraction_rules.xml")
        val exclude = "<exclude domain=\"file\" path=\"$secretsPath\"/>"
        assertTrue(rules.substringAfter("<cloud-backup>").substringBefore("</cloud-backup>").contains(exclude))
        assertTrue(rules.substringAfter("<device-transfer>").substringBefore("</device-transfer>").contains(exclude))
        assertFalse(rules.contains(settingsPath))
    }
}
