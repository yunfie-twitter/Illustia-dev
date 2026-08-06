use crate::models::{self, DeviceRecord, SyncRecord};
use base64::{Engine as _, engine::general_purpose::URL_SAFE_NO_PAD};
use bip39::{Language, Mnemonic};
use chacha20poly1305::Nonce;
use chacha20poly1305::{
    ChaCha20Poly1305, KeyInit,
    aead::{Aead as AeadTrait, Payload},
};
use ed25519_dalek::{Signature, Signer, SigningKey, Verifier, VerifyingKey};
use hkdf::Hkdf;
use hmac::Hmac;
use rand_core::OsRng;
use sha2::{Digest, Sha256};

use rand::RngCore;

pub type HmacSha512 = Hmac<sha2::Sha512>;

pub const SYNC_RECORD_AAD: &[u8] = b"PALLASYNC-AAD-v2";
pub const DEVICE_RECORD_AAD: &[u8] = b"PALLASYNC-DEVICE-AAD-v2";

pub fn generate_seed_phrase() -> String {
    let mut entropy = [0u8; 32];
    rand::thread_rng().fill_bytes(&mut entropy);
    let mnemonic = Mnemonic::from_entropy(&entropy).unwrap();
    mnemonic.to_string()
}

pub struct DerivedKeys {
    pub chain_id: String, // Base64Url
    pub encryption_key: [u8; 32],
    pub signing_key: SigningKey,
}

pub fn derive_keys_from_seed(seed_phrase: &str) -> Result<DerivedKeys, String> {
    let mnemonic =
        Mnemonic::parse_in(Language::English, seed_phrase).map_err(|_| "Invalid seed phrase")?;

    // 1. Convert to 64-byte BIP39 seed
    let seed = mnemonic.to_seed("");

    // 2. HKDF-SHA256
    let hk = Hkdf::<Sha256>::new(Some(b"PallaSync-V2-Salt"), &seed);
    let mut chain_id_bytes = [0u8; 32];
    let mut encryption_key = [0u8; 32];
    let mut signing_key_bytes = [0u8; 32];

    hk.expand(b"chain_id", &mut chain_id_bytes)
        .map_err(|_| "HKDF fail")?;
    hk.expand(b"encryption_key", &mut encryption_key)
        .map_err(|_| "HKDF fail")?;
    hk.expand(b"signing_key", &mut signing_key_bytes)
        .map_err(|_| "HKDF fail")?;

    let signing_key = SigningKey::from_bytes(&signing_key_bytes);

    let chain_id = URL_SAFE_NO_PAD.encode(chain_id_bytes);

    Ok(DerivedKeys {
        chain_id,
        encryption_key,
        signing_key,
    })
}

pub fn generate_ed25519_keypair() -> SigningKey {
    let mut csprng = OsRng;
    SigningKey::generate(&mut csprng)
}

pub fn sign(signing_key: &SigningKey, message: &[u8]) -> Signature {
    signing_key.sign(message)
}

pub fn verify(
    public_key: &VerifyingKey,
    message: &[u8],
    signature: &Signature,
) -> Result<(), ed25519_dalek::SignatureError> {
    public_key.verify(message, signature)
}

pub fn sha256_hash(data: &[u8]) -> [u8; 32] {
    let mut hasher = Sha256::new();
    hasher.update(data);
    let result = hasher.finalize();
    let mut hash = [0u8; 32];
    hash.copy_from_slice(&result);
    hash
}

pub fn derive_record_nonce(record_id: &str) -> [u8; 12] {
    let mut hasher = Sha256::new();
    hasher.update(b"PALLASYNC-NONCE-v2\0");
    hasher.update(record_id.as_bytes());
    let res = hasher.finalize();
    let mut nonce = [0u8; 12];
    nonce.copy_from_slice(&res[0..12]);
    nonce
}

pub fn encrypt_record_payload(
    key: &[u8; 32],
    nonce: &[u8; 12],
    payload: &[u8],
    aad: &[u8],
) -> Vec<u8> {
    let cipher = ChaCha20Poly1305::new(key.into());
    cipher
        .encrypt(Nonce::from_slice(nonce), Payload { msg: payload, aad })
        .expect("encryption failure")
}

pub fn decrypt_record_payload(
    key: &[u8; 32],
    nonce: &[u8; 12],
    ciphertext: &[u8],
    aad: &[u8],
) -> Result<Vec<u8>, chacha20poly1305::Error> {
    let cipher = ChaCha20Poly1305::new(key.into());
    cipher.decrypt(
        Nonce::from_slice(nonce),
        Payload {
            msg: ciphertext,
            aad,
        },
    )
}

fn decode_fixed<const N: usize>(encoded: &str, label: &str) -> Result<[u8; N], String> {
    let decoded = URL_SAFE_NO_PAD
        .decode(encoded)
        .map_err(|_| format!("Invalid base64url {label}"))?;
    decoded
        .try_into()
        .map_err(|_| format!("{label} must be {N} bytes"))
}

pub fn decode_encryption_key(encoded: &str) -> Result<[u8; 32], String> {
    decode_fixed(encoded, "encryption key")
}

pub fn decode_signing_key(encoded: &str) -> Result<SigningKey, String> {
    Ok(SigningKey::from_bytes(&decode_fixed(
        encoded,
        "signing key",
    )?))
}

pub fn decode_verifying_key(encoded: &str) -> Result<VerifyingKey, String> {
    VerifyingKey::from_bytes(&decode_fixed(encoded, "public key")?)
        .map_err(|_| "Invalid Ed25519 public key".to_string())
}

fn decode_signature(encoded: &str) -> Result<Signature, String> {
    Ok(Signature::from_bytes(&decode_fixed(encoded, "signature")?))
}

fn sign_canonical(signing_key: &SigningKey, canonical: &[u8]) -> String {
    let hash = sha256_hash(canonical);
    URL_SAFE_NO_PAD.encode(signing_key.sign(&hash).to_bytes())
}

fn verify_canonical(public_key: &VerifyingKey, signature: &Signature, canonical: &[u8]) -> bool {
    let hash = sha256_hash(canonical);
    public_key.verify(&hash, signature).is_ok()
}

pub fn sign_sync_record(record: &mut SyncRecord, signing_key: &SigningKey) -> Result<(), String> {
    let canonical = models::sync_record_signing_bytes(record)
        .map_err(|error| format!("Cannot canonicalize sync record: {error}"))?;
    record.signature = sign_canonical(signing_key, &canonical);
    Ok(())
}

pub fn verify_sync_record(
    record: &SyncRecord,
    public_key_base64_url: &str,
) -> Result<bool, String> {
    let public_key = decode_verifying_key(public_key_base64_url)?;
    let signature = decode_signature(&record.signature)?;
    let canonical = models::sync_record_signing_bytes(record)
        .map_err(|error| format!("Cannot canonicalize sync record: {error}"))?;
    Ok(verify_canonical(&public_key, &signature, &canonical))
}

pub fn verify_sync_record_json(
    record_json: &str,
    public_key_base64_url: &str,
) -> Result<bool, String> {
    let record = serde_json::from_str::<SyncRecord>(record_json)
        .map_err(|error| format!("Invalid sync record JSON: {error}"))?;
    verify_sync_record(&record, public_key_base64_url)
}

pub fn sign_device_record(
    record: &mut DeviceRecord,
    signing_key: &SigningKey,
) -> Result<(), String> {
    let canonical = models::device_record_signing_bytes(record)
        .map_err(|error| format!("Cannot canonicalize device record: {error}"))?;
    record.signature = sign_canonical(signing_key, &canonical);
    Ok(())
}

pub fn verify_device_record(record: &DeviceRecord) -> Result<bool, String> {
    let public_key = decode_verifying_key(&record.device_public_key)?;
    let signature = decode_signature(&record.signature)?;
    let canonical = models::device_record_signing_bytes(record)
        .map_err(|error| format!("Cannot canonicalize device record: {error}"))?;
    Ok(verify_canonical(&public_key, &signature, &canonical))
}

pub fn verify_device_record_json(record_json: &str) -> Result<bool, String> {
    let record = serde_json::from_str::<DeviceRecord>(record_json)
        .map_err(|error| format!("Invalid device record JSON: {error}"))?;
    verify_device_record(&record)
}

#[allow(clippy::too_many_arguments)]
pub fn create_sync_record_at(
    chain_id: &str,
    record_id: &str,
    collection_name: &str,
    action: &str,
    payload: &[u8],
    device_id: &str,
    encryption_key: &[u8; 32],
    signing_key: &SigningKey,
    created_at_ms: i64,
) -> Result<SyncRecord, String> {
    let nonce = derive_record_nonce(record_id);
    let encrypted_payload = URL_SAFE_NO_PAD.encode(encrypt_record_payload(
        encryption_key,
        &nonce,
        payload,
        SYNC_RECORD_AAD,
    ));
    let mut record = SyncRecord {
        protocol_version: "2.0".to_string(),
        chain_id: chain_id.to_string(),
        record_id: record_id.to_string(),
        collection_name: collection_name.to_string(),
        action: action.to_string(),
        encrypted_payload,
        device_id: device_id.to_string(),
        created_at_ms,
        signature: String::new(),
    };
    sign_sync_record(&mut record, signing_key)?;
    Ok(record)
}

pub fn decrypt_sync_record(
    record: &SyncRecord,
    encryption_key: &[u8; 32],
) -> Result<Vec<u8>, String> {
    let ciphertext = URL_SAFE_NO_PAD
        .decode(&record.encrypted_payload)
        .map_err(|_| "Invalid base64url encrypted payload".to_string())?;
    decrypt_record_payload(
        encryption_key,
        &derive_record_nonce(&record.record_id),
        &ciphertext,
        SYNC_RECORD_AAD,
    )
    .map_err(|_| "Cannot decrypt sync record".to_string())
}

pub fn create_device_record_at(
    chain_id: &str,
    device_id: &str,
    device_name: &[u8],
    encryption_key: &[u8; 32],
    signing_key: &SigningKey,
    created_at_ms: i64,
) -> Result<DeviceRecord, String> {
    let nonce = derive_record_nonce(device_id);
    let encrypted_device_name = URL_SAFE_NO_PAD.encode(encrypt_record_payload(
        encryption_key,
        &nonce,
        device_name,
        DEVICE_RECORD_AAD,
    ));
    let mut record = DeviceRecord {
        protocol_version: "2.0".to_string(),
        chain_id: chain_id.to_string(),
        device_id: device_id.to_string(),
        encrypted_device_name,
        device_public_key: URL_SAFE_NO_PAD.encode(signing_key.verifying_key().as_bytes()),
        created_at_ms,
        signature: String::new(),
    };
    sign_device_record(&mut record, signing_key)?;
    Ok(record)
}

pub fn decrypt_device_name(
    encrypted_device_name: &str,
    device_id: &str,
    encryption_key: &[u8; 32],
) -> Result<Vec<u8>, String> {
    let ciphertext = URL_SAFE_NO_PAD
        .decode(encrypted_device_name)
        .map_err(|_| "Invalid base64url encrypted device name".to_string())?;
    decrypt_record_payload(
        encryption_key,
        &derive_record_nonce(device_id),
        &ciphertext,
        DEVICE_RECORD_AAD,
    )
    .map_err(|_| "Cannot decrypt device name".to_string())
}
