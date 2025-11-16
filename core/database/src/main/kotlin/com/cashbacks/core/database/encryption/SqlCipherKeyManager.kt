package com.cashbacks.core.database.encryption

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

internal class SqlCipherKeyManager(context: Context) {
    private companion object {
        const val PASSPHRASE_PREFS = "secure_db_prefs"
        const val PASSPHRASE_KEY = "encryptedDbPassphrase"
        const val IV_KEY = "encryptedIv"
    }

    private val preferences = context.getSharedPreferences(
        PASSPHRASE_PREFS,
        Context.MODE_PRIVATE
    )
    private val encryptionHelper = PassphraseEncryptionHelper()

    init {
        if (!preferences.contains(PASSPHRASE_KEY)) {
            generateAndEncryptPassphrase()
        }
    }

    private fun generateAndEncryptPassphrase() {
        val passphrase = ByteArray(32)
        SecureRandom().nextBytes(passphrase)

        val encryptionData = encryptionHelper.encryptPass(passphrase)

        preferences.edit {
            putString(PASSPHRASE_KEY, encryptionData.encryptedBytes.encodeBase64())
            putString(IV_KEY, encryptionData.iv.encodeBase64())
        }

        passphrase.fill(0)
    }


    fun getSupportFactory(): SupportSQLiteOpenHelper.Factory {
        val encryptedPhrase = preferences.getString(PASSPHRASE_KEY, null).orEmpty().decodeBase64()
        val iv = preferences.getString(IV_KEY, null).orEmpty().decodeBase64()
        val passphrase = encryptionHelper.decryptPass(EncryptionData(encryptedPhrase, iv))
        return SupportFactory(passphrase)
    }


    private fun String.decodeBase64(): ByteArray = Base64.decode(this, Base64.NO_WRAP)
    private fun ByteArray.encodeBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)
}