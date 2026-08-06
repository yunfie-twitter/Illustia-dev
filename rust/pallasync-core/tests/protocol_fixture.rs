use base64::{Engine as _, engine::general_purpose::URL_SAFE_NO_PAD};
use pallasync_core::{crypto, models};

const SEED_PHRASE: &str =
    "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
const RECORD_ID: &str = "018f0c2a-7b9d-7000-8000-000000000001";
const DEVICE_ID: &str = "018f0c2a-7b9d-7000-8000-000000000002";
const CREATED_AT_MS: i64 = 1_700_000_000_123;
const PAYLOAD: &str = r#"{"entity_id":"landscape","value":"landscape"}"#;
const EXPECTED_CHAIN_ID: &str = "1wMbwFYE4yTAUyAvlIGEtoGpw1y4o8HbtjQ54BRp-1s";
const EXPECTED_ENCRYPTION_KEY: &str = "BMG8TR-7ZeZrtz-89lrT-LfWuuIklBma03oKuf3Jl4M";
const EXPECTED_SIGNING_KEY: &str = "caMg1Gwn0l7J3ZyM9ZlVnGo66Lc9eeVURQ5xQeGh1fg";
const EXPECTED_PUBLIC_KEY: &str = "EGN7bhOUo9Qo73SP4QKcp7Pl7c5odsgadc0qwRjh1os";
const EXPECTED_ENCRYPTED_PAYLOAD: &str =
    "A3sX-YzzsMeGgcIoCrlhOj1hMDzoOtkjg8LXM65FB2kmzfyVDmYavlLAQrG0tYCe9FoQobALFWVg64fUnw";
const EXPECTED_SIGNATURE: &str =
    "S-u3uQoeJRWBHXZwsw9PKYgo7B_IoiEx1HA3dt3n9deA3z188srUNeX9uTvNm-RjNCUZXATOVKUDx3e9zISWBg";

#[test]
fn deterministic_seed_jcs_encryption_and_signature_fixture() {
    let keys = crypto::derive_keys_from_seed(SEED_PHRASE).expect("fixture seed is valid");
    let repeated_keys = crypto::derive_keys_from_seed(SEED_PHRASE).expect("fixture seed is valid");
    assert_eq!(keys.chain_id, repeated_keys.chain_id);
    assert_eq!(keys.encryption_key, repeated_keys.encryption_key);
    assert_eq!(
        keys.signing_key.to_bytes(),
        repeated_keys.signing_key.to_bytes()
    );
    assert_eq!(keys.chain_id, EXPECTED_CHAIN_ID);
    assert_eq!(
        URL_SAFE_NO_PAD.encode(keys.encryption_key),
        EXPECTED_ENCRYPTION_KEY
    );
    assert_eq!(
        URL_SAFE_NO_PAD.encode(keys.signing_key.to_bytes()),
        EXPECTED_SIGNING_KEY
    );
    assert_eq!(
        URL_SAFE_NO_PAD.encode(keys.signing_key.verifying_key().as_bytes()),
        EXPECTED_PUBLIC_KEY
    );

    let record = crypto::create_sync_record_at(
        &keys.chain_id,
        RECORD_ID,
        "palleria.favorite_tag/2",
        "upsert",
        PAYLOAD.as_bytes(),
        DEVICE_ID,
        &keys.encryption_key,
        &keys.signing_key,
        CREATED_AT_MS,
    )
    .expect("fixture record can be created");
    let repeated_record = crypto::create_sync_record_at(
        &repeated_keys.chain_id,
        RECORD_ID,
        "palleria.favorite_tag/2",
        "upsert",
        PAYLOAD.as_bytes(),
        DEVICE_ID,
        &repeated_keys.encryption_key,
        &repeated_keys.signing_key,
        CREATED_AT_MS,
    )
    .expect("fixture record can be recreated");
    assert_eq!(record, repeated_record);
    assert_eq!(record.encrypted_payload, EXPECTED_ENCRYPTED_PAYLOAD);
    assert_eq!(record.signature, EXPECTED_SIGNATURE);

    let canonical = String::from_utf8(
        models::sync_record_signing_bytes(&record).expect("fixture is valid JSON"),
    )
    .expect("JCS is UTF-8");
    let expected_canonical = format!(
        concat!(
            "{{\"action\":\"upsert\",",
            "\"chain_id\":\"{}\",",
            "\"collection_name\":\"palleria.favorite_tag/2\",",
            "\"created_at_ms\":1700000000123,",
            "\"device_id\":\"018f0c2a-7b9d-7000-8000-000000000002\",",
            "\"encrypted_payload\":\"{}\",",
            "\"protocol_version\":\"2.0\",",
            "\"record_id\":\"018f0c2a-7b9d-7000-8000-000000000001\"}}"
        ),
        keys.chain_id, record.encrypted_payload
    );
    assert_eq!(canonical, expected_canonical);

    let public_key = URL_SAFE_NO_PAD.encode(keys.signing_key.verifying_key().as_bytes());
    assert!(crypto::verify_sync_record(&record, &public_key).expect("fixture key is valid"));
    let mut relayed_record = serde_json::to_value(&record).expect("fixture serializes");
    relayed_record["relay_seq"] = serde_json::json!(42);
    assert!(
        crypto::verify_sync_record_json(&relayed_record.to_string(), &public_key)
            .expect("relay metadata is not part of the client signature")
    );
    assert_eq!(
        crypto::decrypt_sync_record(&record, &keys.encryption_key).expect("fixture decrypts"),
        PAYLOAD.as_bytes()
    );

    let mut tampered = record.clone();
    tampered.action = "delete".to_string();
    assert!(!crypto::verify_sync_record(&tampered, &public_key).expect("fixture key is valid"));
}

#[test]
fn device_record_fixture_is_self_signed_and_deterministic() {
    let keys = crypto::derive_keys_from_seed(SEED_PHRASE).expect("fixture seed is valid");
    let record = crypto::create_device_record_at(
        &keys.chain_id,
        DEVICE_ID,
        b"Palleria fixture device",
        &keys.encryption_key,
        &keys.signing_key,
        CREATED_AT_MS,
    )
    .expect("fixture device record can be created");
    let repeated = crypto::create_device_record_at(
        &keys.chain_id,
        DEVICE_ID,
        b"Palleria fixture device",
        &keys.encryption_key,
        &keys.signing_key,
        CREATED_AT_MS,
    )
    .expect("fixture device record can be recreated");

    assert_eq!(record, repeated);
    assert!(crypto::verify_device_record(&record).expect("fixture record is well formed"));
    assert_eq!(
        crypto::decrypt_device_name(
            &record.encrypted_device_name,
            DEVICE_ID,
            &keys.encryption_key,
        )
        .expect("fixture device name decrypts"),
        b"Palleria fixture device"
    );

    let mut tampered = record;
    tampered.created_at_ms += 1;
    assert!(!crypto::verify_device_record(&tampered).expect("fixture record is well formed"));
}
