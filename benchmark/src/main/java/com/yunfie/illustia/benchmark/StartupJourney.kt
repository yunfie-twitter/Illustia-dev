package com.yunfie.illustia.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope

internal const val TARGET_PACKAGE = "com.yunfie.illustia"

/** Shared startup path so profile generation and measurements cannot drift apart. */
internal fun MacrobenchmarkScope.launchFromHome() {
    pressHome()
    startActivityAndWait()
    device.waitForIdle()
}
