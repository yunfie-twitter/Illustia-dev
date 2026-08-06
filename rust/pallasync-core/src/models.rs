use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq, Eq)]
pub struct SyncRecord {
    pub protocol_version: String,
    pub chain_id: String,
    pub record_id: String,
    pub collection_name: String,
    pub action: String,            // "upsert" or "delete"
    pub encrypted_payload: String, // Base64url encoded ChaCha20Poly1305 ciphertext
    pub device_id: String,
    pub created_at_ms: i64,
    pub signature: String, // Base64url encoded ed25519 signature
}

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq, Eq)]
pub struct DeviceRecord {
    pub protocol_version: String,
    pub chain_id: String,
    pub device_id: String,
    pub encrypted_device_name: String,
    pub device_public_key: String, // Base64url encoded
    pub created_at_ms: i64,
    pub signature: String,
}

pub fn to_jcs<T: Serialize>(value: &T) -> Result<Vec<u8>, serde_json::Error> {
    serde_jcs::to_vec(value)
}

fn unsigned_record_jcs<T: Serialize>(record: &T) -> Result<Vec<u8>, serde_json::Error> {
    let mut value = serde_json::to_value(record)?;
    if let Some(object) = value.as_object_mut() {
        object.remove("signature");
    }
    to_jcs(&value)
}

pub fn sync_record_signing_bytes(record: &SyncRecord) -> Result<Vec<u8>, serde_json::Error> {
    unsigned_record_jcs(record)
}

pub fn device_record_signing_bytes(record: &DeviceRecord) -> Result<Vec<u8>, serde_json::Error> {
    unsigned_record_jcs(record)
}
