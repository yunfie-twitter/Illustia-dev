package com.yunfie.illustia.pallasync

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class PallaSyncKeySnapshot(
    val chainId: String? = null,
    val seedPhrase: String,
    val encryptionKeyBase64Url: String,
    val signingKeyBase64Url: String,
    val publicKeyBase64Url: String,
)

class PallaSyncKeystore(
    context: Context,
) {
    private val masterKey =
        MasterKey
            .Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private val sharedPrefs =
        EncryptedSharedPreferences.create(
            context,
            "pallasync_keystore",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

    fun saveRootPrivateKey(seedBase64Url: String) {
        sharedPrefs.edit().putString("root_private_seed", seedBase64Url).apply()
    }

    fun getRootPrivateKey(): String? = sharedPrefs.getString("root_private_seed", null)

    fun saveDevicePrivateKey(seedBase64Url: String) {
        sharedPrefs.edit().putString("device_private_seed", seedBase64Url).apply()
    }

    fun getDevicePrivateKey(): String? = sharedPrefs.getString("device_private_seed", null)

    fun saveDeviceSignPublicKey(keyBase64Url: String) {
        sharedPrefs.edit().putString("device_sign_public", keyBase64Url).apply()
    }

    fun getDeviceSignPublicKey(): String? = sharedPrefs.getString("device_sign_public", null)

    fun saveDeviceHpkePrivateKey(keyBase64Url: String) {
        sharedPrefs.edit().putString("device_hpke_private", keyBase64Url).apply()
    }

    fun getDeviceHpkePrivateKey(): String? = sharedPrefs.getString("device_hpke_private", null)

    fun saveDeviceHpkePublicKey(keyBase64Url: String) {
        sharedPrefs.edit().putString("device_hpke_public", keyBase64Url).apply()
    }

    fun getDeviceHpkePublicKey(): String? = sharedPrefs.getString("device_hpke_public", null)

    fun saveDeviceCertificate(certificateJson: String) {
        sharedPrefs.edit().putString("device_certificate", certificateJson).apply()
    }

    fun getDeviceCertificate(): String? = sharedPrefs.getString("device_certificate", null)

    fun saveDeviceId(deviceId: String) {
        sharedPrefs.edit().putString("device_id", deviceId).apply()
    }

    fun getDeviceId(): String? {
        var id = sharedPrefs.getString("device_id", null)
        if (id == null) {
            id =
                com.yunfie.illustia.pallasync.util.UuidV7
                    .generateString()
            saveDeviceId(id)
        }
        return id
    }

    fun saveEpochKey(epochKeyBase64Url: String) {
        sharedPrefs.edit().putString("epoch_key", epochKeyBase64Url).apply()
    }

    fun getEpochKey(): String? = sharedPrefs.getString("epoch_key", null)

    fun savePairingPrivateKey(privateKeyBase64Url: String) {
        sharedPrefs.edit().putString("pairing_private_key", privateKeyBase64Url).apply()
    }

    fun getPairingPrivateKey(): String? = sharedPrefs.getString("pairing_private_key", null)

    /**
     * Commits all active-chain key material in one encrypted preference edit.
     * The synchronous result lets the coordinator avoid activating Room state
     * when durable key storage failed.
     */
    fun saveActiveChainKeys(snapshot: PallaSyncKeySnapshot): Boolean {
        require(snapshot.seedPhrase.isNotBlank()) { "seed phrase is blank" }
        require(snapshot.encryptionKeyBase64Url.isNotBlank()) { "encryption key is blank" }
        require(snapshot.signingKeyBase64Url.isNotBlank()) { "signing key is blank" }
        require(snapshot.publicKeyBase64Url.isNotBlank()) { "public key is blank" }

        val editor =
            sharedPrefs
                .edit()
                .putString("seed_phrase", snapshot.seedPhrase)
                .putString("epoch_key", snapshot.encryptionKeyBase64Url)
                .putString("device_private_seed", snapshot.signingKeyBase64Url)
                .putString("device_sign_public", snapshot.publicKeyBase64Url)
        if (snapshot.chainId.isNullOrBlank()) {
            editor.remove("active_chain_id")
        } else {
            editor.putString("active_chain_id", snapshot.chainId)
        }
        return editor.commit()
    }

    fun getActiveChainKeys(): PallaSyncKeySnapshot? {
        val seedPhrase = getSeedPhrase()?.takeIf { it.isNotBlank() } ?: return null
        val encryptionKey = getEpochKey()?.takeIf { it.isNotBlank() } ?: return null
        val signingKey = getDevicePrivateKey()?.takeIf { it.isNotBlank() } ?: return null
        val publicKey = getDeviceSignPublicKey()?.takeIf { it.isNotBlank() } ?: return null
        return PallaSyncKeySnapshot(
            chainId = sharedPrefs.getString("active_chain_id", null),
            seedPhrase = seedPhrase,
            encryptionKeyBase64Url = encryptionKey,
            signingKeyBase64Url = signingKey,
            publicKeyBase64Url = publicKey,
        )
    }

    /** Stages a candidate without overwriting the currently active chain keys. */
    fun savePendingChainKeys(snapshot: PallaSyncKeySnapshot): Boolean {
        require(!snapshot.chainId.isNullOrBlank()) { "pending chain ID is blank" }
        return sharedPrefs
            .edit()
            .putString("pending_chain_id", snapshot.chainId)
            .putString("pending_seed_phrase", snapshot.seedPhrase)
            .putString("pending_epoch_key", snapshot.encryptionKeyBase64Url)
            .putString("pending_signing_key", snapshot.signingKeyBase64Url)
            .putString("pending_public_key", snapshot.publicKeyBase64Url)
            .commit()
    }

    fun getPendingChainKeys(): PallaSyncKeySnapshot? {
        val chainId =
            sharedPrefs.getString("pending_chain_id", null)?.takeIf { it.isNotBlank() }
                ?: return null
        return PallaSyncKeySnapshot(
            chainId = chainId,
            seedPhrase =
                sharedPrefs
                    .getString("pending_seed_phrase", null)
                    ?.takeIf { it.isNotBlank() } ?: return null,
            encryptionKeyBase64Url =
                sharedPrefs
                    .getString("pending_epoch_key", null)
                    ?.takeIf { it.isNotBlank() } ?: return null,
            signingKeyBase64Url =
                sharedPrefs
                    .getString("pending_signing_key", null)
                    ?.takeIf { it.isNotBlank() } ?: return null,
            publicKeyBase64Url =
                sharedPrefs
                    .getString("pending_public_key", null)
                    ?.takeIf { it.isNotBlank() } ?: return null,
        )
    }

    /** Promotes a previously durable candidate after Room activation succeeds. */
    fun promotePendingChainKeys(): Boolean {
        val pending = getPendingChainKeys() ?: return false
        return sharedPrefs
            .edit()
            .putString("active_chain_id", pending.chainId)
            .putString("seed_phrase", pending.seedPhrase)
            .putString("epoch_key", pending.encryptionKeyBase64Url)
            .putString("device_private_seed", pending.signingKeyBase64Url)
            .putString("device_sign_public", pending.publicKeyBase64Url)
            .remove("pending_chain_id")
            .remove("pending_seed_phrase")
            .remove("pending_epoch_key")
            .remove("pending_signing_key")
            .remove("pending_public_key")
            .commit()
    }

    fun clearPendingChainKeys(): Boolean =
        sharedPrefs
            .edit()
            .remove("pending_chain_id")
            .remove("pending_seed_phrase")
            .remove("pending_epoch_key")
            .remove("pending_signing_key")
            .remove("pending_public_key")
            .commit()

    fun clearActiveChainKeys(): Boolean =
        sharedPrefs
            .edit()
            .remove("seed_phrase")
            .remove("epoch_key")
            .remove("device_private_seed")
            .remove("device_sign_public")
            .remove("active_chain_id")
            .commit()

    fun clearAllKeys() {
        val deviceId = sharedPrefs.getString("device_id", null)
        val editor = sharedPrefs.edit().clear()
        if (deviceId != null) {
            editor.putString("device_id", deviceId)
        }
        editor.commit()
    }

    fun saveSeedPhrase(seedPhrase: String) {
        sharedPrefs.edit().putString("seed_phrase", seedPhrase).apply()
    }

    fun getSeedPhrase(): String? = sharedPrefs.getString("seed_phrase", null)
}
