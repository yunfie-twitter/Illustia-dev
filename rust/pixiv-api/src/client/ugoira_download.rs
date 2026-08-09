use std::collections::HashMap;
use std::fs;
use std::io::Write;
use std::path::{Path, PathBuf};
use std::sync::{Arc, Mutex, OnceLock, Weak};
use futures_util::StreamExt;
use tokio::sync::Mutex as AsyncMutex;

use super::{PixivHttpClient, transport};
use crate::error::{ApiError, invalid_request, io_error, network_error};
use crate::models::{UgoiraFrame, UgoiraPlayback};
use crate::temp_path::TempPath;
use crate::ugoira;

const MAX_ARCHIVE_BYTES: u64 = 512 * 1024 * 1024;
static CACHE_LOCKS: OnceLock<Mutex<HashMap<PathBuf, Weak<AsyncMutex<()>>>>> = OnceLock::new();

pub(super) async fn prepare(
    client: &PixivHttpClient,
    url: String,
    headers: HashMap<String, String>,
    cache_dir: String,
    frames: Vec<UgoiraFrame>,
) -> Result<UgoiraPlayback, ApiError> {
    let cache_dir = PathBuf::from(cache_dir);
    let cache_lock = cache_lock(&cache_dir)?;
    let _guard = cache_lock.lock().await;

    if let Some(playback) = ugoira::cached(&cache_dir, &frames) {
        return Ok(playback);
    }

    let headers = transport::request_headers(client, headers, None)?;
    let response = client
        .client
        .get(&url)
        .headers(headers)
        .send()
        .await
        .map_err(network_error)?;
    let response = transport::ensure_success(response).await?;
    if response
        .content_length()
        .is_some_and(|length| length > MAX_ARCHIVE_BYTES)
    {
        return Err(archive_too_large());
    }

    let parent = cache_dir
        .parent()
        .ok_or_else(|| invalid_request("ugoira cache directory has no parent"))?;
    fs::create_dir_all(parent).map_err(|error| io_error("create ugoira cache parent", error))?;
    let zip_path = TempPath::sibling(&cache_dir, "zip.download");
    download_and_extract(response, &zip_path, &cache_dir, frames).await
}

async fn download_and_extract(
    response: reqwest::Response,
    zip_path: &TempPath,
    cache_dir: &Path,
    frames: Vec<UgoiraFrame>,
) -> Result<UgoiraPlayback, ApiError> {
    let mut output = zip_path
        .create_file()
        .map_err(|error| io_error("create ugoira download", error))?;
    copy_with_limit(response, &mut output, MAX_ARCHIVE_BYTES).await?;
    output
        .flush()
        .map_err(|error| io_error("flush ugoira download", error))?;
    drop(output);
    ugoira::prepare(zip_path.path(), cache_dir, frames).await
}

fn cache_lock(cache_dir: &Path) -> Result<Arc<AsyncMutex<()>>, ApiError> {
    let locks = CACHE_LOCKS.get_or_init(|| Mutex::new(HashMap::new()));
    let mut locks = locks
        .lock()
        .map_err(|_| invalid_request("ugoira lock registry is poisoned"))?;
    locks.retain(|_, lock| lock.strong_count() > 0);
    if let Some(lock) = locks.get(cache_dir).and_then(Weak::upgrade) {
        return Ok(lock);
    }
    let lock = Arc::new(AsyncMutex::new(()));
    locks.insert(cache_dir.to_path_buf(), Arc::downgrade(&lock));
    Ok(lock)
}

async fn copy_with_limit(
    response: reqwest::Response,
    writer: &mut impl Write,
    limit: u64,
) -> Result<u64, ApiError> {
    let mut stream = response.bytes_stream();
    let mut copied = 0;
    while let Some(chunk) = stream.next().await {
        let chunk = chunk.map_err(network_error)?;
        copied += chunk.len() as u64;
        if copied > limit {
            return Err(archive_too_large());
        }
        writer.write_all(&chunk).map_err(|error| io_error("write ugoira download", error))?;
    }
    Ok(copied)
}

fn archive_too_large() -> ApiError {
    invalid_request("ugoira archive exceeds the download limit")
}
